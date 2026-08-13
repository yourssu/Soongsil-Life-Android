package com.yourssu.data.coursecatalog

import kotlinx.serialization.Serializable

@Serializable
enum class CourseCatalogSemester(val displayName: String) {
    FIRST("1학기"),
    SUMMER("여름학기"),
    SECOND("2학기"),
    WINTER("겨울학기")
}

@Serializable
enum class CourseCatalogCategoryData(
    val displayName: String,
    val acceptsKeyword: Boolean = false,
    val requiresScopeSelection: Boolean = false
) {
    DEPARTMENT("학부전공별", requiresScopeSelection = true),
    REQUIRED_GENERAL("교양필수"),
    ELECTIVE_GENERAL("교양선택"),
    CHAPEL("채플"),
    TEACHING("교직"),
    GRADUATE("대학원", requiresScopeSelection = true),
    LINKED_MAJOR("연계전공"),
    CONVERGENCE_MAJOR("융합전공"),
    PROFESSOR("교수명검색", acceptsKeyword = true),
    SUBJECT("과목검색", acceptsKeyword = true),
    CROSS_MAJOR("타전공인정과목", requiresScopeSelection = true),
    CYBER_UNIVERSITY("숭실사이버대")
}

@Serializable
data class CourseCatalogFilterOptionData(
    val key: String = "",
    val label: String = ""
)

@Serializable
data class CourseCatalogFilterData(
    val index: Int = 0,
    val name: String = "",
    val selectedKey: String = "",
    val selectedLabel: String = "",
    val options: List<CourseCatalogFilterOptionData> = emptyList()
)

@Serializable
data class CourseCatalogSearchOptionsData(
    val year: String = "",
    val semester: CourseCatalogSemester = CourseCatalogSemester.FIRST,
    val category: CourseCatalogCategoryData = CourseCatalogCategoryData.DEPARTMENT,
    val filters: List<CourseCatalogFilterData> = emptyList(),
    val acceptsKeyword: Boolean = false
)

@Serializable
data class CourseCatalogSelectedFilterData(
    val name: String = "",
    val key: String = "",
    val label: String = ""
)

@Serializable
data class CourseCatalogData(
    val year: String = "",
    val semester: CourseCatalogSemester = CourseCatalogSemester.FIRST,
    val category: CourseCatalogCategoryData = CourseCatalogCategoryData.DEPARTMENT,
    val filterKeys: List<String> = emptyList(),
    val selectedFilters: List<CourseCatalogSelectedFilterData> = emptyList(),
    val keyword: String = "",
    val totalCourseCount: Int = 0,
    val courses: List<CourseCatalogCourseData> = emptyList()
)

@Serializable
data class CourseCatalogCourseData(
    val plan: String = "",
    val primaryClassification: String = "",
    val multiMajorClassification: String = "",
    val engineeringCertification: String = "",
    val curriculumArea: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val registrationNotice: String = "",
    val courseType: String = "",
    val section: String = "",
    val professor: String = "",
    val department: String = "",
    val hoursCredits: String = "",
    val enrollmentCapacity: String = "",
    val remainingSeats: String = "",
    val schedule: String = "",
    val targetStudents: String = "",
    val year: String = "",
    val semester: CourseCatalogSemester = CourseCatalogSemester.FIRST
)
