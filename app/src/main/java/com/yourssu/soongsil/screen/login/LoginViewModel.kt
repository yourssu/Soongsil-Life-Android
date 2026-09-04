package com.yourssu.soongsil.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.OnBoardingRepository
import com.yourssu.soongsil.data.toUserFriendlyMessage
import com.yourssu.soongsil.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 로그인 화면의 비즈니스 로직과 상태를 관리하는 ViewModel입니다.
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val lmsAuthRepository: LmsAuthRepository,
    private val onBoardingRepository: OnBoardingRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 사용자 입력을 검증하고 유세인트 로그인을 시도합니다.
    // @param studentId 입력받은 학번 문자열입니다.
    // @param password 입력받은 비밀번호 문자열입니다.
    fun login(studentId: String, password: String) {
        val trimmedStudentId = studentId.trim()
        if (trimmedStudentId.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "학번과 비밀번호를 입력해주세요.") }
            return
        }
        if (_uiState.value.isLoading) return

        // 인터넷 연결이 끊겨있는 경우 스낵바 알림 상태를 즉시 설정합니다.
        if (!networkMonitor.isCurrentlyConnected) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isNetworkOffline = true,
                    error = "인터넷에 연결되어 있지 않아 로그인할 수 없습니다."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isNetworkOffline = false) }

            lmsAuthRepository.loginAndSaveCredentials(trimmedStudentId, password)
                .onFailure { throwable ->
                    val userFriendlyMessage = throwable.toUserFriendlyMessage()
                    val isOffline = !networkMonitor.isCurrentlyConnected ||
                            userFriendlyMessage.contains("인터넷")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = userFriendlyMessage,
                            isNetworkOffline = isOffline
                        )
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

    // 로그인 완료 후 화면 전환 처리가 끝났음을 알립니다.
    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }

    // 네트워크 오프라인 스낵바 처리가 완료되었음을 알립니다.
    fun onNetworkOfflineHandled() {
        _uiState.update { it.copy(isNetworkOffline = false) }
    }

    // 로그인 화면의 UI 상태를 정의하는 데이터 클래스입니다.
    data class LoginUiState(
        val isLoading: Boolean = false,
        val isLoginSuccessful: Boolean = false,
        val isOnboardingRequired: Boolean = false,
        val isNetworkOffline: Boolean = false,
        val error: String? = null
    )
}
