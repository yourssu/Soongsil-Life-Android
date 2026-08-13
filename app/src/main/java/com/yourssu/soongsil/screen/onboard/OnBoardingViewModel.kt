package com.yourssu.soongsil.screen.onboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.soongsil.data.OnBoardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val onBoardingRepository: OnBoardingRepository
) : ViewModel() {
    var serviceTermsAgreed by mutableStateOf(false)
        private set

    var privacyPolicyAgreed by mutableStateOf(false)
        private set

    var marketingTermsAgreed by mutableStateOf(false)
        private set

    var isTermsAgreementCompleted by mutableStateOf(false)
        private set

    val allTermsAgreed: Boolean
        get() = serviceTermsAgreed && privacyPolicyAgreed && marketingTermsAgreed

    val canStart: Boolean
        get() = serviceTermsAgreed && privacyPolicyAgreed

    // 전체 약관의 선택 상태를 변경합니다.
    fun onAllTermsClick() {
        val shouldAgreeAll = !allTermsAgreed
        serviceTermsAgreed = shouldAgreeAll
        privacyPolicyAgreed = shouldAgreeAll
        marketingTermsAgreed = shouldAgreeAll
    }

    // 서비스 이용약관의 선택 상태를 변경합니다.
    fun onServiceTermsClick() {
        serviceTermsAgreed = !serviceTermsAgreed
    }

    // 개인정보 처리방침의 선택 상태를 변경합니다.
    fun onPrivacyPolicyClick() {
        privacyPolicyAgreed = !privacyPolicyAgreed
    }

    // 마케팅 정보 수신 약관의 선택 상태를 변경합니다.
    fun onMarketingTermsClick() {
        marketingTermsAgreed = !marketingTermsAgreed
    }

    // 필수 약관 동의 상태를 저장하고 완료 화면 이동 상태를 변경합니다.
    fun onStartClick() {
        if (!canStart) return

        viewModelScope.launch {
            runCatching { onBoardingRepository.saveTermsAgreement() }
                .onSuccess { isTermsAgreementCompleted = true }
        }
    }

    // 완료 화면으로 이동한 뒤 이동 상태를 초기화합니다.
    fun onTermsAgreementNavigationHandled() {
        isTermsAgreementCompleted = false
    }
}
