package com.yourssu.soongsil.screen.chapel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.dashboard.ChapelCacheData
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.data.dashboard.DashboardChapelTerm
import com.yourssu.soongsil.data.DashboardRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
import com.yourssu.soongsil.data.toUserFriendlyMessage
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSemesterLoading: Boolean = false,
    val isTermsLoading: Boolean = false,
    val termsError: String? = null,
    val semesterError: String? = null,
    val selectedYear: String = "",
    val selectedSemester: String = "",
    val chapelData: DashboardChapelData? = null,
    val availableTerms: List<DashboardChapelTerm> = emptyList(),
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

    // 현재 선택된 학기의 채플 데이터를 당겨서 새로고침합니다.
    fun refreshCurrentSemester() {
        val year = _uiState.value.selectedYear.ifBlank {
            _uiState.value.chapelData?.year.orEmpty()
        }
        val semester = _uiState.value.selectedSemester.ifBlank {
            _uiState.value.chapelData?.semester.orEmpty()
        }

        if (year.isNotBlank() && semester.isNotBlank()) {
            selectSemester(year, semester)
        } else {
            loadInitialData()
        }
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    // 에러 알림 팝업을 닫습니다.
    fun dismissError() {
        _uiState.update {
            it.copy(
                error = null,
                semesterError = null,
                termsError = null
            )
        }
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
                            semesterError = throwable.toUserFriendlyMessage(),
                            loginRequired = throwable.isLmsLoginRequired(),
                        )
                    }
                    return@launch
                }

            dashboardRepository.getChapelData(year, semester)
                .onSuccess { chapelData ->
                    Log.d("ChapelViewModel", "채플 데이터 로드 성공: $year $normalizedSemesterName, seat=${chapelData.seat}, required=${chapelData.required}, attendances=${chapelData.weeklyAttendances.size}")
                    val updatedChapelData = chapelData.copy(
                        year = year,
                        semester = normalizedSemesterName
                    )
                    _uiState.update { state ->
                        state.copy(
                            chapelData = updatedChapelData,
                            selectedYear = year,
                            selectedSemester = normalizedSemesterName,
                            isSemesterLoading = false,
                            semesterError = null,
                        )
                    }
                    // 최신 데이터 로드 성공 시 로컬 캐시를 갱신합니다.
                    dashboardRepository.updateChapelCacheData(
                        ChapelCacheData(
                            chapelData = updatedChapelData,
                            availableTerms = _uiState.value.availableTerms
                        )
                    )
                }
                .onFailure { throwable ->
                    Log.e("ChapelViewModel", "채플 데이터 로드 실패: $year $normalizedSemesterName", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSemesterLoading = false,
                            error = if (it.chapelData == null) throwable.toUserFriendlyMessage() else null,
                            semesterError = throwable.toUserFriendlyMessage(),
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
                            isLoading = false,
                            isTermsLoading = false,
                            error = if (it.chapelData == null) throwable.toUserFriendlyMessage() else null,
                            termsError = throwable.toUserFriendlyMessage(),
                            loginRequired = throwable.isLmsLoginRequired(),
                        )
                    }
                    return@launch
                }

            dashboardRepository.getChapelTerms()
                .onSuccess { terms ->
                    hasLoadedAvailableTerms = true
                    val combinedTerms = (_uiState.value.availableTerms + terms)
                        .distinct()
                        .sortedWith(
                            compareByDescending<DashboardChapelTerm> { it.year }
                                .thenByDescending { it.semester },
                        )

                    _uiState.update { state ->
                        state.copy(
                            availableTerms = combinedTerms,
                            isTermsLoading = false,
                            termsError = null,
                        )
                    }

                    // 최신 학기 목록을 캐시에 동기화
                    _uiState.value.chapelData?.let { chapel ->
                        dashboardRepository.updateChapelCacheData(
                            ChapelCacheData(
                                chapelData = chapel,
                                availableTerms = combinedTerms
                            )
                        )
                    }

                    // 현재 채플 데이터가 유효하지 않은 경우 최신 학기 조회 시도
                    val currentChapel = _uiState.value.chapelData
                    val isCurrentDataEmpty = currentChapel == null ||
                            (currentChapel.seat.isBlank() && currentChapel.weeklyAttendances.isEmpty() && currentChapel.required == 0)

                    val topTerm = combinedTerms.firstOrNull()
                    if (topTerm != null && isCurrentDataEmpty) {
                        selectSemester(topTerm.year, topTerm.semester)
                    } else if (isCurrentDataEmpty && combinedTerms.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isTermsLoading = false,
                            error = if (it.chapelData == null) throwable.toUserFriendlyMessage() else null,
                            termsError = throwable.toUserFriendlyMessage(),
                        )
                    }
                }
        }
    }

    // 초기 진입 시 DataStore에 캐시된 채플 데이터를 먼저 불러오고, 백그라운드에서 학기 목록과 데이터를 새로고침합니다.
    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val cachedChapelCache = dashboardRepository.getCachedChapelData()
            val dashboardData = dashboardRepository.getCachedData()

            val initialChapelData = cachedChapelCache?.chapelData?.takeIf {
                it.seat.isNotBlank() || it.weeklyAttendances.isNotEmpty() || it.required > 0
            } ?: dashboardData?.chapel?.takeIf {
                it.seat.isNotBlank() || it.weeklyAttendances.isNotEmpty() || it.required > 0
            }

            val initialTerms = cachedChapelCache?.availableTerms?.takeIf { it.isNotEmpty() }
                ?: initialChapelData?.takeIf { it.year.isNotBlank() && it.semester.isNotBlank() }
                    ?.let { listOf(DashboardChapelTerm(it.year, it.semester)) }
                ?: emptyList()

            if (initialChapelData != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        selectedYear = initialChapelData.year,
                        selectedSemester = initialChapelData.semester,
                        chapelData = initialChapelData,
                        availableTerms = initialTerms,
                    )
                }
            }

            loadAvailableChapelTerms()
        }
    }
}

