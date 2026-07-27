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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
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

    suspend fun login(studentId: String, password: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            LmsApi.loginLMS(studentId, password) { result ->
                if (!continuation.isActive) return@loginLMS

                if (result.success) {
                    continuation.resume(Result.success(Unit))
                } else {
                    continuation.resume(
                        Result.failure(
                            IllegalArgumentException(
                                result.errorMessage ?: "로그인에 실패했습니다."
                            )
                        )
                    )
                }
            }
        }

    fun hasActiveSession(): Boolean = LmsApi.isLoggined

    suspend fun logout(): Result<Unit> = runCatching {
        clearCredentials()
        suspendCancellableCoroutine { continuation ->
            LmsApi.logout {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
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
    }
}
