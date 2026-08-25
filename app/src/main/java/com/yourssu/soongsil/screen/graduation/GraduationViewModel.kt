package com.yourssu.soongsil.screen.graduation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.graduation.GraduationData
import com.yourssu.soongsil.data.GraduationRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
import com.yourssu.soongsil.data.toUserFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GraduationViewModel @Inject constructor(
    private val graduationRepository: GraduationRepository,
    private val lmsAuthRepository: LmsAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GraduationUiState())
    val uiState: StateFlow<GraduationUiState> = _uiState.asStateFlow()

    data class GraduationUiState(
        val isLoading: Boolean = true,
        val error: String? = null,
        val graduationData: GraduationData? = null,
        val loginRequired: Boolean = false
    )

    init {
        loadInitialGraduationData()
    }

    fun retry() {
        loadGraduationData()
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    // 초기 진입 시 로컬 캐시를 먼저 불러오고, 백그라운드에서 최신 데이터를 조회합니다.
    private fun loadInitialGraduationData() {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedData = graduationRepository.getCachedData()
            if (cachedData != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        graduationData = cachedData,
                        error = null
                    )
                }
            }
            loadGraduationData()
        }
    }

    // LMS 서버에서 최신 졸업사정표 데이터를 불러와 UI 및 캐시를 갱신합니다.
    private fun loadGraduationData() {
        val hasExistingData = _uiState.value.graduationData != null
        if (!hasExistingData) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            lmsAuthRepository.ensureActiveSession()
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.toUserFriendlyMessage(),
                            loginRequired = throwable.isLmsLoginRequired()
                        )
                    }
                    return@launch
                }

            graduationRepository.getGraduationData()
                .onSuccess { graduationData ->
                    _uiState.update {
                        it.copy(isLoading = false, error = null, graduationData = graduationData)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.toUserFriendlyMessage()
                        )
                    }
                }
        }
    }
}
