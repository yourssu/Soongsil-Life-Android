package com.yourssu.soongsil.screen.grade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.soongsil.screen.grade.model.CourseItem
import com.yourssu.soongsil.screen.grade.model.GpaPoint
import com.yourssu.soongsil.screen.grade.model.SemesterTab
import com.yourssu.soongsil.screen.grade.model.gradeStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.Semester
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

data class GradeUiData(
    val courses: List<CourseItem> = emptyList(),
    val gpa: String = "-",
    val credits: String = "-",
    val courseCount: String = "-",
    val rank: String = "-"
)

data class GradeUiState(
    val gradeData: GradeUiData = GradeUiData(),
    val semesters: List<SemesterTab> = emptyList(),
    val selectedSemesterIndex: Int = 0,
    val gpaPoints: List<GpaPoint> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class GradeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(GradeUiState())
    val uiState = _uiState.asStateFlow()
    private val summaryGpaCache = mutableMapOf<String, String>()
    private val summaryRankCache = mutableMapOf<String, String>()
    private val gradeCache = mutableMapOf<String, GradeUiData>()

    init {
        getTerms()
    }

    @OptIn(ExperimentalTime::class)
    fun getTerms() {
        if(_uiState.value.semesters.isNotEmpty()) return

        LmsApi.getTerms { result ->
            val semesters = result.terms
                .mapNotNull { it.name?.toSemesterTab() }
                .mapIndexed { index, tab -> tab.copy(isActive = index == 0) }

            _uiState.update {
                it.copy(
                    semesters = semesters,
                    selectedSemesterIndex = 0
                )
            }
            val latestSemester = semesters.firstOrNull() ?: return@getTerms
            viewModelScope.launch {
                loadSummaryGpa()
                loadGrade(latestSemester, applyToUi = true)
                semesters.drop(1).forEach {
                    loadGrade(it, applyToUi = false) }
            }
        }
    }


    fun selectSemester(selectedIndex: Int) {
        val selectedTab = _uiState.value.semesters.getOrNull(selectedIndex) ?: return

        _uiState.update {
            it.copy(
                selectedSemesterIndex = selectedIndex,
                semesters = _uiState.value.semesters.mapIndexed { index, tab ->
                    tab.copy(isActive = index == selectedIndex)
                }
            )
        }

        viewModelScope.launch {
            loadGrade(selectedTab, applyToUi = true)
        }
    }

    private suspend fun loadSummaryGpa() {
        if (summaryGpaCache.isNotEmpty()) return

        runCatching {
            withContext(Dispatchers.IO) {
                LmsApi.getSemesterGradeSummaryTable()
            }
        }.onSuccess { summaryTable ->
            summaryTable.items.forEach { summary ->
                summaryGpaCache["${summary.year}-${summary.semester?.name}"] = summary.gpa
                summaryRankCache["${summary.year}-${summary.semester?.name}"] = summary.semesterRank
            }
            val gpaPoints = summaryTable.items
                .sortedWith(compareBy({ it.year }, { it.semester?.ordinal }))
                .mapIndexed { index, summary ->
                    GpaPoint(
                        semester = buildSemesterLabel(
                            year = summary.year,
                            semester = summary.semester?.nameKor.orEmpty()
                        ),
                        gpa = summary.gpa.toFloatOrNull() ?: 0f,
                        isCurrent = index == summaryTable.items.lastIndex
                    )
                }

            _uiState.value = _uiState.value.copy(
                gpaPoints = gpaPoints
            )
        }
    }


    private suspend fun loadGrade(tab: SemesterTab, applyToUi: Boolean) {
        val key = "${tab.year}-${tab.semester.name}"
        val cached = gradeCache[key]
        if (cached != null) {
            if (applyToUi) updateGradeData(cached)
            return
        }

        if (applyToUi) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        }

        runCatching {
            withContext(Dispatchers.IO) {
                LmsApi.getGradeTable(year = tab.year, semester = tab.semester)
            }
        }.onSuccess { gradeTable ->
            val courses = gradeTable.items.map { item ->
                val gradePoint = item.gradePoint
                val style = gradeStyle(gradePoint)

                CourseItem(
                    name = item.subjectName,
                    professor = item.professor,
                    credit = "${item.credits}학점",
                    grade = gradePoint,
                    dotColor = style.first,
                    gradeColor = style.second,
                    badgeBgColor = style.third
                )
            }
            val totalCredits = gradeTable.items.fold(0.0) { total, item ->
                total + item.credits.toDouble()
            }
            val gradeData = GradeUiData(
                courses = courses,
                gpa = summaryGpaCache[key] ?: "-",
                credits = "${totalCredits}학점",
                courseCount = courses.size.toString(),
                rank = summaryRankCache[key] ?: "-"
            )

            gradeCache[key] = gradeData
            if (applyToUi) updateGradeData(gradeData)
        }.onFailure { throwable ->
            if (applyToUi) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message
                )
            }
        }
    }

    private fun updateGradeData(data: GradeUiData) {
        _uiState.value = _uiState.value.copy(
            gradeData = data,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun buildSemesterLabel(year: String, semester: String): String {
         return "${year.takeLast(2)}-${semester.semesterLabel()}"
    }

    private fun String.semesterLabel(): String = when {
        startsWith("1") -> "1"
        startsWith("2") -> "2"
        startsWith("여름") -> "여름"
        startsWith("겨울") -> "겨울"
        else -> removeSuffix("학기")
    }

    private fun String.toSemesterTab(): SemesterTab? {
        Regex("""(\d{4})(?:년|-)\s*([12])학기""").find(this)?.let { match ->
            val year = match.groupValues[1]
            val semester = when (match.groupValues[2]) {
                "1" -> Semester.FIRST
                "2" -> Semester.SECOND
                else -> return null
            }

            return SemesterTab(
                label = buildSemesterLabel(
                    year = year,
                    semester = semester.nameKor
                ),
                year = year,
                semester = semester
            )
        }

        Regex("""(\d{4})-(하계|동계)계절제""").find(this)?.let { match ->
            val year = match.groupValues[1]
            val semester = when (match.groupValues[2]) {
                "하계" -> Semester.SUMMER
                "동계" -> Semester.WINTER
                else -> return null
            }
            return SemesterTab(
                label = buildSemesterLabel(
                    year = year,
                    semester = semester.nameKor
                ),
                year = year,
                semester = semester
            )
        }

        return null
    }

}


