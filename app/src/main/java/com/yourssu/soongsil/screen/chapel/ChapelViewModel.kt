package com.yourssu.soongsil.screen.chapel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.data.dashboard.DashboardChapelTerm
import com.yourssu.soongsil.data.DashboardRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chlwhdtn03.data.Lms.Semester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChapelUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedYear: String = "",
    val selectedSemester: String = "",
    val chapelData: DashboardChapelData? = null,
    val isSemesterLoading: Boolean = false,
    val semesterError: String? = null,
    val availableTerms: List<DashboardChapelTerm> = emptyList(),
    val isTermsLoading: Boolean = false,
    val termsError: String? = null,
    val loginRequired: Boolean = false,
)

@HiltViewModel
class ChapelViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val lmsAuthRepository: LmsAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapelUiState())
    val uiState: StateFlow<ChapelUiState> = _uiState.asStateFlow()
    private var semesterLoadJob: Job? = null
    private var termsLoadJob: Job? = null
    private var studentId: String = ""
    private var hasLoadedAvailableTerms = false

    init {
        loadInitialData()
    }

    // 채플 데이터를 다시 불러옵니다.
    fun retry() {
        loadInitialData()
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    // 드롭다운 등에서 학기를 선택했을 때 해당 학기의 채플 데이터를 조회합니다.
    // @param year 연도 (예: "2026")
    // @param semesterName 학기명 (예: "1학기", "2학기")
    fun selectSemester(year: String, semesterName: String) {
        val semester = when {
            semesterName.contains("1") -> Semester.FIRST
            semesterName.contains("2") -> Semester.SECOND
            else -> return
        }

        val normalizedSemesterName = if (semesterName.contains("1")) "1학기" else "2학기"
        Log.d("ChapelViewModel", "selectSemester 선택: year=$year, semesterName=$semesterName -> $normalizedSemesterName")

        _uiState.update {
            it.copy(
                selectedYear = year,
                selectedSemester = normalizedSemesterName,
                isSemesterLoading = true,
                semesterError = null,
            )
        }

        semesterLoadJob?.cancel()
        semesterLoadJob = viewModelScope.launch(Dispatchers.IO) {
            lmsAuthRepository.ensureActiveSession()
                .onFailure { throwable ->
                    Log.e("ChapelViewModel", "세션 활성화 실패", throwable)
                    _uiState.update {
                        it.copy(
                            isSemesterLoading = false,
                            semesterError = throwable.message,
                            loginRequired = throwable.isLmsLoginRequired(),
                        )
                    }
                    return@launch
                }

            dashboardRepository.getChapelData(year, semester)
                .onSuccess { chapelData ->
                    Log.d("ChapelViewModel", "채플 데이터 로드 성공: $year $normalizedSemesterName, seat=${chapelData.seat}, required=${chapelData.required}, attendances=${chapelData.weeklyAttendances.size}")
                    _uiState.update {
                        it.copy(
                            chapelData = chapelData.copy(
                                year = year,
                                semester = normalizedSemesterName
                            ),
                            selectedYear = year,
                            selectedSemester = normalizedSemesterName,
                            isSemesterLoading = false,
                            semesterError = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e("ChapelViewModel", "채플 데이터 로드 실패: $year $normalizedSemesterName", throwable)
                    _uiState.update {
                        it.copy(
                            chapelData = DashboardChapelData(
                                year = year,
                                semester = normalizedSemesterName,
                            ),
                            selectedYear = year,
                            selectedSemester = normalizedSemesterName,
                            isSemesterLoading = false,
                            semesterError = null,
                        )
                    }
                }
        }
    }

    // LMS getTerms()를 기반으로 선택 가능한 채플 학기 목록을 불러옵니다.
    fun loadAvailableChapelTerms() {
        if (hasLoadedAvailableTerms || _uiState.value.isTermsLoading) return

        termsLoadJob?.cancel()
        termsLoadJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isTermsLoading = true,
                    termsError = null,
                )
            }

            lmsAuthRepository.ensureActiveSession()
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isTermsLoading = false,
                            termsError = throwable.message,
                            loginRequired = throwable.isLmsLoginRequired(),
                        )
                    }
                    return@launch
                }

            dashboardRepository.getChapelTerms()
                .onSuccess { terms ->
                    hasLoadedAvailableTerms = true
                    _uiState.update { state ->
                        state.copy(
                            availableTerms = (state.availableTerms + terms)
                                .distinct()
                                .sortedWith(
                                    compareByDescending<DashboardChapelTerm> { it.year }
                                        .thenByDescending { it.semester },
                                ),
                            isTermsLoading = false,
                            termsError = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isTermsLoading = false,
                            termsError = "조회할 수 있는 채플 학기를 불러오지 못했습니다.",
                        )
                    }
                }
        }
    }

    // 초기 진입 시 캐시된 채플 데이터를 불러오고 학기 목록을 함께 로드합니다.
    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }

            val dashboardData = dashboardRepository.getCachedData()
            val chapelData = dashboardData?.chapel
            studentId = dashboardData?.studentId.orEmpty()
            val initialYear = chapelData?.year.orEmpty()
            val initialSemester = chapelData?.semester.orEmpty()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    selectedYear = initialYear,
                    selectedSemester = initialSemester,
                    chapelData = chapelData ?: DashboardChapelData(),
                    availableTerms = chapelData
                        ?.takeIf { it.year.isNotBlank() && it.semester.isNotBlank() }
                        ?.let {
                            listOf(
                                DashboardChapelTerm(
                                    year = it.year,
                                    semester = it.semester,
                                ),
                            )
                        }
                        .orEmpty(),
                )
            }

            loadAvailableChapelTerms()
        }
    }
}

