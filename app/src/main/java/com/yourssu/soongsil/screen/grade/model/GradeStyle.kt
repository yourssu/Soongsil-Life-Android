package com.yourssu.soongsil.screen.grade.model

import androidx.compose.ui.graphics.Color

data class GradeStyle(
    val gradeColor: Color,
    val gradeDarkColor: Color,
    val badgeBgColor: Color,
    val badgeBgDarkColor: Color,
    val isOutlined: Boolean = false
)

// 성적 문자열에 맞는 시각 스타일(배경색, 텍스트색)을 반환합니다.
fun String.getGradeStyle(): GradeStyle {
    return when {
        startsWith("A") -> GradeStyle(
            gradeColor = Color(0xFF4C75F2),
            gradeDarkColor = Color(0xFF7295FE),
            badgeBgColor = Color(0xFFE8EFFF),
            badgeBgDarkColor = Color(0xFF1E284A)
        )
        startsWith("B") -> GradeStyle(
            gradeColor = Color(0xFF7E57C2),
            gradeDarkColor = Color(0xFFA582EC),
            badgeBgColor = Color(0xFFEFE8FF),
            badgeBgDarkColor = Color(0xFF2C2245)
        )
        startsWith("C") -> GradeStyle(
            gradeColor = Color(0xFFB58900),
            gradeDarkColor = Color(0xFFE6C153),
            badgeBgColor = Color(0xFFFFF7DB),
            badgeBgDarkColor = Color(0xFF38331A)
        )
        startsWith("D") -> GradeStyle(
            gradeColor = Color(0xFF5A626A),
            gradeDarkColor = Color(0xFFB0B8C1),
            badgeBgColor = Color(0xFFF0F2F5),
            badgeBgDarkColor = Color(0xFF2A2C30)
        )
        this == "F" -> GradeStyle(
            gradeColor = Color(0xFF2E6B9E),
            gradeDarkColor = Color(0xFF5D9ECC),
            badgeBgColor = Color(0xFFE6F4FE),
            badgeBgDarkColor = Color(0xFF1C2C3D)
        )
        this == "NP" || this == "N/P" -> GradeStyle(
            gradeColor = Color(0xFFE53935),
            gradeDarkColor = Color(0xFFFF6E6E),
            badgeBgColor = Color(0xFFFFEBEE),
            badgeBgDarkColor = Color(0xFF3A1C20)
        )
        this == "P" -> GradeStyle(
            gradeColor = Color(0xFF0062FF),
            gradeDarkColor = Color(0xFF60A5FA),
            badgeBgColor = Color(0xFFE8F3FF),
            badgeBgDarkColor = Color(0xFF1B2A4A)
        )
        else -> GradeStyle(
            gradeColor = Color(0xFF6C757D),
            gradeDarkColor = Color(0xFF8A8A8E),
            badgeBgColor = Color.Transparent,
            badgeBgDarkColor = Color.Transparent,
            isOutlined = true
        )
    }
}