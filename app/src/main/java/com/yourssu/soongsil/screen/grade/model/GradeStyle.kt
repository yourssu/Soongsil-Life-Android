package com.yourssu.soongsil.life.screen.grade.model

import androidx.compose.ui.graphics.Color

fun gradeStyle(grade: String): Triple<Color, Color, Color> {
    return when (grade) {
        "P" -> Triple(Color(0xFF0062FF), Color(0xFF0062FF), Color(0xFFE6F0FF))
        "A+" -> Triple(Color(0xFF059669), Color(0xFF059669), Color(0xFFECFDF5))
        "A-", "A0" -> Triple(Color(0xFF0062FF), Color(0xFF0062FF), Color(0xFFE6F0FF))
        "B+" -> Triple(Color(0xFFD97706), Color(0xFFD97706), Color(0xFFFFF7ED))
        else -> Triple(Color(0xFF8B95A1), Color(0xFF8B95A1), Color(0xFFF2F4F6))
    }
}
