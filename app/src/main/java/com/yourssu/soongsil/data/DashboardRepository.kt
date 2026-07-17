package com.yourssu.soongsil.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.data.dashboard.DashboardData
import com.yourssu.data.dashboard.DashboardRefreshStep
import com.yourssu.data.dashboard.DashboardSemesterGrade
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.ChapelInformation
import io.github.chlwhdtn03.data.Lms.Info
import io.github.chlwhdtn03.data.Lms.SemesterGradeSummaryTable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import java.util.Locale
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

    suspend fun refreshData(
        studentId: String,
        onStepChanged: (DashboardRefreshStep) -> Unit = {}
    ): Result<DashboardData> = runCatching {
        onStepChanged(DashboardRefreshStep.STUDENT_INFO)
        val loginInfo = runCatching { getLoginInfo() }
            .onSuccess { Log.d(TAG, "사용자 정보 로드 성공: 1건") }
            .onFailure { Log.e(TAG, "사용자 정보 로드 실패", it) }
            .getOrThrow()

        onStepChanged(DashboardRefreshStep.GRADES)
        val grades = runCatching { LmsApi.getSemesterGradeSummaryTable() }
            .onSuccess { Log.d(TAG, "학기별 성적 로드 성공: ${it.items.size}건") }
            .onFailure { Log.e(TAG, "학기별 성적 로드 실패", it) }
            .getOrThrow()

        onStepChanged(DashboardRefreshStep.CHAPEL)
        val chapel = runCatching { LmsApi.getChapelTable() }
            .onSuccess {
                Log.d(
                    TAG,
                    "채플 정보 로드 성공 : 좌석 ${it.seatStatusTable.items.size}건, " +
                        "출석 ${it.attendanceTable.items.size}건"
                )
            }
            .onFailure { Log.e(TAG, "채플 정보 로드 실패", it) }
            .getOrThrow()
        val dashboardData = DashboardData(
            studentName = loginInfo.user_name,
            department = loginInfo.dept_name,
            studentId = studentId,
            overallGpa = grades.calculateOverallGpa(),
            semesterGrades = grades.toDashboardGrades(),
            chapel = chapel.toDashboardChapel()
        )

        context.dashboardDataStore.edit { preferences ->
            preferences[dashboardDataKey] = json.encodeToString(dashboardData)
        }
        dashboardData
    }

    private suspend fun getLoginInfo(): Info = suspendCancellableCoroutine { continuation ->
        LmsApi.getLoginInfo { result ->
            if (!continuation.isActive) return@getLoginInfo

            val info = result.info
            if (result.success && info != null) {
                continuation.resume(info)
            } else {
                continuation.resumeWithException(
                    IllegalStateException(result.errorMessage ?: "사용자 정보를 불러오지 못했습니다.")
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
            progress = if (required == 0) 0f else recorded.toFloat() / required
        )
    }

    private companion object {
        const val TAG = "DashboardRepository"
        const val MAX_VISIBLE_SEMESTERS = 5
    }
}
