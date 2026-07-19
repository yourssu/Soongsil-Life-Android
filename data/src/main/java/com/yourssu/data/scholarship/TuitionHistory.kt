package com.yourssu.data.scholarship

import kotlinx.serialization.Serializable

@Serializable
data class TuitionHistory(
    val year: String,
    val semester: String,
    val grade: String,
    val registrationType: String,
    val registrationDate: String,
    val amount: String,
    val reduction: String,
    val paymentAmount: String
)

data class TuitionScholarshipUiState(
    val isTuitionLoading: Boolean = false,
    val tuitionHistories: List<TuitionHistory> = emptyList(),
    val tuitionErrorMessage: String? = null
)
