package com.yourssu.soongsil.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.dashboard.DashboardData
import com.yourssu.data.dashboard.DashboardRefreshStep
import com.yourssu.soongsil.data.DashboardRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val lmsAuthRepository: LmsAuthRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    data class DashboardUiState(
        val isLoading: Boolean = true,
        val loginRequired: Boolean = false,
        val error: String? = null,
        val dashboardData: DashboardData? = null,
        val refreshStatus: DashboardRefreshStatus = DashboardRefreshStatus.LOADING,
        val refreshStep: DashboardRefreshStep = DashboardRefreshStep.CONNECTING,
        val isPullRefreshing: Boolean = false
    )

    private var studentId: String? = null

    init {
        loginWithSavedCredentials()
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    private fun loginWithSavedCredentials() {
        viewModelScope.launch(Dispatchers.IO) {
            val credentials = lmsAuthRepository.getSavedCredentials()
            if (credentials == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginRequired = true,
                        refreshStatus = DashboardRefreshStatus.HIDDEN
                    )
                }
                return@launch
            }
            studentId = credentials.studentId

            dashboardRepository.getCachedData()
                ?.takeIf { it.studentId == credentials.studentId }
                ?.let { cachedData ->
                    _uiState.update { it.copy(dashboardData = cachedData) }
                }

            if (lmsAuthRepository.hasActiveSession()) {
                refreshDashboardData(credentials.studentId)
                return@launch
            }

            lmsAuthRepository.login(credentials.studentId, credentials.password)
                .onSuccess {
                    refreshDashboardData(credentials.studentId)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginRequired = true,
                            error = throwable.message,
                            refreshStatus = DashboardRefreshStatus.HIDDEN
                        )
                    }
                }
        }
    }

    fun retryRefresh() {
        requestRefresh(isPullToRefresh = false)
    }

    fun pullToRefresh() {
        requestRefresh(isPullToRefresh = true)
    }

    private fun requestRefresh(isPullToRefresh: Boolean) {
        if (_uiState.value.refreshStatus == DashboardRefreshStatus.LOADING) return
        val currentStudentId = studentId ?: return
        if (isPullToRefresh) {
            _uiState.update { it.copy(isPullRefreshing = true) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            refreshDashboardData(currentStudentId)
        }
    }

    private suspend fun refreshDashboardData(studentId: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                refreshStatus = DashboardRefreshStatus.LOADING,
                refreshStep = DashboardRefreshStep.STUDENT_INFO
            )
        }

        val refreshResult = dashboardRepository.refreshData(studentId) { step ->
            _uiState.update { it.copy(refreshStep = step) }
        }
        refreshResult
            .onSuccess { dashboardData ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        dashboardData = dashboardData,
                        refreshStatus = DashboardRefreshStatus.SUCCESS,
                        isPullRefreshing = false
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message,
                        refreshStatus = DashboardRefreshStatus.ERROR,
                        isPullRefreshing = false
                    )
                }
            }

        if (refreshResult.isSuccess) {
            delay(REFRESH_SUCCESS_DURATION_MILLIS)
            _uiState.update {
                if (it.refreshStatus == DashboardRefreshStatus.SUCCESS) {
                    it.copy(refreshStatus = DashboardRefreshStatus.HIDDEN)
                } else {
                    it
                }
            }
        }
    }

    private companion object {
        const val REFRESH_SUCCESS_DURATION_MILLIS = 2_000L
    }
}

enum class DashboardRefreshStatus {
    HIDDEN,
    LOADING,
    SUCCESS,
    ERROR
}
