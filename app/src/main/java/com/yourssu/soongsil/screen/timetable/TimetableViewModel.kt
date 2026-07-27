package com.yourssu.soongsil.screen.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.timetable.TimetableCourse
import com.yourssu.data.timetable.TimetableSemester
import com.yourssu.data.timetable.TimetableTerm
import com.yourssu.soongsil.data.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository
) : ViewModel() {
    private val initialTerm = defaultTimetableTerm()
    private val _uiState = MutableStateFlow(
        TimetableUiState(
            year = initialTerm.year,
            semester = initialTerm.semester.label,
            selectedYear = initialTerm.year,
            selectedSemester = initialTerm.semester
        )
    )
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()
    private var requestId = 0L

    init {
        loadTimetable(term = initialTerm, forceReload = true)
    }

    fun retry() {
        loadTimetable(term = currentTerm(), forceReload = true)
    }

    fun selectTerm(year: String, semester: TimetableSemester) {
        loadTimetable(
            term = TimetableTerm(
                year = year.toAcademicYearText(),
                semester = semester
            ),
            forceReload = false
        )
    }

    fun selectCourse(course: TimetableCourse) {
        _uiState.update { it.copy(selectedCourse = course) }
    }

    fun dismissCourseDetail() {
        _uiState.update { it.copy(selectedCourse = null) }
    }

    private fun loadTimetable(
        term: TimetableTerm,
        forceReload: Boolean
    ) {
        if (!forceReload && term == currentTerm()) return

        val currentRequestId = ++requestId

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    selectedCourse = null,
                    year = term.year,
                    semester = term.semester.label,
                    selectedYear = term.year,
                    selectedSemester = term.semester,
                    courses = emptyList()
                )
            }

            runCatching { timetableRepository.getTimetable(term.year, term.semester) }
                .onSuccess { timetableData ->
                    if (currentRequestId != requestId) return@onSuccess

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
                    if (currentRequestId != requestId) return@onFailure

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            year = term.year,
                            semester = term.semester.label,
                            courses = emptyList(),
                            errorMessage = throwable.message ?: "시간표를 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    private fun currentTerm(): TimetableTerm {
        return TimetableTerm(
            year = _uiState.value.selectedYear,
            semester = _uiState.value.selectedSemester
        )
    }

    data class TimetableUiState(
        val isLoading: Boolean = false,
        val year: String = "",
        val semester: String = "",
        val selectedYear: String = "",
        val selectedSemester: TimetableSemester = TimetableSemester.FIRST,
        val courses: List<TimetableCourse> = emptyList(),
        val selectedCourse: TimetableCourse? = null,
        val errorMessage: String? = null
    )
}

private fun defaultTimetableTerm(today: LocalDate = LocalDate.now()): TimetableTerm {
    val defaultSemester = if (today.monthValue in 1..8) {
        TimetableSemester.FIRST
    } else {
        TimetableSemester.SECOND
    }

    return TimetableTerm(
        year = today.year.toString(),
        semester = defaultSemester
    )
}

private fun String.toAcademicYearText(): String {
    val trimmedValue = trim()
    val digits = trimmedValue.filter { it.isDigit() }
    return if (digits.length == 4) digits else trimmedValue.removeSuffix("학년도").trim()
}
