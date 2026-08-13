package com.yourssu.soongsil.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.data.dashboard.DashboardChapelTerm
import com.yourssu.data.dashboard.DashboardChapelWeeklyAttendance
import com.yourssu.data.dashboard.DashboardData
import com.yourssu.data.dashboard.DashboardGradeOverview
import com.yourssu.data.dashboard.DashboardSemesterGrade
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.ChapelInformation
import io.github.chlwhdtn03.data.Lms.Semester
import io.github.chlwhdtn03.data.Lms.SemesterGradeSummaryTable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val Context.dashboardDataStore by preferencesDataStore(name = "dashboard_cache")

@Singleton
class DashboardRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dashboardDataKey = stringPreferencesKey("dashboard_data")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCachedData(): DashboardData? {
        val encodedData = context.dashboardDataStore.data.first()[dashboardDataKey]
            ?: return null
        return runCatching {
            json.decodeFromString<DashboardData>(encodedData)
        }.getOrNull()
    }

    suspend fun clearCachedData(): Result<Unit> = runCatching {
        context.dashboardDataStore.edit { it.clear() }
    }

    suspend fun getChapelData(
        year: String,
        semester: Semester,
    ): Result<DashboardChapelData> = runCatching {
        LmsApi.getChapelTable(year, semester).toDashboardChapel()
    }

    suspend fun getAvailableChapelTerms(
        studentId: String,
        onTermFound: (DashboardChapelTerm) -> Unit = {},
    ): Result<List<DashboardChapelTerm>> = runCatching {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val admissionYear = studentId
            .take(4)
            .toIntOrNull()
            ?.coerceAtMost(currentYear)
            ?: currentYear
        val availableTerms = mutableListOf<DashboardChapelTerm>()

        for (year in currentYear downTo admissionYear) {
            for (semester in listOf(Semester.SECOND, Semester.FIRST)) {
                val chapelInformation = runCatching {
                    LmsApi.getChapelTable(year.toString(), semester)
                }.getOrNull() ?: continue

                val hasChapelData = chapelInformation.seatStatusTable.items.isNotEmpty() ||
                        chapelInformation.attendanceTable.items.isNotEmpty() ||
                        chapelInformation.absenceTable.items.isNotEmpty()

                if (hasChapelData) {
                    val term = DashboardChapelTerm(
                        year = chapelInformation.year,
                        semester = chapelInformation.semester.nameKor,
                    )
                    if (term !in availableTerms) {
                        availableTerms += term
                        onTermFound(term)
                    }
                }
            }
        }

        availableTerms
    }

    suspend fun refreshData(
        studentId: String,
        onRequestCompleted: (Int) -> Unit = {},
        onGradesLoaded: (DashboardGradeOverview) -> Unit = {},
        onChapelLoaded: (DashboardChapelData) -> Unit = {}
    ): Result<DashboardData> = runCatching {
        supervisorScope {
            val completedCount = AtomicInteger(0)

            // 로그인 이후 독립적인 세 요청을 먼저 모두 시작해 응답 대기 시간을 겹칩니다.
            val termsRequest = async {
                runCatching { getTerms() }
                    .onSuccess { Log.d(TAG, "학기 정보 로드 성공") }
                    .onFailure { Log.e(TAG, "학기 정보 로드 실패", it) }
                    .also { onRequestCompleted(completedCount.incrementAndGet()) }
            }
            val gradesRequest = async {
                runCatching { LmsApi.getSemesterGradeSummaryTable() }
                    .onSuccess {
                        Log.d(TAG, "학기별 성적 로드 성공: ${it.items.size}건")
                        onGradesLoaded(it.toDashboardGradeOverview())
                    }
                    .onFailure { Log.e(TAG, "학기별 성적 로드 실패", it) }
                    .also { onRequestCompleted(completedCount.incrementAndGet()) }
            }
            val chapelRequest = async {
                runCatching { LmsApi.getChapelTable().toAvailableChapelData() }
                    .onSuccess {
                        Log.d(TAG, "채플 정보 로드 성공")
                        onChapelLoaded(it)
                    }
                    .onFailure { Log.e(TAG, "채플 정보 로드 실패", it) }
                    .also { onRequestCompleted(completedCount.incrementAndGet()) }
            }

            // 한 요청이 실패해도 이미 실행 중인 나머지 요청의 응답까지 모두 받습니다.
            val termsResult = termsRequest.await()
            val gradesResult = gradesRequest.await()
            val chapelResult = chapelRequest.await()

            termsResult.getOrThrow()
            val grades = gradesResult.getOrThrow()
            val chapel = chapelResult.getOrThrow()

            val dashboardData = DashboardData(
                studentId = studentId,
                overallGpa = grades.calculateOverallGpa(),
                earnedCredits = grades.calculateEarnedCredits(),
                semesterRank = grades.latestSemesterRank(),
                totalRank = grades.latestTotalRank(),
                semesterGrades = grades.toDashboardGrades(),
                chapel = chapel
            )
            context.dashboardDataStore.edit { preferences ->
                preferences[dashboardDataKey] = json.encodeToString(dashboardData)
            }
            dashboardData
        }
    }

    private suspend fun getTerms() = suspendCancellableCoroutine<Unit> { continuation ->
        LmsApi.getTerms { result ->
            if (!continuation.isActive) return@getTerms

            if (result.success) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(
                    IllegalStateException(result.errorMessage ?: "학기 정보를 불러오지 못했습니다.")
                )
            }
        }
    }

    private fun SemesterGradeSummaryTable.calculateOverallGpa(): String {
        val gradePointSum = items.sumOf { it.gpaSum.toDoubleOrNull() ?: 0.0 }
        val gradedCredits = items.sumOf {
            val attemptedCredits = it.attemptedCredits.toDoubleOrNull() ?: 0.0
            val pfCredits = it.pfCredits.toDoubleOrNull() ?: 0.0
            (attemptedCredits - pfCredits).coerceAtLeast(0.0)
        }
        if (gradedCredits <= 0.0) return items.lastOrNull()?.gpa.orEmpty()

        return String.format(Locale.US, "%.2f", gradePointSum / gradedCredits)
    }

    private fun SemesterGradeSummaryTable.toDashboardGradeOverview() = DashboardGradeOverview(
        overallGpa = calculateOverallGpa(),
        earnedCredits = calculateEarnedCredits(),
        semesterRank = latestSemesterRank(),
        totalRank = latestTotalRank(),
        semesterGrades = toDashboardGrades()
    )

    private fun SemesterGradeSummaryTable.calculateEarnedCredits(): String {
        val credits = items.sumOf { it.earnedCredits.toDoubleOrNull() ?: 0.0 }
        return if (credits % 1.0 == 0.0) credits.toInt().toString() else credits.toString()
    }

    private fun SemesterGradeSummaryTable.latestSemesterRank(): String =
        items.maxWithOrNull(
            compareBy({ it.year }, { it.semester?.ordinal ?: -1 })
        )?.semesterRank.orEmpty()

    private fun SemesterGradeSummaryTable.latestTotalRank(): String =
        items.maxWithOrNull(
            compareBy({ it.year }, { it.semester?.ordinal ?: -1 })
        )?.totalRank.orEmpty()

    private fun SemesterGradeSummaryTable.toDashboardGrades(): List<DashboardSemesterGrade> =
        items.sortedWith(compareBy({ it.year }, { it.semester?.ordinal }))
            .takeLast(MAX_VISIBLE_SEMESTERS)
            .map { grade ->
                DashboardSemesterGrade(
                    label = buildSemesterLabel(
                        year = grade.year,
                        semester = grade.semester?.nameKor.orEmpty()
                    ),
                    gpa = grade.gpa
                )
            }

    private fun buildSemesterLabel(year: String, semester: String): String =
        "${year.takeLast(2)}-${semester.semesterLabel()}"

    private fun String.semesterLabel(): String = when {
        startsWith("1") -> "1"
        startsWith("2") -> "2"
        startsWith("여름") -> "여름"
        startsWith("겨울") -> "겨울"
        else -> removeSuffix("학기")
    }
    private suspend fun ChapelInformation.toAvailableChapelData(): DashboardChapelData {
        val newChapelData = toDashboardChapel()

        val hasNewSeatData = seatStatusTable.items.any {
            it.seatNo.isNotBlank()
        }

        if (hasNewSeatData) {
            Log.d(
                TAG,
                "새 채플 좌석 정보를 사용합니다: ${newChapelData.seat}"
            )

            return newChapelData
        }

        val cachedChapelData = getCachedData()?.chapel

        if (cachedChapelData != null && cachedChapelData.seat.isNotBlank()) {
            Log.d(
                TAG,
                "새 좌석 정보가 없어 기존 대시보드의 채플 정보를 사용합니다: " +
                        cachedChapelData.seat
            )

            return newChapelData.copy(
                seat = cachedChapelData.seat,
                seatDescription = cachedChapelData.seatDescription,
            )
        }

        Log.d(TAG, "새 좌석 정보와 저장된 좌석 정보가 모두 없습니다.")

        return newChapelData
    }

    private fun ChapelInformation.toDashboardChapel(): DashboardChapelData {
        val seatStatus = seatStatusTable.items.firstOrNull()
        val attendance = attendanceTable.items
        val attended = attendance.count { it.status.trim().startsWith("출석") }
        val late = attendance.count { it.status.trim().startsWith("지각") }
        val absent = attendance.count {
            val status = it.status.trim()
            status.contains("결석") || status.contains("미출석")
        }
        val recorded = attended + late + absent
        val required = attendance.size

        return DashboardChapelData(
            year = year,
            semester = semester.nameKor,
            seat = seatStatus?.seatNo.orEmpty(),
            seatDescription = listOfNotNull(
                seatStatus?.classroom?.takeIf { it.isNotBlank() },
                seatStatus?.timetable?.takeIf { it.isNotBlank() }
            ).joinToString(" · "),
            remaining = (required - recorded).coerceAtLeast(0),
            required = required,
            attended = attended,
            late = late,
            absent = absent,
            progress = if (required == 0) 0f else recorded.toFloat() / required,
            weeklyAttendances = attendance
                .sortedBy { it.date }
                .mapIndexed { index, item ->
                    DashboardChapelWeeklyAttendance(
                        week = index + 1,
                        date = item.date,
                        lectureType = item.lectureType,
                        speaker = item.rawValues["강사"].orEmpty(),
                        title = item.rawValues["제목"].orEmpty(),
                        status = item.status,
                    )
                },
        )
    }

    private companion object {
        const val TAG = "DashboardRepository"
        const val MAX_VISIBLE_SEMESTERS = 8
    }
}
