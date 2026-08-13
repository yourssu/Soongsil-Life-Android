package com.yourssu.soongsil.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onBoardingDataStore by preferencesDataStore(name = "onboarding_preferences")

@Singleton
class OnBoardingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val termsAgreedKey = booleanPreferencesKey("terms_agreed")

    // 약관에 동의한 이력이 있는지 확인합니다.
    suspend fun hasAgreedToTerms(): Boolean =
        context.onBoardingDataStore.data.first()[termsAgreedKey] ?: false

    // 약관 동의 완료 상태를 저장합니다.
    suspend fun saveTermsAgreement() {
        context.onBoardingDataStore.edit { preferences ->
            preferences[termsAgreedKey] = true
        }
    }
}
