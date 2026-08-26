package com.yourssu.data.dashboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdvertisementData(
    @SerialName("image_url") val imageUrl: String,
    val link: String,
    val success: Boolean
)

@Serializable
data class DashboardData(
    val studentName: String = "",
    val department: String = "",
    val studentId: String = "",
    val overallGpa: String = "",
    val earnedCredits: String = "",
    val semesterRank: String = "",
    val totalRank: String = "",
    val semesterGrades: List<DashboardSemesterGrade> = emptyList(),
    val chapel: DashboardChapelData = DashboardChapelData()
)

data class DashboardGradeOverview(
    val overallGpa: String,
    val earnedCredits: String,
    val semesterRank: String,
    val totalRank: String,
    val semesterGrades: List<DashboardSemesterGrade>
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

@Serializable
data class DashboardChapelTerm(
    val year: String = "",
    val semester: String = "",
)

// 채플 화면 캐싱을 위한 데이터 모델입니다.
@Serializable
data class ChapelCacheData(
    val chapelData: DashboardChapelData = DashboardChapelData(),
    val availableTerms: List<DashboardChapelTerm> = emptyList(),
)

enum class DashboardRefreshStep(val current: Int) {
    CONNECTING(0),
    DATA_LOADING(0),
    ONE_COMPLETED(1),
    TWO_COMPLETED(2),
    COMPLETED(3);

    companion object {
        const val TOTAL = 3

        fun fromCompletedCount(count: Int): DashboardRefreshStep = when (count) {
            1 -> ONE_COMPLETED
            2 -> TWO_COMPLETED
            3 -> COMPLETED
            else -> CONNECTING
        }
    }
}
