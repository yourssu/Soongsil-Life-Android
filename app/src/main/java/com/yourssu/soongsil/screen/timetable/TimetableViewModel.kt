package com.yourssu.soongsil.screen.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.timetable.TimetableCourse
import com.yourssu.data.timetable.TimetableSemester
import com.yourssu.data.timetable.TimetableTerm
import com.yourssu.soongsil.data.TimetableRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TimetableUiState()
    )
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()
    private var termRequestId = 0L
    private var timetableRequestId = 0L

    init {
        loadAvailableTerms()
    }

    fun retry() {
        val state = _uiState.value
        if (state.termLoadError != null || state.availableTerms.isEmpty()) {
            loadAvailableTerms()
            return
        }

        loadTimetable(term = currentTerm(), forceReload = true)
    }

    fun selectTerm(year: String, semester: TimetableSemester) {
        val selectedTerm = _uiState.value.availableTerms.firstOrNull {
            it.year == year.toAcademicYearText() && it.semester == semester
        } ?: return

        loadTimetable(term = selectedTerm, forceReload = false)
    }

    fun selectCourse(course: TimetableCourse) {
        _uiState.update { it.copy(selectedCourse = course) }
    }

    fun dismissCourseDetail() {
        _uiState.update { it.copy(selectedCourse = null) }
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    private fun loadAvailableTerms() {
        val currentRequestId = ++termRequestId

        _uiState.update {
            it.copy(
                isLoadingTerms = true,
                termLoadError = null,
                errorMessage = null,
                selectedCourse = null
            )
        }

        timetableRepository.getAvailableTerms { result ->
            viewModelScope.launch {
                if (currentRequestId != termRequestId) return@launch

                result
                    .onSuccess { availableTerms ->
                        val selectedTerm = availableTerms.firstOrNull {
                            it.year == _uiState.value.selectedYear &&
                                it.semester == _uiState.value.selectedSemester
                        } ?: availableTerms.firstOrNull()

                        if (selectedTerm == null) {
                            _uiState.update {
                                it.copy(
                                    isLoadingTerms = false,
                                    isLoading = false,
                                    availableTerms = emptyList(),
                                    selectedYear = "",
                                    selectedSemester = TimetableSemester.FIRST,
                                    year = "",
                                    semester = "",
                                    courses = emptyList(),
                                    errorMessage = null,
                                    termLoadError = null
                                )
                            }
                            return@onSuccess
                        }

                        _uiState.update {
                            it.copy(
                                isLoadingTerms = false,
                                isLoading = true,
                                availableTerms = availableTerms,
                                selectedYear = selectedTerm.year,
                                selectedSemester = selectedTerm.semester,
                                year = selectedTerm.year,
                                semester = selectedTerm.semester.label,
                                errorMessage = null,
                                termLoadError = null
                            )
                        }

                        loadTimetable(term = selectedTerm, forceReload = true)
                    }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoadingTerms = false,
                                isLoading = false,
                                availableTerms = emptyList(),
                                selectedYear = "",
                                selectedSemester = TimetableSemester.FIRST,
                                year = "",
                                semester = "",
                                courses = emptyList(),
                                errorMessage = null,
                                termLoadError = throwable.message ?: "수강 학기 정보를 불러오지 못했습니다.",
                                loginRequired = throwable.isLmsLoginRequired()
                            )
                        }
                    }
            }
        }
    }

    private fun loadTimetable(
        term: TimetableTerm,
        forceReload: Boolean
    ) {
        if (!forceReload && term.hasSameSelection(currentTerm())) return

        val currentRequestId = ++timetableRequestId

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    selectedYear = term.year,
                    selectedSemester = term.semester,
                    selectedCourse = null,
                    year = term.year,
                    semester = term.semester.label,
                    courses = emptyList()
                )
            }

            runCatching { timetableRepository.getTimetable(term.year, term.semester) }
                .onSuccess { timetableData ->
                    if (currentRequestId != timetableRequestId) return@onSuccess

                    val displayYear = timetableData.year.ifBlank { term.year }
                    val displaySemester = timetableData.semester.ifBlank { term.semester.label }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            year = displayYear,
                            semester = displaySemester,
                            courses = timetableData.courses,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    if (currentRequestId != timetableRequestId) return@onFailure

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            year = term.year,
                            semester = term.semester.label,
                            courses = emptyList(),
                            errorMessage = throwable.message ?: "시간표를 불러오지 못했습니다.",
                            loginRequired = throwable.isLmsLoginRequired()
                        )
                    }
                }
        }
    }

    private fun currentTerm(): TimetableTerm {
        return _uiState.value.availableTerms.firstOrNull {
            it.year == _uiState.value.selectedYear &&
                it.semester == _uiState.value.selectedSemester
        } ?: TimetableTerm(
            year = _uiState.value.selectedYear,
            semester = _uiState.value.selectedSemester
        )
    }

    data class TimetableUiState(
        val isLoadingTerms: Boolean = false,
        val isLoading: Boolean = false,
        val year: String = "",
        val semester: String = "",
        val availableTerms: List<TimetableTerm> = emptyList(),
        val selectedYear: String = "",
        val selectedSemester: TimetableSemester = TimetableSemester.FIRST,
        val courses: List<TimetableCourse> = emptyList(),
        val selectedCourse: TimetableCourse? = null,
        val errorMessage: String? = null,
        val termLoadError: String? = null,
        val loginRequired: Boolean = false
    )
}

private fun String.toAcademicYearText(): String {
    val trimmedValue = trim()
    val digits = trimmedValue.filter { it.isDigit() }
    return if (digits.length == 4) digits else trimmedValue.removeSuffix("학년도").trim()
}

private fun TimetableTerm.hasSameSelection(other: TimetableTerm): Boolean {
    return year == other.year && semester == other.semester
}
