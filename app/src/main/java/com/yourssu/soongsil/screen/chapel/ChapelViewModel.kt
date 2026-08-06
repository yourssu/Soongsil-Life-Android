package com.yourssu.soongsil.screen.chapel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.soongsil.data.DashboardRepository
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
    val error: String? = null,
    val chapelData: DashboardChapelData? = null,
)

@HiltViewModel
class ChapelViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapelUiState())
    val uiState: StateFlow<ChapelUiState> = _uiState.asStateFlow()

    init {
        loadCachedChapelData()
    }

    fun retry() {
        loadCachedChapelData()
    }

    private fun loadCachedChapelData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }

            val chapelData = dashboardRepository
                .getCachedData()
                ?.chapel

            if (chapelData != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        chapelData = chapelData,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "저장된 채플 정보를 불러올 수 없습니다.",
                        chapelData = null,
                    )
                }
            }
        }
    }
}