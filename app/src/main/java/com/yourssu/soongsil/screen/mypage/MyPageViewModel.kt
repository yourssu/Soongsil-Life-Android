package com.yourssu.soongsil.screen.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.soongsil.data.CourseCatalogRepository
import com.yourssu.soongsil.data.DashboardRepository
import com.yourssu.soongsil.data.GradeRepository
import com.yourssu.soongsil.data.GraduationRepository
import com.yourssu.soongsil.data.KeepRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.OnBoardingRepository
import com.yourssu.soongsil.data.TimetableRepository
import com.yourssu.soongsil.data.TuitionScholarshipCache
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
    private val gradeRepository: GradeRepository,
    private val tuitionScholarshipCache: TuitionScholarshipCache,
    private val keepRepository: KeepRepository,
    private val courseCatalogRepository: CourseCatalogRepository,
    private val onBoardingRepository: OnBoardingRepository,
    private val timetableRepository: TimetableRepository,
    private val graduationRepository: GraduationRepository,
) : ViewModel() {
    private val _gradeNotificationEnabled = MutableStateFlow(true)
    val gradeNotificationEnabled: StateFlow<Boolean> = _gradeNotificationEnabled.asStateFlow()

    private val _logoutCompleted = MutableStateFlow(false)
    val logoutCompleted: StateFlow<Boolean> = _logoutCompleted.asStateFlow()

    fun setGradeNotificationEnabled(enabled: Boolean) {
        _gradeNotificationEnabled.value = enabled
    }

    // 로그아웃 시 사용자의 모든 로컬 캐시 DataStore를 초기화합니다.
    fun logout() {
        viewModelScope.launch {
            // 일부 삭제가 실패해도 나머지 DataStore까지 모두 초기화를 시도합니다.
            val clearResults = listOf(
                dashboardRepository.clearCachedData(),
                dashboardRepository.clearCachedChapelData(),
                gradeRepository.clearCachedData(),
                timetableRepository.clearCachedData(),
                graduationRepository.clearCachedData(),
                tuitionScholarshipCache.clearCachedData(),
                keepRepository.clearCachedData(),
                courseCatalogRepository.clearCachedData(),
                onBoardingRepository.clearCachedData()
            )
            val logoutResult = lmsAuthRepository.logout()

            if (clearResults.all { it.isSuccess } && logoutResult.isSuccess) {
                _logoutCompleted.value = true
            }
        }
    }

    fun onLogoutNavigationHandled() {
        _logoutCompleted.value = false
    }
}
