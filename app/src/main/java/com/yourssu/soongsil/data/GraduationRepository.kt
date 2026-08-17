package com.yourssu.soongsil.data

import android.util.Log
import com.yourssu.data.graduation.GraduationData
import com.yourssu.data.graduation.GraduationRequirementItem
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.GraduateTableCell
import javax.inject.Inject
import javax.inject.Singleton

// 이수 결과 판정 문자열 중 "부족"으로 취급할 값들입니다.
private val FAIL_RESULTS = setOf("부족", "미필", "불합격")

@Singleton
class GraduationRepository @Inject constructor() {

    // 학과/학부별 졸업요건이나 학점 합산은 LMS 서버(getGraduateTable)가 이미 계산해서 내려줍니다.
    // 여기서는 로그인 세션 기준으로 그 결과를 받아 우리 데이터 모델로 옮겨 담기만 합니다.
    suspend fun getGraduationData(): Result<GraduationData> = runCatching {
        val table = runCatching { LmsApi.getGraduateTable() }
            .onSuccess { Log.d(TAG, "졸업사정표 로드 성공: ${it.items.size}건") }
            .onFailure { Log.e(TAG, "졸업사정표 로드 실패", it) }
            .getOrThrow()

        val items = table.items.map { it.toGraduationRequirementItem() }
        GraduationData(
            overallResult = items.calculateOverallResult(),
            items = items
        )
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
