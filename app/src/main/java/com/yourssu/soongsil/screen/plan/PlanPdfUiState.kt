package com.yourssu.soongsil.screen.plan

data class PlanPdfData(
    val title: String,
    val bytes: ByteArray
)

data class PlanPdfUiState(
    val isLoading: Boolean = false,
    val loadingTitle: String = "",
    val pdf: PlanPdfData? = null,
    val errorMessage: String? = null
)
