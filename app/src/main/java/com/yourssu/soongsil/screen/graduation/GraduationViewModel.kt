package com.yourssu.soongsil.screen.graduation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.graduation.GraduationData
import com.yourssu.soongsil.data.GraduationRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
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
        loadGraduationData()
    }

    fun retry() {
        loadGraduationData()
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    private fun loadGraduationData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch(Dispatchers.IO) {
            lmsAuthRepository.ensureActiveSession()
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message,
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
                            error = throwable.message ?: "졸업사정표를 불러오지 못했습니다."
                        )
                    }
                }
        }
    }
}
