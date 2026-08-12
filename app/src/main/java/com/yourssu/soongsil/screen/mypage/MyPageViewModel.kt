package com.yourssu.soongsil.screen.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.soongsil.data.DashboardRepository
import com.yourssu.soongsil.data.KeepRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val lmsAuthRepository: LmsAuthRepository,
    private val dashboardRepository: DashboardRepository,
    private val keepRepository: KeepRepository
) : ViewModel() {
    private val _gradeNotificationEnabled = MutableStateFlow(true)
    val gradeNotificationEnabled: StateFlow<Boolean> = _gradeNotificationEnabled.asStateFlow()

    private val _logoutCompleted = MutableStateFlow(false)
    val logoutCompleted: StateFlow<Boolean> = _logoutCompleted.asStateFlow()

    fun setGradeNotificationEnabled(enabled: Boolean) {
        _gradeNotificationEnabled.value = enabled
    }

    fun logout() {
        viewModelScope.launch {
            if (dashboardRepository.clearCachedData().isFailure) return@launch
            if (keepRepository.clearCachedData().isFailure) return@launch

            lmsAuthRepository.logout()
                .onSuccess { _logoutCompleted.value = true }
        }
    }

    fun onLogoutNavigationHandled() {
        _logoutCompleted.value = false
    }
}
