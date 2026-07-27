package com.yourssu.soongsil.data

import com.yourssu.data.timetable.TimetableCourse
import com.yourssu.data.timetable.TimetableData
import com.yourssu.data.timetable.TimetableDayOfWeek
import com.yourssu.data.timetable.TimetableSemester
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.Semester
import io.github.chlwhdtn03.data.Lms.DayOfWeek as LmsDayOfWeek
import io.github.chlwhdtn03.data.Lms.Timetable
import io.github.chlwhdtn03.data.Lms.TimetableCell
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class TimetableRepository @Inject constructor(
    private val lmsAuthRepository: LmsAuthRepository
) {
    suspend fun getTimetable(
        year: String,
        semester: TimetableSemester
    ): TimetableData {
        ensureActiveSession()
        val normalizedYear = year.toApiAcademicYear()

        return suspendCancellableCoroutine { continuation ->
            LmsApi.getTimetable(
                year = normalizedYear,
                semester = semester.toLmsSemester()
            ) { result ->
                if (!continuation.isActive) return@getTimetable

                val timetable = result.timetable
                if (result.success && timetable != null) {
                    continuation.resume(timetable.toTimetableData())
                } else {
                    continuation.resumeWithException(
                        IllegalStateException(result.errorMessage ?: "시간표를 불러오지 못했습니다.")
                    )
                }
            }
        }
    }

    private suspend fun ensureActiveSession() {
        if (lmsAuthRepository.hasActiveSession()) return

        val credentials = lmsAuthRepository.getSavedCredentials()
            ?: throw IllegalStateException("로그인이 필요합니다.")

        lmsAuthRepository.login(credentials.studentId, credentials.password).getOrThrow()
    }
}

private fun TimetableSemester.toLmsSemester(): Semester = when (this) {
    TimetableSemester.FIRST -> Semester.FIRST
    TimetableSemester.SUMMER -> Semester.SUMMER
    TimetableSemester.SECOND -> Semester.SECOND
    TimetableSemester.WINTER -> Semester.WINTER
}

private fun Timetable.toTimetableData(): TimetableData {
    val courses = items.mapNotNull { it.toTimetableCourse() }
        .sortedWith(compareBy<TimetableCourse> { it.dayOfWeek.ordinal }.thenBy { it.startMinutes })

    return TimetableData(
        year = year.normalizeWhitespace(),
        semester = semester.normalizeWhitespace(),
        courses = courses
    )
}

private fun TimetableCell.toTimetableCourse(): TimetableCourse? {
    val dayOfWeek = dayOfWeek.toTimetableDayOfWeek() ?: return null
    val timeRange = TimetableTimeParser.parseRange(primary = time, fallback = periodTime) ?: return null

    return TimetableCourse(
        subject = subject.normalizeWhitespace(),
        professor = professor.normalizeWhitespace(),
        classroom = classroom.normalizeWhitespace(),
        dayOfWeek = dayOfWeek,
        startMinutes = timeRange.startMinutes,
        endMinutes = timeRange.endMinutes,
        periodText = timeRange.displayText
    )
}

private fun LmsDayOfWeek.toTimetableDayOfWeek(): TimetableDayOfWeek? = when (this) {
    LmsDayOfWeek.MONDAY -> TimetableDayOfWeek.MONDAY
    LmsDayOfWeek.TUESDAY -> TimetableDayOfWeek.TUESDAY
    LmsDayOfWeek.WEDNESDAY -> TimetableDayOfWeek.WEDNESDAY
    LmsDayOfWeek.THURSDAY -> TimetableDayOfWeek.THURSDAY
    LmsDayOfWeek.FRIDAY -> TimetableDayOfWeek.FRIDAY
    LmsDayOfWeek.SATURDAY,
    LmsDayOfWeek.SUNDAY -> null
}

private fun String.normalizeWhitespace(): String = trim().replace(Regex("\\s+"), " ")

private fun String.toApiAcademicYear(): String {
    val trimmedValue = trim()
    val digits = trimmedValue.filter { it.isDigit() }
    return if (digits.length == 4) digits else trimmedValue.removeSuffix("학년도").trim()
}

internal object TimetableTimeParser {
    private val timeRegex = Regex("""(\d{1,2})\s*:\s*(\d{2})""")

    fun parseRange(primary: String, fallback: String): ParsedTimeRange? {
        return listOf(primary, fallback)
            .mapNotNull { parseCandidate(it) }
            .firstOrNull()
    }

    private fun parseCandidate(value: String): ParsedTimeRange? {
        val matches = timeRegex.findAll(value).toList()
        if (matches.size < 2) return null

        val startMinutes = matches[0].toMinutes() ?: return null
        val endMinutes = matches[1].toMinutes() ?: return null
        if (endMinutes <= startMinutes) return null

        return ParsedTimeRange(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            displayText = "${formatMinutes(startMinutes)}-${formatMinutes(endMinutes)}"
        )
    }

    private fun MatchResult.toMinutes(): Int? {
        val hour = groupValues[1].toIntOrNull() ?: return null
        val minute = groupValues[2].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }

    private fun formatMinutes(minutes: Int): String {
        val hour = minutes / 60
        val minute = minutes % 60
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }
}

internal data class ParsedTimeRange(
    val startMinutes: Int,
    val endMinutes: Int,
    val displayText: String
)
