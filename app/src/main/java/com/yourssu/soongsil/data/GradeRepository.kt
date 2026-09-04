package com.yourssu.soongsil.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourssu.data.grade.GradeCourse
import com.yourssu.data.grade.GradeData
import com.yourssu.data.grade.GradeSemester
import com.yourssu.data.grade.GradeSemesterData
import com.yourssu.data.grade.GradeSemesterSummary
import com.yourssu.data.grade.SemesterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.LmsTermsResult
import io.github.chlwhdtn03.data.Lms.Semester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.ExperimentalTime

private val Context.gradeDataStore by preferencesDataStore(name = "grade_cache")

@Singleton
class GradeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gradeDataKey = stringPreferencesKey(name = "grade_data")
    private val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    // 캐시에 저장된 성적 데이터를 불러옵니다.
    suspend fun getCachedData(): GradeData? {
        val encodedData = context.gradeDataStore.data.first()[gradeDataKey]
            ?: return null

        return runCatching {
            json.decodeFromString<GradeData>(encodedData)
        }.getOrNull()
    }

    // 캐시에 저장된 성적 데이터를 삭제합니다.
    suspend fun clearCachedData(): Result<Unit> = runCatching {
        context.gradeDataStore.edit { it.clear() }
    }

    // 성적 캐시 데이터를 최신 상태로 갱신합니다.
    suspend fun updateCacheData(data: GradeData): Result<Unit> = runCatching {
        context.gradeDataStore.edit { preferences ->
            preferences[gradeDataKey] = json.encodeToString(data)
        }
    }

    // LMS API에서 성적 조회에 사용 가능한 학기 목록을 불러옵니다.
    @OptIn(ExperimentalTime::class)
    suspend fun getSemesters(): Result<List<GradeSemester>> = runCatching {
        val terms = getTerms()
        terms.terms
            .mapNotNull { semester ->
                semester.name?.let { name ->
                    parseGradeSemester(name)
                }
            }
            .distinctBy { semester -> semester.cacheKey }
    }

    // LMS API에서 전체 학기별 성적 요약표(평점, 석차, 취득학점)를 불러옵니다.
    suspend fun getGradeSummaries(): Result<Map<SemesterKey, GradeSemesterSummary>> = runCatching {
        val summaryTable = withContext(Dispatchers.IO) {
            LmsApi.getSemesterGradeSummaryTable()
        }
        summaryTable.items
            .mapNotNull { summary ->
                val semester = summary.semester ?: return@mapNotNull null
                buildCacheKey(year = summary.year, semester = semester) to GradeSemesterSummary(
                    gpa = summary.gpa,
                    rank = summary.semesterRank,
                    totalRank = summary.totalRank,
                    earnedCredits = summary.earnedCredits,
                    attemptedCredits = summary.attemptedCredits
                )
            }
            .toMap()
    }

    // LMS API에서 학기 목록과 성적 요약 정보를 병렬로 새로 불러옵니다.
    suspend fun refreshGradeOverview(): Result<GradeData> = runCatching {
        coroutineScope {
            val semestersDeferred = async { getSemesters().getOrThrow() }
            val summariesDeferred = async { getGradeSummaries().getOrThrow() }
            GradeData(
                semesters = semestersDeferred.await(),
                summaries = summariesDeferred.await()
            )
        }
    }

    // LMS API에서 특정 학기의 상세 성적을 새로 불러옵니다.
    suspend fun refreshSemesterGrade(
        year: String,
        semester: Semester
    ): Result<GradeSemesterData> = runCatching {
        val gradeTable = withContext(Dispatchers.IO) {
            LmsApi.getGradeTable(year = year, semester = semester)
        }
        val courses = gradeTable.items.map { item ->
            GradeCourse(
                name = item.subjectName,
                professor = item.professor,
                credit = "${item.credits}학점",
                grade = item.gradePoint
            )
        }
        val totalCredits = gradeTable.items.fold(0.0) { total, item ->
            total + item.credits.toDouble()
        }

        GradeSemesterData(
            courses = courses,
            credits = totalCredits.toString(),
            courseCount = courses.size.toString()
        )
    }

    // LMS API 콜백을 코루틴 방식으로 감싸 학기 목록을 불러옵니다.
    @OptIn(ExperimentalTime::class)
    private suspend fun getTerms(): LmsTermsResult = suspendCancellableCoroutine { continuation ->
        LmsApi.getTerms { result ->
            if (!continuation.isActive) return@getTerms
            if (result.success) {
                continuation.resume(result)
            } else {
                continuation.resumeWithException(
                    IllegalStateException(result.errorMessage ?: "학기 목록을 불러오지 못했습니다.")
                )
            }
        }
    }
    // 연도와 학기명을 화면 탭에서 사용하는 학기 라벨("2024년 1학기")로 변환합니다.
    private fun buildSemesterLabel(year: String, semester: String): String {
       return "${year}년 ${semester}"
    }

    // LMS 학기 문자열을 성적 학기 데이터로 변환합니다.
    private fun parseGradeSemester(rawSemesterName: String): GradeSemester? {
        Regex("""(\d{4})(?:년|-)\s*([12]학기)""")
            .find(rawSemesterName)?.let { match ->
                val year = match.groupValues[1]
                val semester = when (match.groupValues[2]) {
                    "1학기" -> Semester.FIRST
                    "2학기" -> Semester.SECOND
                    else -> return null
                }
                return GradeSemester(
                    label = buildSemesterLabel(year = year, semester = semester.nameKor),
                    year = year,
                    semesterName = semester.name,
                    cacheKey = buildCacheKey(year = year, semester = semester)
                )
            }
        Regex("""(\d{4})-(하계|동계)계절제""")
            .find(rawSemesterName)?.let { match ->
                val year = match.groupValues[1]
                val semester = when (match.groupValues[2]) {
                    "하계" -> Semester.SUMMER
                    "동계" -> Semester.WINTER
                    else -> return null
                }

                return GradeSemester(
                    label = buildSemesterLabel(year = year, semester = semester.nameKor),
                    year = year,
                    semesterName = semester.name,
                    cacheKey = buildCacheKey(year = year, semester = semester)
                )
            }
        return null
    }

    // 연도와 LMS 학기 값을 성적 캐시 키로 변환합니다.
    private fun buildCacheKey(year: String, semester: Semester): SemesterKey =
        SemesterKey(year = year, semesterName = semester.name)
}
