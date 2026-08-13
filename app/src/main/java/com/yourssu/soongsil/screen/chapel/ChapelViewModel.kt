package com.yourssu.soongsil.screen.chapel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.data.dashboard.DashboardChapelTerm
import com.yourssu.soongsil.data.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chlwhdtn03.data.Lms.Semester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    val isSemesterLoading: Boolean = false,
    val semesterError: String? = null,
    val availableTerms: List<DashboardChapelTerm> = emptyList(),
    val isTermsLoading: Boolean = false,
    val termsError: String? = null,
)

@HiltViewModel
class ChapelViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapelUiState())
    val uiState: StateFlow<ChapelUiState> = _uiState.asStateFlow()
    private var semesterLoadJob: Job? = null
    private var termsLoadJob: Job? = null
    private var studentId: String = ""
    private var hasLoadedAvailableTerms = false

    init {
        loadCachedChapelData()
    }

    fun retry() {
        loadCachedChapelData()
    }

    fun selectSemester(year: String, semesterName: String) {
        val semester = when (semesterName) {
            Semester.FIRST.nameKor -> Semester.FIRST
            Semester.SECOND.nameKor -> Semester.SECOND
            else -> return
        }

        semesterLoadJob?.cancel()
        semesterLoadJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isSemesterLoading = true,
                    semesterError = null,
                )
            }

            dashboardRepository.getChapelData(year, semester)
                .onSuccess { chapelData ->
                    _uiState.update {
                        it.copy(
                            chapelData = chapelData,
                            isSemesterLoading = false,
                            semesterError = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSemesterLoading = false,
                            semesterError = "선택한 학기의 채플 정보를 불러올 수 없습니다.",
                        )
                    }
                }
        }
    }

    fun loadAvailableChapelTerms() {
        if (hasLoadedAvailableTerms || _uiState.value.isTermsLoading) return

        if (studentId.isBlank()) {
            _uiState.update {
                it.copy(termsError = "로그인한 사용자의 학번 정보를 확인할 수 없습니다.")
            }
            return
        }

        termsLoadJob?.cancel()
        termsLoadJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isTermsLoading = true,
                    termsError = null,
                )
            }

            dashboardRepository.getAvailableChapelTerms(studentId) { term ->
                _uiState.update { state ->
                    state.copy(
                        availableTerms = (state.availableTerms + term)
                            .distinct()
                            .sortedWith(
                                compareByDescending<DashboardChapelTerm> { it.year }
                                    .thenByDescending { it.semester },
                            ),
                    )
                }
            }.onSuccess { terms ->
                hasLoadedAvailableTerms = true
                _uiState.update { state ->
                    state.copy(
                        availableTerms = (state.availableTerms + terms)
                            .distinct()
                            .sortedWith(
                                compareByDescending<DashboardChapelTerm> { it.year }
                                    .thenByDescending { it.semester },
                            ),
                        isTermsLoading = false,
                        termsError = null,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isTermsLoading = false,
                        termsError = "조회할 수 있는 채플 학기를 불러오지 못했습니다.",
                    )
                }
            }
        }
    }

    private fun loadCachedChapelData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }

            val dashboardData = dashboardRepository.getCachedData()
            val chapelData = dashboardData?.chapel
            studentId = dashboardData?.studentId.orEmpty()

            if (chapelData != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        chapelData = chapelData,
                        availableTerms = chapelData
                            .takeIf { it.year.isNotBlank() && it.semester.isNotBlank() }
                            ?.let {
                                listOf(
                                    DashboardChapelTerm(
                                        year = it.year,
                                        semester = it.semester,
                                    ),
                                )
                            }
                            .orEmpty(),
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
