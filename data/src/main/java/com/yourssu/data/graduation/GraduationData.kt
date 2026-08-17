package com.yourssu.data.graduation

import kotlinx.serialization.Serializable

/**
 * 졸업사정표 한 줄(요건) 데이터입니다.
 * LMS-API의 GraduateTableCell을 앱에서 쓰기 편한 형태로 옮겨 담습니다.
 * @param classification 이수구분 (예: "전공선택") - 화면에서 그룹 소제목으로 사용됩니다.
 * @param requirement 졸업요건 이름 (예: "학부-졸업학점 133")
 * @param standardValue 기준값 (예: "133")
 * @param calculatedValue 계산값 (예: "131.0")
 * @param difference 기준값과 계산값의 차이 (예: "-2.0")
 * @param result 이수 여부 판정 (예: "충족", "부족")
 * @param usedSubjects 해당 요건 계산에 사용된 과목명 목록
 */
@Serializable
data class GraduationRequirementItem(
    val classification: String,
    val requirement: String,
    val standardValue: String = "",
    val calculatedValue: String = "",
    val difference: String = "",
    val result: String = "",
    val usedSubjects: List<String> = emptyList()
)

/**
 * 졸업사정표 전체 데이터입니다.
 * @param overallResult 전체 졸업 판정 결과 (예: "가능", "불가능")
 * @param items 이수구분별 졸업 요건 목록
 */
@Serializable
data class GraduationData(
    val overallResult: String = "",
    val items: List<GraduationRequirementItem> = emptyList()
)
