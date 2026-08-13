package com.yourssu.data.grade

import kotlinx.serialization.Serializable

@Serializable
data class GradeData(
    val semesters: List<GradeSemester> = emptyList(),
    val summaries: Map<SemesterKey, GradeSemesterSummary> = emptyMap(),
    val grades: Map<SemesterKey, GradeSemesterData> = emptyMap()
)

@Serializable
data class GradeSemester(
    val label: String,
    val year: String,
    val semesterName: String,
    val cacheKey: SemesterKey = SemesterKey(year = year, semesterName = semesterName)
)

@Serializable
data class SemesterKey(
    val year: String,
    val semesterName: String
)

@Serializable
data class GradeSemesterSummary(
    val gpa: String = "-",
    val rank: String = "-"
)

@Serializable
data class GradeSemesterData(
    val courses: List<GradeCourse> = emptyList(),
    val gpa: String = "-",
    val credits: String = "-",
    val courseCount: String = "-",
    val rank: String = "-"
)

@Serializable
data class GradeCourse(
    val name: String,
    val professor: String,
    val credit: String,
    val grade: String
)
