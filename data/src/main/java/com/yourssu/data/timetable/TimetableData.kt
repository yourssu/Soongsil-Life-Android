package com.yourssu.data.timetable

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class TimetableData(
    val year: String = "",
    val semester: String = "",
    val courses: List<TimetableCourse> = emptyList()
)

@Serializable
data class TimetableTerm(
    val year: String,
    val semester: TimetableSemester,
    val sourceName: String = "",
    val startAt: String? = null,
    val endAt: String? = null
)

@Serializable
enum class TimetableSemester(val label: String) {
    FIRST("1학기"),
    SUMMER("여름학기"),
    SECOND("2학기"),
    WINTER("겨울학기");

    companion object {
        fun fromName(value: String): TimetableSemester? {
            val normalizedValue = value.trim().replace(" ", "")
            val lowercaseValue = normalizedValue.lowercase(Locale.ROOT)
            return when {
                normalizedValue.isBlank() -> null
                normalizedValue == FIRST.name ||
                    normalizedValue.startsWith("1") -> FIRST
                normalizedValue == SUMMER.name ||
                    normalizedValue.startsWith("하계") ||
                    normalizedValue.startsWith("여름") ||
                    lowercaseValue.startsWith("summer") -> SUMMER
                normalizedValue == SECOND.name ||
                    normalizedValue.startsWith("2") -> SECOND
                normalizedValue == WINTER.name ||
                    normalizedValue.startsWith("동계") ||
                    normalizedValue.startsWith("겨울") ||
                    lowercaseValue.startsWith("winter") -> WINTER
                else -> null
            }
        }
    }
}

@Serializable
data class TimetableCourse(
    val subject: String,
    val professor: String,
    val classroom: String,
    val dayOfWeek: TimetableDayOfWeek,
    val startMinutes: Int,
    val endMinutes: Int,
    val periodText: String
)

@Serializable
enum class TimetableDayOfWeek(val shortLabel: String, val fullLabel: String) {
    MONDAY("월", "월요일"),
    TUESDAY("화", "화요일"),
    WEDNESDAY("수", "수요일"),
    THURSDAY("목", "목요일"),
    FRIDAY("금", "금요일"),
    SATURDAY("토", "토요일");
}
