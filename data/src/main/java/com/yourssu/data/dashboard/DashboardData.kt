package com.yourssu.data.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DashboardData(
    val studentName: String = "",
    val department: String = "",
    val studentId: String = "",
    val overallGpa: String = "",
    val semesterGrades: List<DashboardSemesterGrade> = emptyList(),
    val chapel: DashboardChapelData = DashboardChapelData()
)

@Serializable
data class DashboardSemesterGrade(
    val label: String,
    val gpa: String
)

@Serializable
data class DashboardChapelData(
    val year: String = "",
    val semester: String = "",
    val seat: String = "",
    val seatDescription: String = "",
    val remaining: Int = 0,
    val required: Int = 0,
    val attended: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
    val progress: Float = 0f,
    val weeklyAttendances: List<DashboardChapelWeeklyAttendance> = emptyList(),
)

@Serializable
data class DashboardChapelWeeklyAttendance(
    val week: Int = 0,
    val date: String = "",
    val lectureType: String = "",
    val speaker: String = "",
    val title: String = "",
    val status: String = "",
)

data class DashboardChapelTerm(
    val year: String,
    val semester: String,
)

enum class DashboardRefreshStep(val current: Int) {
    CONNECTING(0),
    STUDENT_INFO(1),
    GRADES(2),
    CHAPEL(3);

    companion object {
        const val TOTAL = 3
    }
}
