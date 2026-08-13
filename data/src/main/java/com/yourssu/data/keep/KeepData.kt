package com.yourssu.data.keep

import kotlinx.serialization.Serializable

@Serializable
data class KeepData(
    val period: String = "",
    val reservationStatus: String = "",
    val totalCourseCount: String = "",
    val totalCredits: String = "",
    val availableCredits: String = "",
    val courses: List<KeepCourse> = emptyList()
)

@Serializable
data class KeepCourse(
    val priority: String = "",
    val plan: String = "",
    val classification: String = "",
    val multiMajorClassification: String = "",
    val engineeringCertification: String = "",
    val curriculumArea: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val section: String = "",
    val professor: String = "",
    val hoursCredits: String = "",
    val schedule: String = "",
    val applicationDate: String = "",
    val note: String = "",
    val savedStudentCount: String = ""
)
