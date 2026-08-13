package com.yourssu.soongsil.screen.graduation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.graduation.GraduationData
import com.yourssu.soongsil.data.GraduationRepository
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
    private val graduationRepository: GraduationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GraduationUiState())
    val uiState: StateFlow<GraduationUiState> = _uiState.asStateFlow()

    data class GraduationUiState(
        val isLoading: Boolean = true,
        val error: String? = null,
        val graduationData: GraduationData? = null
    )

    init {
        loadGraduationData()
    }

    fun retry() {
        loadGraduationData()
    }

    private fun loadGraduationData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch(Dispatchers.IO) {
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
