package com.yourssu.soongsil.screen.grade.model

import androidx.compose.ui.graphics.Color
import io.github.chlwhdtn03.data.Lms.Semester

data class SemesterTab(
    val label: String,
    val isActive: Boolean = false,
    val year: String,
    val semester: Semester
)

data class GpaPoint(
    val semester: String,
    val gpa: Float,
    val isCurrent: Boolean = false
)

val GpaPoint.shortSemesterName
    get() = semester.substring(2)

data class CourseItem(
    val name: String,
    val professor: String,
    val credit: String,
    val grade: String,
    val gradeColor: Color,
    val gradeDarkColor: Color,
    val badgeBgColor: Color,
    val badgeBgDarkColor: Color
)

fun CourseItem.badgeBgColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) badgeBgDarkColor
    else badgeBgColor
}

fun CourseItem.gradeColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) gradeDarkColor
    else gradeColor
}

fun CourseItem.dodColor(isDarkMode: Boolean): Color = gradeColor(isDarkMode)