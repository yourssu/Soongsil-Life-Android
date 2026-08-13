package com.yourssu.soongsil.data

import com.yourssu.data.timetable.TimetableCourse
import com.yourssu.data.timetable.TimetableData
import com.yourssu.data.timetable.TimetableDayOfWeek
import com.yourssu.data.timetable.TimetableSemester
import com.yourssu.data.timetable.TimetableTerm
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.Semester
import io.github.chlwhdtn03.data.Lms.Term
import io.github.chlwhdtn03.data.Lms.Timetable
import io.github.chlwhdtn03.data.Lms.TimetableCell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import io.github.chlwhdtn03.data.Lms.DayOfWeek as LmsDayOfWeek

@Singleton
class TimetableRepository @Inject constructor(
    private val lmsAuthRepository: LmsAuthRepository
) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun getAvailableTerms(
        completion: (Result<List<TimetableTerm>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { ensureActiveSession() }
                .onFailure { throwable ->
                    completion(
                        Result.failure(
                            IllegalStateException(
                                throwable.message ?: "수강 학기 정보를 불러오지 못했습니다.",
                                throwable
                            )
                        )
                    )
                }
                .onSuccess {
                    LmsApi.getTerms { result ->
                        val availableTerms = result.terms.toAvailableTimetableTerms()
                        if (result.success) {
                            completion(Result.success(availableTerms))
                        } else {
                            completion(
                                Result.failure(
                                    IllegalStateException(
                                        result.errorMessage ?: "수강 학기 정보를 불러오지 못했습니다."
                                    )
                                )
                            )
                        }
                    }
                }
        }
    }

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
        lmsAuthRepository.ensureActiveSession().getOrThrow()
    }
}

private fun TimetableSemester.toLmsSemester(): Semester = when (this) {
    TimetableSemester.FIRST -> Semester.FIRST
    TimetableSemester.SUMMER -> Semester.SUMMER
    TimetableSemester.SECOND -> Semester.SECOND
    TimetableSemester.WINTER -> Semester.WINTER
}

@OptIn(kotlin.time.ExperimentalTime::class)
internal fun List<Term>.toAvailableTimetableTerms(): List<TimetableTerm> {
    return mapNotNull { it.toTimetableTermOrNull() }
        .distinctBy { it.year to it.semester }
        .sortedWith(
            compareByDescending<TimetableTerm> { it.year.toIntOrNull() ?: Int.MIN_VALUE }
                .thenBy { it.semester.sortOrder }
        )
}

internal fun List<String?>.parseAvailableTimetableTerms(): List<TimetableTerm> {
    return mapNotNull { it.toTimetableTermOrNull() }
        .distinctBy { it.year to it.semester }
        .sortedWith(
            compareByDescending<TimetableTerm> { it.year.toIntOrNull() ?: Int.MIN_VALUE }
                .thenBy { it.semester.sortOrder }
        )
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

internal fun LmsDayOfWeek.toTimetableDayOfWeek(): TimetableDayOfWeek? = when (this) {
    LmsDayOfWeek.MONDAY -> TimetableDayOfWeek.MONDAY
    LmsDayOfWeek.TUESDAY -> TimetableDayOfWeek.TUESDAY
    LmsDayOfWeek.WEDNESDAY -> TimetableDayOfWeek.WEDNESDAY
    LmsDayOfWeek.THURSDAY -> TimetableDayOfWeek.THURSDAY
    LmsDayOfWeek.FRIDAY -> TimetableDayOfWeek.FRIDAY
    LmsDayOfWeek.SATURDAY -> TimetableDayOfWeek.SATURDAY
    LmsDayOfWeek.SUNDAY -> null
}

private fun String.normalizeWhitespace(): String = trim().replace(Regex("\\s+"), " ")

internal fun String?.toTimetableTermOrNull(): TimetableTerm? {
    val normalizedName = this?.normalizeWhitespace().orEmpty()
    if (normalizedName.isBlank()) return null
    if (normalizedName.contains("비정규과정")) return null
    if (normalizedName.contains("default term", ignoreCase = true)) return null

    val year = TIMETABLE_TERM_YEAR_REGEX.find(normalizedName)?.value ?: return null
    val semester = normalizedName.toTimetableSemesterOrNull() ?: return null

    return TimetableTerm(
        year = year,
        semester = semester,
        sourceName = normalizedName
    )
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun Term.toTimetableTermOrNull(): TimetableTerm? {
    val parsedTerm = name.toTimetableTermOrNull() ?: return null
    return parsedTerm.copy(
        startAt = start_at?.toString(),
        endAt = end_at?.toString()
    )
}

private fun String.toApiAcademicYear(): String {
    val trimmedValue = trim()
    val digits = trimmedValue.filter { it.isDigit() }
    return if (digits.length == 4) digits else trimmedValue.removeSuffix("학년도").trim()
}

private fun String.toTimetableSemesterOrNull(): TimetableSemester? {
    val lowercaseValue = lowercase(Locale.ROOT)
    return when {
        contains("1학기") -> TimetableSemester.FIRST
        contains("2학기") -> TimetableSemester.SECOND
        contains("하계") || contains("여름") || lowercaseValue.contains("summer") -> TimetableSemester.SUMMER
        contains("동계") || contains("겨울") || lowercaseValue.contains("winter") -> TimetableSemester.WINTER
        else -> null
    }
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

private val TIMETABLE_TERM_YEAR_REGEX = Regex("""\d{4}""")

private val TimetableSemester.sortOrder: Int
    get() = when (this) {
        TimetableSemester.WINTER -> 0
        TimetableSemester.SECOND -> 1
        TimetableSemester.SUMMER -> 2
        TimetableSemester.FIRST -> 3
    }
