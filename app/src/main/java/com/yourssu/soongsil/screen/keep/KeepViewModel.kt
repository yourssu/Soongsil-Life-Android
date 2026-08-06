package com.yourssu.soongsil.screen.keep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.keep.KeepCourse
import com.yourssu.data.keep.KeepData
import com.yourssu.soongsil.data.KeepRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.screen.plan.PlanPdfData
import com.yourssu.soongsil.screen.plan.PlanPdfUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KeepUiState(
    val data: KeepData? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val loginRequired: Boolean = false,
    val planPdfState: PlanPdfUiState = PlanPdfUiState()
)

@HiltViewModel
class KeepViewModel @Inject constructor(
    private val keepRepository: KeepRepository,
    private val lmsAuthRepository: LmsAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(KeepUiState())
    val uiState: StateFlow<KeepUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var planJob: Job? = null

    init {
        loadData()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            refreshFromNetwork()
        }
    }

    fun retry() {
        if (refreshJob?.isActive == true) return

        _uiState.update { it.copy(isLoading = it.data == null, errorMessage = null) }
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            refreshFromNetwork()
        }
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    fun loadPlan(course: KeepCourse) {
        if (planJob?.isActive == true) return

        _uiState.update {
            it.copy(
                planPdfState = PlanPdfUiState(
                    isLoading = true,
                    loadingTitle = course.subjectName
                )
            )
        }
        planJob = viewModelScope.launch(Dispatchers.IO) {
            ensureLoggedIn()
                .onFailure { throwable ->
                    if (throwable is CancellationException) return@launch
                    _uiState.update {
                        it.copy(
                            planPdfState = PlanPdfUiState(
                                errorMessage = throwable.message ?: "로그인이 필요합니다."
                            )
                        )
                    }
                    return@launch
                }

            keepRepository.loadPlan(course)
                .onSuccess { bytes ->
                    if (!isActive) return@onSuccess
                    _uiState.update {
                        it.copy(
                            planPdfState = PlanPdfUiState(
                                pdf = PlanPdfData(
                                    title = course.subjectName,
                                    bytes = bytes
                                )
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) return@onFailure
                    _uiState.update {
                        it.copy(
                            planPdfState = PlanPdfUiState(
                                errorMessage = throwable.message
                                    ?: "강의계획서를 불러오지 못했습니다."
                            )
                        )
                    }
                }
        }
    }

    fun cancelPlanLoading() {
        planJob?.cancel()
        planJob = null
        _uiState.update { it.copy(planPdfState = PlanPdfUiState()) }
    }

    fun closePlan() {
        _uiState.update { it.copy(planPdfState = PlanPdfUiState()) }
    }

    fun dismissPlanError() {
        _uiState.update { it.copy(planPdfState = PlanPdfUiState()) }
    }

    private fun loadData() {
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            val cachedData = keepRepository.getCachedData()
            _uiState.update {
                it.copy(
                    data = cachedData,
                    isLoading = cachedData == null
                )
            }
            refreshFromNetwork()
        }
    }

    private suspend fun refreshFromNetwork() {
        ensureLoggedIn()
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = throwable.message,
                        loginRequired = true
                    )
                }
                return
            }

        keepRepository.refreshData()
            .onSuccess { keepData ->
                _uiState.update {
                    it.copy(
                        data = keepData,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = throwable.message ?: "장바구니 정보를 불러오지 못했습니다."
                    )
                }
            }
    }

    private suspend fun ensureLoggedIn(): Result<Unit> {
        if (lmsAuthRepository.hasActiveSession()) return Result.success(Unit)

        val credentials = lmsAuthRepository.getSavedCredentials()
            ?: return Result.failure(IllegalStateException("로그인이 필요합니다."))
        return lmsAuthRepository.login(credentials.studentId, credentials.password)
    }
}
