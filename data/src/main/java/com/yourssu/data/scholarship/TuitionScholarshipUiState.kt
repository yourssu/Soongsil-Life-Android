package com.yourssu.data.scholarship

data class TuitionScholarshipUiState(
    val isTuitionLoading: Boolean = false,
    val tuitionHistories: List<TuitionHistory> = emptyList(),
    val tuitionErrorMessage: String? = null,
    val isScholarshipLoading: Boolean = false,
    val scholarshipHistories: List<ScholarshipHistory> = emptyList(),
    val scholarshipErrorMessage: String? = null
)
