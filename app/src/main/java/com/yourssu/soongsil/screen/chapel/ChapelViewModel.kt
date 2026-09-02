package com.yourssu.soongsil.screen.chapel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.soongsil.data.DashboardRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
import com.yourssu.soongsil.data.toUserFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 채플 화면의 UI 상태를 나타내는 데이터 클래스입니다.
data class ChapelUiState(
    val isLoading: Boolean = false,
    val isSemesterLoading: Boolean = false,
    val error: String? = null,
    val chapelData: DashboardChapelData? = null,
    val loginRequired: Boolean = false,
)

@HiltViewModel
class ChapelViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val lmsAuthRepository: LmsAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapelUiState())
    val uiState: StateFlow<ChapelUiState> = _uiState.asStateFlow()
    private var fetchJob: Job? = null

    init {
        loadInitialData()
    }

    // 채플 데이터를 다시 불러옵니다.
    fun retry() {
        fetchLatestChapel(isPullToRefresh = false)
    }

    // 당겨서 새로고침(Pull to Refresh) 시 채플 정보만 단독으로 새로 불러옵니다.
    fun refreshCurrentSemester() {
        fetchLatestChapel(isPullToRefresh = true)
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    // 에러 안내를 닫습니다.
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    // 초기 진입 시 로컬 캐시를 즉시 표시하고, 백그라운드에서 최신 채플 데이터를 갱신합니다.
    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            val dashboardData = dashboardRepository.getCachedData()
            val cachedChapelCache = dashboardRepository.getCachedChapelData()

            val initialChapelData = dashboardData?.chapel?.takeIf {
                it.seat.isNotBlank() || it.weeklyAttendances.isNotEmpty() || it.required > 0
            } ?: cachedChapelCache?.chapelData?.takeIf {
                it.seat.isNotBlank() || it.weeklyAttendances.isNotEmpty() || it.required > 0
            }

            if (initialChapelData != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        chapelData = initialChapelData,
                        error = null,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            // 백그라운드에서 최신 채플 데이터 동기화
            fetchLatestChapel(isPullToRefresh = false)
        }
    }

    // 최신 학기 채플 정보만 단독으로 유세인트 서버에서 가져옵니다.
    // @param isPullToRefresh 당겨서 새로고침 여부 (스피너 표시 제어)
    private fun fetchLatestChapel(isPullToRefresh: Boolean) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            if (isPullToRefresh) {
                _uiState.update { it.copy(isSemesterLoading = true, error = null) }
            } else if (_uiState.value.chapelData == null) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            lmsAuthRepository.ensureActiveSession()
                .onFailure { throwable ->
                    Log.e("ChapelViewModel", "세션 활성화 실패", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSemesterLoading = false,
                            error = throwable.toUserFriendlyMessage(),
                            loginRequired = throwable.isLmsLoginRequired(),
                        )
                    }
                    return@launch
                }

            dashboardRepository.getLatestChapelData()
                .onSuccess { chapelData ->
                    Log.d(
                        "ChapelViewModel",
                        "최신 채플 데이터 로드 성공: ${chapelData.year} ${chapelData.semester}, seat=${chapelData.seat}, attended=${chapelData.attended}/${chapelData.required}"
                    )
                    _uiState.update { state ->
                        state.copy(
                            chapelData = chapelData,
                            isLoading = false,
                            isSemesterLoading = false,
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e("ChapelViewModel", "최신 채플 데이터 로드 실패", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSemesterLoading = false,
                            error = throwable.toUserFriendlyMessage(),
                        )
                    }
                }
        }
    }
}

