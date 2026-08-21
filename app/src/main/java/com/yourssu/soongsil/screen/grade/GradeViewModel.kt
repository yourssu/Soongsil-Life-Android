package com.yourssu.soongsil.screen.grade

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.grade.GradeData
import com.yourssu.data.grade.GradeSemester
import com.yourssu.data.grade.GradeSemesterData
import com.yourssu.data.grade.GradeSemesterSummary
import com.yourssu.soongsil.data.GradeRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
import com.yourssu.soongsil.screen.grade.components.GradeRefreshStatus
import com.yourssu.soongsil.screen.grade.model.CourseItem
import com.yourssu.soongsil.screen.grade.model.GpaPoint
import com.yourssu.soongsil.screen.grade.model.SemesterTab
import com.yourssu.soongsil.screen.grade.model.getGradeStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chlwhdtn03.data.Lms.Semester
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SemesterGradeUiData(
    val courses: List<CourseItem> = emptyList(),
    val gpa: String = "-",
    val credits: String = "-",
    val courseCount: String = "-",
    val rank: String = "-"
)

data class GradeUiState(
    val semesterGradeData: SemesterGradeUiData = SemesterGradeUiData(),
    val semesters: List<SemesterTab> = emptyList(),
    val selectedSemesterIndex: Int = 0,
    val gpaPoints: List<GpaPoint> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val refreshStatus: GradeRefreshStatus = GradeRefreshStatus.HIDDEN,
    val refreshMessage: String = "",
    val refreshCurrentStep: Int? = null,
    val refreshTotalStep: Int? = null,
    val loginRequired: Boolean = false
)

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val gradeRepository: GradeRepository,
    private val lmsAuthRepository: LmsAuthRepository
) : ViewModel() {
    private companion object {
        const val REFRESH_SUCCESS_DURATION_MILLIS = 1_000L

        // 앱 프로세스에서 성적 상세 자동 갱신 팝업을 이미 보여줬는지 기억합니다.
        var hasShownAutoRefreshPopupInProcess = false
    }

    private val _uiState = MutableStateFlow(GradeUiState())
    val uiState = _uiState.asStateFlow()
    private var currentGradeData: GradeData = GradeData()
    private var isGradeRefreshing = false
    private var showRefreshPopup = true

    init {
        val showPopup = !hasShownAutoRefreshPopupInProcess
        hasShownAutoRefreshPopupInProcess = true
        loadGradeOverview(showPopup = showPopup)
    }

    // 성적 개요와 학기별 상세 성적을 불러옵니다.
    fun loadGradeOverview(showPopup: Boolean = true) {
        if (isGradeRefreshing) return
        isGradeRefreshing = true
        showRefreshPopup = showPopup

        viewModelScope.launch {
            try {
                updateRefreshLoading(
                    message = "성적 학기 정보를 확인하는 중",
                    currentStep = null,
                    totalStep = null
                )

                val cachedData = gradeRepository.getCachedData()
                val isFirstSemesterGradeLoad = cachedData?.grades.isNullOrEmpty()
                cachedData?.let { gradeData ->
                    updateGradeState(gradeData)
                }

                lmsAuthRepository.ensureActiveSession()
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(loginRequired = throwable.isLmsLoginRequired())
                        }
                        updateRefreshError(throwable.message)
                        hideRefreshPopupAfterDelay()
                        return@launch
                    }

                gradeRepository.refreshGradeOverview()
                    .onSuccess { overviewData ->
                        updateAndSaveGradeData(
                            overviewData.copy(grades = currentGradeData.grades)
                        )
                        refreshAllSemesterGrades(
                            semesters = overviewData.semesters,
                            showEverySemesterProgress = isFirstSemesterGradeLoad
                        )
                    }
                    .onFailure { throwable ->
                        updateRefreshError(throwable.message)
                        hideRefreshPopupAfterDelay()
                    }
            } finally {
                isGradeRefreshing = false
            }
        }
    }

    // 선택한 학기 인덱스에 맞춰 화면 상태를 갱신합니다.
    fun selectSemester(selectedIndex: Int) {
        updateGradeState(currentGradeData, selectedIndex)
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    // 전체 학기의 상세 성적 요청을 동시에 보내고 결과를 한 번에 반영합니다.
    private suspend fun refreshAllSemesterGrades(
        semesters: List<GradeSemester>,
        showEverySemesterProgress: Boolean
    ) {
        if (semesters.isEmpty()) {
            updateRefreshSuccess(message = "성적 정보를 불러왔어요")
            hideRefreshPopupAfterDelay()
            return
        }

        updateRefreshLoading(
            message = "모든 학기 성적 정보를 불러오는 중",
            currentStep = null,
            totalStep = null
        )

        val semesterResults = coroutineScope {
            semesters.map { semester ->
                async {
                    semester to gradeRepository.refreshSemesterGrade(
                        year = semester.year,
                        semester = Semester.valueOf(semester.semesterName)
                    )
                }
            }.awaitAll()
        }

        val refreshedGrades = semesterResults.mapNotNull { (semester, result) ->
            result.getOrNull()?.let { semesterData ->
                semester.cacheKey to semesterData
            }
        }.toMap()

        if (refreshedGrades.isNotEmpty()) {
            updateAndSaveGradeData(
                currentGradeData.copy(grades = currentGradeData.grades + refreshedGrades)
            )
        }

        val failure = semesterResults.firstNotNullOfOrNull { (_, result) ->
            result.exceptionOrNull()
        }
        if (failure != null) {
            updateRefreshError(failure.message)
            hideRefreshPopupAfterDelay()
            return
        }

        updateRefreshSuccess(
            message = if (showEverySemesterProgress) {
                "모든 성적 정보를 불러왔어요"
            } else {
                "최신 학기 성적 정보를 불러왔어요"
            }
        )
        hideRefreshPopupAfterDelay()
    }

    // 성적 데이터를 화면 상태와 캐시에 함께 반영합니다.
    private fun updateAndSaveGradeData(data: GradeData) {
        if (data == currentGradeData) return

        updateGradeState(data)
        viewModelScope.launch {
            gradeRepository.updateCacheData(data)
        }
    }

    // 성적 갱신 로딩 팝업 상태를 갱신합니다.
    private fun updateRefreshLoading(
        message: String,
        currentStep: Int?,
        totalStep: Int?
    ) {
        if (!showRefreshPopup) return

        _uiState.update {
            it.copy(
                refreshStatus = GradeRefreshStatus.LOADING,
                refreshMessage = message,
                refreshCurrentStep = currentStep,
                refreshTotalStep = totalStep,
                errorMessage = null
            )
        }
    }

    // 성적 갱신 성공 팝업 상태를 갱신합니다.
    private fun updateRefreshSuccess(message: String) {
        if (!showRefreshPopup) return

        _uiState.update {
            it.copy(
                refreshStatus = GradeRefreshStatus.SUCCESS,
                refreshMessage = message,
                refreshCurrentStep = null,
                refreshTotalStep = null,
                errorMessage = null
            )
        }
    }

    // 성적 갱신 실패 팝업 상태를 갱신합니다.
    private fun updateRefreshError(message: String?) {
        if (!showRefreshPopup) return

        _uiState.update {
            it.copy(
                refreshStatus = GradeRefreshStatus.ERROR,
                errorMessage = message,
                refreshCurrentStep = null,
                refreshTotalStep = null
            )
        }
    }

    // 성공 팝업을 잠시 표시한 뒤 숨깁니다.
    private suspend fun hideRefreshPopupAfterDelay() {
        if (!showRefreshPopup) return

        delay(REFRESH_SUCCESS_DURATION_MILLIS)
        _uiState.update {
            if (it.refreshStatus == GradeRefreshStatus.SUCCESS || it.refreshStatus == GradeRefreshStatus.ERROR) {
                it.copy(refreshStatus = GradeRefreshStatus.HIDDEN)
            } else {
                it
            }
        }
    }

    // 현재 성적 데이터를 기준으로 성적 화면 상태를 갱신합니다.
    private fun updateGradeState(
        data: GradeData,
        selectedIndex: Int = _uiState.value.selectedSemesterIndex
    ) {
        currentGradeData = data

        // 과목 수가 1개 이상인 학기만 화면에 노출합니다.
        val visibleSemesters = data.semesters.filter { semester ->
            val gradeData = data.grades[semester.cacheKey]
            gradeData != null && gradeData.courses.isNotEmpty()
        }

        if (visibleSemesters.isEmpty()) {
            _uiState.update {
                it.copy(
                    semesterGradeData = SemesterGradeUiData(),
                    semesters = emptyList(),
                    selectedSemesterIndex = 0,
                    gpaPoints = emptyList()
                )
            }
            return
        }

        val validSelectedIndex = selectedIndex
            .coerceAtLeast(0)
            .coerceAtMost(visibleSemesters.lastIndex)

        val semesters = visibleSemesters.mapIndexed { index, semester ->
            semester.toSemesterTab(isActive = index == validSelectedIndex)
        }
        val selectedSemester = visibleSemesters.getOrNull(validSelectedIndex)

        val selectedGradeData = selectedSemester
            ?.let { data.grades[it.cacheKey] }
            ?.toUiData(data.summaries[selectedSemester.cacheKey])
            ?: SemesterGradeUiData(
                gpa = data.summaries[selectedSemester?.cacheKey]?.gpa ?: "-",
                rank = data.summaries[selectedSemester?.cacheKey]?.rank ?: "-"
            )

        _uiState.update {
            it.copy(
                semesterGradeData = selectedGradeData,
                semesters = semesters,
                selectedSemesterIndex = validSelectedIndex,
                gpaPoints = data.toGpaPoints()
            )
        }
    }

    // 학기 상세 성적 데이터를 화면 표시용 데이터로 변환합니다.
    private fun GradeSemesterData.toUiData(summary: GradeSemesterSummary?): SemesterGradeUiData {
        val courses = courses.map { course ->
            val style = course.grade.getGradeStyle()
            CourseItem(
                name = course.name,
                professor = course.professor,
                credit = course.credit,
                grade = course.grade,
                gradeColor = style.gradeColor,
                gradeDarkColor = style.gradeDarkColor,
                badgeBgColor = style.badgeBgColor,
                badgeBgDarkColor = style.badgeBgDarkColor
            )
        }

        return SemesterGradeUiData(
            courses = courses,
            gpa = summary?.gpa ?: "-",
            credits = credits,
            courseCount = courseCount,
            rank = summary?.rank ?: "-"
        )
    }

    // 학기 데이터를 탭 표시용 데이터로 변환합니다.
    private fun GradeSemester.toSemesterTab(isActive: Boolean): SemesterTab =
        SemesterTab(
            label = label,
            isActive = isActive,
            year = year,
            semester = Semester.valueOf(semesterName)
        )

    // 성적 요약 데이터를 평점 추이 차트용 데이터로 변환합니다.
    private fun GradeData.toGpaPoints(): List<GpaPoint> {
        val points = semesters
            .filter { semester -> grades[semester.cacheKey]?.courses?.isNotEmpty() == true } // 과목이 있는 학기만 차트에 표시
            .sortedWith(compareBy({ it.year }, { Semester.valueOf(it.semesterName).ordinal }))
            .mapNotNull { semester ->
                val summary = summaries[semester.cacheKey] ?: return@mapNotNull null
                GpaPoint(
                    semester = semester.label,
                    gpa = summary.gpa.toFloatOrNull() ?: 0f
                )
            }

        return points.mapIndexed { index, point ->
            point.copy(isCurrent = index == points.lastIndex)
        }
    }
}
