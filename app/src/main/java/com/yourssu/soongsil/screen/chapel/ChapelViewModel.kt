package com.yourssu.soongsil.screen.chapel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.soongsil.data.DashboardRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChapelUiState(
    val isLoading: Boolean = true,
    val loginRequired: Boolean = false,
    val error: String? = null,
    val chapelData: DashboardChapelData? = null,
)

@HiltViewModel
class ChapelViewModel @Inject constructor(
    private val lmsAuthRepository: LmsAuthRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapelUiState())
    val uiState: StateFlow<ChapelUiState> = _uiState.asStateFlow()

    init {
        loadChapelData()
    }

    fun retry() {
        loadChapelData()
    }

    private fun loadChapelData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }

            val credentials = lmsAuthRepository.getSavedCredentials()

            if (credentials == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginRequired = true,
                    )
                }
                return@launch
            }

            if (!lmsAuthRepository.hasActiveSession()) {
                val loginResult = lmsAuthRepository.login(
                    credentials.studentId,
                    credentials.password,
                )

                if (loginResult.isFailure) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginRequired = true,
                            error = loginResult.exceptionOrNull()?.message,
                        )
                    }
                    return@launch
                }
            }

            dashboardRepository.getChapelData()
                .onSuccess { chapelData ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginRequired = false,
                            error = null,
                            chapelData = chapelData,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message
                                ?: "채플 정보를 불러오지 못했습니다.",
                        )
                    }
                }
        }
    }
}
