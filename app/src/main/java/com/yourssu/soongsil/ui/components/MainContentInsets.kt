package com.yourssu.soongsil.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 메인 화면의 하단 바 높이 패딩 값을 제공하는 CompositionLocal입니다.
val LocalMainBottomBarPadding = staticCompositionLocalOf<Dp> { 0.dp }

// 전역 스낵바를 제어하기 위한 SnackbarHostState를 제공하는 CompositionLocal입니다.
val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState가 제공되지 않았습니다.")
}

