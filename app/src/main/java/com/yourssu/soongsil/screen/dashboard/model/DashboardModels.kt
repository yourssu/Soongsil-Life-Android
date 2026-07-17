package com.yourssu.soongsil.screen.dashboard.model

import androidx.compose.ui.unit.Dp

data class GpaBarData(
    val label: String,
    val height: Dp,
    val isCurrent: Boolean = false,
    val gpaText: String? = null
)
