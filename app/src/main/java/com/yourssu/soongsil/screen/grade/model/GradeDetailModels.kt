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

data class CourseItem(
    val name: String,
    val professor: String,
    val credit: String,
    val grade: String,
    val dotColor: Color,
    val gradeColor: Color,
    val badgeBgColor: Color
)
