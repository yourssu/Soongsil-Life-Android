package com.yourssu.soongsil.screen.scholarship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.scholarship.TuitionScholarshipUiState
import com.yourssu.soongsil.data.ScholarshipRepository
import com.yourssu.soongsil.data.TuitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScholarshipViewModel @Inject constructor(
    private val tuitionRepository: TuitionRepository,
    private val scholarshipRepository: ScholarshipRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TuitionScholarshipUiState())
    val uiState: StateFlow<TuitionScholarshipUiState> = _uiState.asStateFlow()

    init {
        loadTuitionHistories()
        loadScholarshipHistories()
    }

    fun loadTuitionHistories() {

        _uiState.update {
            it.copy(
                isTuitionLoading = true,
                tuitionErrorMessage = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            tuitionRepository.getTuitionHistories()
                .onSuccess { histories ->
                    _uiState.update {
                        it.copy(
                            isTuitionLoading = false,
                            tuitionHistories = histories
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isTuitionLoading = false,
                            tuitionErrorMessage =
                                throwable.message ?: "등록금 내역을 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    fun loadScholarshipHistories() {

        _uiState.update {
            it.copy(
                isScholarshipLoading = true,
                scholarshipErrorMessage = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            scholarshipRepository.getScholarshipHistories()
                .onSuccess { histories ->
                    _uiState.update {
                        it.copy(
                            isScholarshipLoading = false,
                            scholarshipHistories = histories
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isScholarshipLoading = false,
                            scholarshipErrorMessage =
                                throwable.message ?: "장학금 내역을 불러오지 못했습니다."
                        )
                    }
                }
        }
    }
}
