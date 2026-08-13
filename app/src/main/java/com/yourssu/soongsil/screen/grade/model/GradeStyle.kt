package com.yourssu.soongsil.screen.grade.model

import androidx.compose.ui.graphics.Color
import com.yourssu.soongsil.ui.theme.SoongsilPalette

data class GradeStyle(
    val gradeColor: Color,
    val gradeDarkColor: Color,
    val badgeBgColor: Color,
    val badgeBgDarkColor: Color
)

// 성적 문자열에 맞는 색상 스타일을 반환합니다.
fun String.getGradeStyle(): GradeStyle {
     return when (this) {
        "A+" -> GradeStyle(SoongsilPalette.Green500, Color(0xFF34D399), SoongsilPalette.Green50, Color(0xFF16291F))
        "P", "A-", "A0" ->GradeStyle(SoongsilPalette.Blue600, Color(0xFF60A5FA), SoongsilPalette.Blue50, Color(0xFF1B2A4A))
        "B+" -> GradeStyle(SoongsilPalette.Orange500, Color(0xFFFBBF24), SoongsilPalette.Orange50, Color(0xFF3A2E18))
        else -> GradeStyle(SoongsilPalette.Slate400, Color(0xFFA1A1AA), SoongsilPalette.Gray100, Color(0xFF3A3A3C))
    }
}