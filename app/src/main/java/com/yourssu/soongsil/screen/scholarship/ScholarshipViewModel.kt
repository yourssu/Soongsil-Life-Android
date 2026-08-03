package com.yourssu.soongsil.screen.scholarship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.scholarship.TuitionScholarshipUiState
import com.yourssu.soongsil.data.ScholarshipRepository
import com.yourssu.soongsil.data.TuitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
                isTuitionLoading = it.tuitionHistories.isEmpty(),
                tuitionErrorMessage = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val refreshResult = async { tuitionRepository.getTuitionHistories() }
            val cachedHistories = tuitionRepository.getCachedTuitionHistories()

            cachedHistories?.let { histories ->
                _uiState.update {
                    it.copy(
                        isTuitionLoading = false,
                        tuitionHistories = histories
                    )
                }
            }

            refreshResult.await()
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
                        val hasHistories = cachedHistories != null || it.tuitionHistories.isNotEmpty()
                        it.copy(
                            isTuitionLoading = false,
                            tuitionErrorMessage = if (hasHistories) {
                                null
                            } else {
                                throwable.message ?: "등록금 내역을 불러오지 못했습니다."
                            }
                        )
                    }
                }
        }
    }

    fun loadScholarshipHistories() {
        _uiState.update {
            it.copy(
                isScholarshipLoading = it.scholarshipHistories.isEmpty(),
                scholarshipErrorMessage = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val refreshResult = async { scholarshipRepository.getScholarshipHistories() }
            val cachedHistories = scholarshipRepository.getCachedScholarshipHistories()

            cachedHistories?.let { histories ->
                _uiState.update {
                    it.copy(
                        isScholarshipLoading = false,
                        scholarshipHistories = histories
                    )
                }
            }

            refreshResult.await()
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
                        val hasHistories =
                            cachedHistories != null || it.scholarshipHistories.isNotEmpty()
                        it.copy(
                            isScholarshipLoading = false,
                            scholarshipErrorMessage = if (hasHistories) {
                                null
                            } else {
                                throwable.message ?: "장학금 내역을 불러오지 못했습니다."
                            }
                        )
                    }
                }
        }
    }
}
