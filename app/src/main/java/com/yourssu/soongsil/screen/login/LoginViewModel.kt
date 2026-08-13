package com.yourssu.soongsil.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.OnBoardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val lmsAuthRepository: LmsAuthRepository,
    private val onBoardingRepository: OnBoardingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(studentId: String, password: String) {
        val trimmedStudentId = studentId.trim()
        if (trimmedStudentId.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "학번과 비밀번호를 입력해주세요.") }
            return
        }
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            lmsAuthRepository.loginAndSaveCredentials(trimmedStudentId, password)
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message)
                    }
                }
                .onSuccess {
                    val onboardingRequired = !onBoardingRepository.hasAgreedToTerms()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            isOnboardingRequired = onboardingRequired
                        )
                    }
                }
        }
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }

    data class LoginUiState(
        val isLoading: Boolean = false,
        val isLoginSuccessful: Boolean = false,
        val isOnboardingRequired: Boolean = false,
        val error: String? = null
    )

}
