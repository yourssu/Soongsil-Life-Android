package com.yourssu.soongsil.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourssu.data.graduation.GraduationData
import com.yourssu.data.graduation.GraduationRequirementItem
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.GraduateTableCell
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// 이수 결과 판정 문자열 중 "부족"으로 취급할 값들입니다.
private val FAIL_RESULTS = setOf("부족", "미필", "불합격")

private val Context.graduationDataStore by preferencesDataStore(name = "graduation_cache")

@Singleton
class GraduationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val graduationDataKey = stringPreferencesKey("graduation_data")
    private val json = Json { ignoreUnknownKeys = true }

    // 캐시에 저장된 졸업사정표 데이터를 불러옵니다.
    suspend fun getCachedData(): GraduationData? {
        val encodedData = context.graduationDataStore.data.first()[graduationDataKey]
            ?: return null
        return runCatching {
            json.decodeFromString<GraduationData>(encodedData)
        }.getOrNull()
    }

    // 졸업사정표 캐시를 최신 데이터로 갱신합니다.
    suspend fun updateCacheData(data: GraduationData): Result<Unit> = runCatching {
        context.graduationDataStore.edit { preferences ->
            preferences[graduationDataKey] = json.encodeToString(data)
        }
    }

    // 캐시에 저장된 졸업사정표 데이터를 삭제합니다.
    suspend fun clearCachedData(): Result<Unit> = runCatching {
        context.graduationDataStore.edit { it.clear() }
    }

    // 학과/학부별 졸업요건이나 학점 합산은 LMS 서버(getGraduateTable)가 이미 계산해서 내려줍니다.
    // 여기서는 로그인 세션 기준으로 그 결과를 받아 우리 데이터 모델로 옮겨 담기만 합니다.
    suspend fun getGraduationData(): Result<GraduationData> = runCatching {
        val table = runCatching { LmsApi.getGraduateTable() }
            .onSuccess { Log.d(TAG, "졸업사정표 로드 성공: ${it.items.size}건") }
            .onFailure { Log.e(TAG, "졸업사정표 로드 실패", it) }
            .getOrThrow()

        val items = table.items.map { it.toGraduationRequirementItem() }
        val graduationData = GraduationData(
            overallResult = items.calculateOverallResult(),
            items = items
        )

        // 로드 성공 시 로컬 캐시를 갱신합니다.
        updateCacheData(graduationData)

        graduationData
    }

    private fun GraduateTableCell.toGraduationRequirementItem() = GraduationRequirementItem(
        classification = classification,
        requirement = requirement,
        standardValue = standardValue,
        calculatedValue = calculatedValue,
        difference = difference,
        result = result,
        usedSubjects = usedSubjects
    )

    // 요건 중 하나라도 부족/미필이면 전체 판정은 "불가능"입니다.
    private fun List<GraduationRequirementItem>.calculateOverallResult(): String =
        if (any { it.result in FAIL_RESULTS }) "불가능" else "가능"

    private companion object {
        const val TAG = "GraduationRepository"
    }
}
