package com.yourssu.soongsil.screen.grade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.grade.GradeData
import com.yourssu.data.grade.GradeSemester
import com.yourssu.data.grade.GradeSemesterData
import com.yourssu.data.grade.GradeSemesterSummary
import com.yourssu.soongsil.data.GradeRepository
import com.yourssu.soongsil.screen.grade.components.GradeRefreshStatus
import com.yourssu.soongsil.screen.grade.model.CourseItem
import com.yourssu.soongsil.screen.grade.model.GpaPoint
import com.yourssu.soongsil.screen.grade.model.SemesterTab
import com.yourssu.soongsil.screen.grade.model.getGradeStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chlwhdtn03.data.Lms.Semester
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val refreshTotalStep: Int? = null
)

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val gradeRepository: GradeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GradeUiState())
    val uiState = _uiState.asStateFlow()
    private var currentGradeData: GradeData = GradeData()

    init {
        loadGradeOverview()
    }

    // 성적 개요와 학기별 상세 성적을 불러옵니다.
    fun loadGradeOverview() {
        if (_uiState.value.refreshStatus == GradeRefreshStatus.LOADING) return

        viewModelScope.launch {
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
        }
    }

    // 선택한 학기 인덱스에 맞춰 화면 상태를 갱신합니다.
    fun selectSemester(selectedIndex: Int) {
        updateGradeState(currentGradeData, selectedIndex)
    }

    // 전체 학기의 상세 성적을 순서대로 갱신합니다.
    private suspend fun refreshAllSemesterGrades(
        semesters: List<GradeSemester>,
        showEverySemesterProgress: Boolean
    ) {
        if (semesters.isEmpty()) {
            updateRefreshSuccess(message = "성적 정보를 불러왔어요")
            hideRefreshPopupAfterDelay()
            return
        }

        val latestSemester = semesters.first()
        if (!showEverySemesterProgress) {
            updateRefreshLoading(
                message = latestSemester.toRefreshMessage(),
                currentStep = 1,
                totalStep = 1
            )

            refreshSemesterGrade(latestSemester)
                .onFailure { throwable ->
                    updateRefreshError(throwable.message)
                    hideRefreshPopupAfterDelay()
                    return
                }

            updateRefreshSuccess(message = "최신 학기 성적 정보를 불러왔어요")
            hideRefreshPopupAfterDelay()

            semesters.drop(1).forEach { semester ->
                refreshSemesterGrade(semester)
            }
            return
        }

        semesters.forEachIndexed { index, semester ->
            updateRefreshLoading(
                message = semester.toRefreshMessage(),
                currentStep = index + 1,
                totalStep = semesters.size
            )

            refreshSemesterGrade(semester)
                .onFailure { throwable ->
                    updateRefreshError(throwable.message)
                    hideRefreshPopupAfterDelay()
                    return
                }
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

    // 특정 학기의 상세 성적을 API에서 다시 불러옵니다.
    private suspend fun refreshSemesterGrade(semester: GradeSemester): Result<Unit> {
        return gradeRepository.refreshSemesterGrade(
            year = semester.year,
            semester = Semester.valueOf(semester.semesterName)
        ).map { semesterData ->
            val updatedGrades = currentGradeData.grades.toMutableMap().apply {
                put(semester.cacheKey, semesterData)
            }
            val updatedData = currentGradeData.copy(grades = updatedGrades)
            updateAndSaveGradeData(updatedData)
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(errorMessage = throwable.message)
            }
        }
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
        val validSelectedIndex = selectedIndex
            .coerceAtLeast(0)
            .coerceAtMost(data.semesters.lastIndex)

        val semesters = data.semesters.mapIndexed { index, semester ->
            semester.toSemesterTab(isActive = index == validSelectedIndex)
        }
        val selectedSemester = data.semesters.getOrNull(validSelectedIndex)

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

    // 학기 데이터를 성적 갱신 팝업 문구로 변환합니다.
    private fun GradeSemester.toRefreshMessage(): String {
        val semester = Semester.valueOf(semesterName).nameKor
        return "${year}학년도 $semester 성적 정보를 불러오는 중"
    }

    private companion object {
        const val REFRESH_SUCCESS_DURATION_MILLIS = 750L
    }
}
