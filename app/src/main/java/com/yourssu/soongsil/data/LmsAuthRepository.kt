package com.yourssu.soongsil.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourssu.data.auth.LmsCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.chlwhdtn03.LmsApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private val Context.lmsCredentialsDataStore by preferencesDataStore(name = "lms_credentials")

@Singleton
class LmsAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 화면 이동과 관계없이 메인 로그인 작업이 완료되도록 앱 수명의 스코프를 사용합니다.
    private val loginScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // 전역 LMS 세션이 동시에 변경되지 않도록 로그인 요청을 순서대로 처리합니다.
    private val loginMutex = Mutex()
    private val studentIdKey = stringPreferencesKey("student_id")
    private val encryptedPasswordKey = stringPreferencesKey("encrypted_password")
    private val passwordIvKey = stringPreferencesKey("password_iv")

    suspend fun getSavedCredentials(): LmsCredentials? {
        val preferences = runCatching {
            context.lmsCredentialsDataStore.data.first()
        }.getOrNull() ?: return null

        val studentId = preferences[studentIdKey]?.takeIf { it.isNotBlank() } ?: return null
        val encryptedPassword = preferences[encryptedPasswordKey] ?: return null
        val passwordIv = preferences[passwordIvKey] ?: return null

        return runCatching {
            LmsCredentials(
                studentId = studentId,
                password = decryptPassword(encryptedPassword, passwordIv)
            )
        }.getOrElse {
            clearCredentials()
            null
        }
    }

    suspend fun saveCredentials(studentId: String, password: String): Result<Unit> = runCatching {
        val (encryptedPassword, passwordIv) = encryptPassword(password)
        context.lmsCredentialsDataStore.edit { preferences ->
            preferences[studentIdKey] = studentId
            preferences[encryptedPasswordKey] = encryptedPassword
            preferences[passwordIvKey] = passwordIv
        }
    }

    suspend fun login(studentId: String, password: String): Result<Unit> {
        val loginTask = loginScope.async {
            loginMutex.withLock {
                // 이미 생성된 LMS 세션이 있으면 중복 로그인을 요청하지 않습니다.
                if (hasActiveSession()) return@withLock Result.success(Unit)

                performLogin(studentId, password)
            }
        }

        return try {
            loginTask.await()
        } catch (exception: CancellationException) {
            // 호출 화면이 닫혀도 앱 수명의 로그인 작업은 계속 진행합니다.
            throw exception
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }

    private suspend fun performLogin(studentId: String, password: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            LmsApi.loginLMS(studentId, password) { result ->
                if (!continuation.isActive) return@loginLMS

                if (result.success) {
                    continuation.resume(Result.success(Unit))
                } else {
                    val errorMessage = result.errorMessage ?: "로그인에 실패했습니다."
                    continuation.resume(
                        Result.failure(
                            if (errorMessage == INVALID_CREDENTIALS_MESSAGE) {
                                LmsLoginRequiredException(errorMessage)
                            } else {
                                IllegalStateException(errorMessage)
                            }
                        )
                    )
                }
            }
        }

    // 메인 로그인과 자격 증명 저장을 화면의 수명과 분리하여 끝까지 처리합니다.
    suspend fun loginAndSaveCredentials(studentId: String, password: String): Result<Unit> {
        val loginTask = loginScope.async {
            loginMutex.withLock {
                // 다른 화면에서 먼저 로그인했다면 입력값을 덮어쓰지 않고 그대로 완료합니다.
                if (hasActiveSession()) return@withLock

                performLogin(studentId, password).getOrThrow()
                saveCredentials(studentId, password).getOrElse { throwable ->
                    throw IllegalStateException(
                        "로그인 정보를 안전하게 저장하지 못했습니다.",
                        throwable
                    )
                }
            }
        }

        return try {
            loginTask.await()
            Result.success(Unit)
        } catch (exception: CancellationException) {
            // 호출 화면만 닫힌 경우 앱 수명의 로그인 작업은 계속 진행합니다.
            throw exception
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }

    fun hasActiveSession(): Boolean = LmsApi.isLoggined

    // 모든 LMS 기능이 동일한 방식으로 저장된 계정의 세션을 복구하도록 합니다.
    suspend fun ensureActiveSession(): Result<Unit> {
        if (hasActiveSession()) return Result.success(Unit)

        val credentials = getSavedCredentials()
            ?: return Result.failure(LmsLoginRequiredException("로그인이 필요합니다."))

        return login(credentials.studentId, credentials.password)
            .onFailure { throwable ->
                // 잘못 저장된 계정으로 자동 로그인이 반복되지 않도록 인증 정보만 삭제합니다.
                if (throwable is LmsLoginRequiredException) clearCredentials()
            }
    }

    suspend fun logout(): Result<Unit> = runCatching {
        clearCredentials()
        LmsApi.logout {  }
    }

    private suspend fun clearCredentials() {
        context.lmsCredentialsDataStore.edit { it.clear() }
    }

    private fun encryptPassword(password: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        return Pair(
            Base64.encodeToString(cipher.doFinal(password.toByteArray()), Base64.NO_WRAP),
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        )
    }

    private fun decryptPassword(encryptedPassword: String, passwordIv: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(passwordIv, Base64.NO_WRAP)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))

        val decrypted = cipher.doFinal(Base64.decode(encryptedPassword, Base64.NO_WRAP))
        return decrypted.toString(Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE_PROVIDER)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private companion object {
        const val KEY_STORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "lms_password_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val INVALID_CREDENTIALS_MESSAGE = "아이디 또는 비밀번호가 일치하지 않습니다."
    }
}

// 자동 로그인을 계속 시도할 수 없는 경우 로그인 화면 이동 여부를 구분하는 예외입니다.
class LmsLoginRequiredException(message: String) : IllegalStateException(message)

// 저장소에서 예외를 감싸더라도 로그인 화면 이동 사유를 잃지 않도록 확인합니다.
fun Throwable.isLmsLoginRequired(): Boolean =
    generateSequence(this) { it.cause }.any { it is LmsLoginRequiredException }

// LMS API 및 네트워크 예외를 사용자 친화적인 메시지로 변환합니다.
fun Throwable.toUserFriendlyMessage(): String {
    val causes = generateSequence(this) { it.cause }
    val isWebDynproBlocked = causes.any { cause ->
        val msg = cause.message.orEmpty()
        val name = cause.javaClass.simpleName
        name.contains("WebDynproSessionException", ignoreCase = true) ||
                msg.contains("Web Dynpro", ignoreCase = true) ||
                msg.contains("화면 세션을 초기화하지 못했습니다", ignoreCase = true) ||
                cause.toString().contains("WebDynproSessionException", ignoreCase = true)
    }

    return if (isWebDynproBlocked) {
        "현재 유세인트에서 정보 요청을 거절하고 있어요. 나중에 다시 시도해주세요."
    } else {
        message ?: "데이터를 불러오는 중 오류가 발생했습니다."
    }
}
