package com.yourssu.soongsil.screen.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.timetable.TimetableCacheData
import com.yourssu.data.timetable.TimetableCourse
import com.yourssu.data.timetable.TimetableData
import com.yourssu.data.timetable.TimetableSemester
import com.yourssu.data.timetable.TimetableTerm
import com.yourssu.soongsil.data.TimetableRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
import com.yourssu.soongsil.data.timetableTermOf
import com.yourssu.soongsil.data.toUserFriendlyMessage
import com.yourssu.soongsil.data.withAdditionalTimetableTerm
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
        loadCachedTimetable()
        loadInitialTimetable()
    }

    private fun loadCachedTimetable() {
        viewModelScope.launch(Dispatchers.IO) {
            val cached = timetableRepository.getCachedData() ?: return@launch
            val defaultTimetable = cached.defaultTimetable
            val availableTerms = cached.availableTerms
            val defaultTerm = availableTerms.firstOrNull()
                ?: defaultTimetable.takeIf { it.year.isNotBlank() }?.let {
                    timetableTermOf(year = it.year, semester = it.semester)
                }

            if (defaultTimetable.courses.isNotEmpty() || availableTerms.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingTerms = false,
                        availableTerms = availableTerms,
                        selectedYear = defaultTerm?.year ?: defaultTimetable.year,
                        selectedSemester = defaultTerm?.semester ?: TimetableSemester.FIRST,
                        year = defaultTimetable.year,
                        semester = defaultTimetable.semester,
                        courses = defaultTimetable.courses,
                        errorMessage = null,
                        termLoadError = null
                    )
                }
            }
        }
    }

    fun retry() {
        val state = _uiState.value
        if (state.termLoadError != null || state.availableTerms.isEmpty()) {
            loadInitialTimetable()
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

    // 에러 팝업을 닫습니다.
    fun dismissError() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                termLoadError = null
            )
        }
    }

    private fun loadInitialTimetable() {
        val currentRequestId = ++termRequestId

        val hasExistingData = _uiState.value.courses.isNotEmpty() || _uiState.value.availableTerms.isNotEmpty()
        if (!hasExistingData) {
            _uiState.update {
                it.copy(
                    isLoadingTerms = true,
                    isLoading = true,
                    termLoadError = null,
                    errorMessage = null,
                    selectedCourse = null
                )
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val defaultTimetableResult = runCatching {
                timetableRepository.getDefaultTimetable()
            }

            timetableRepository.getAvailableTerms { availableTermsResult ->
                viewModelScope.launch {
                    if (currentRequestId != termRequestId) return@launch
                    applyInitialTimetableResult(
                        defaultTimetableResult = defaultTimetableResult,
                        availableTermsResult = availableTermsResult
                    )
                }
            }
        }
    }

    private fun applyInitialTimetableResult(
        defaultTimetableResult: Result<TimetableData>,
        availableTermsResult: Result<List<TimetableTerm>>
    ) {
        val defaultTimetable = defaultTimetableResult.getOrNull()
        val defaultTerm = defaultTimetable?.let {
            timetableTermOf(year = it.year, semester = it.semester)
        }

        availableTermsResult
            .onSuccess { termsFromLms ->
                val availableTerms = termsFromLms.withAdditionalTimetableTerm(defaultTerm)
                val selectedTerm = defaultTerm ?: availableTerms.firstOrNull()

                if (selectedTerm == null) {
                    showInitialLoadFailure(defaultTimetableResult.exceptionOrNull())
                    return@onSuccess
                }

                if (defaultTimetable != null && defaultTerm != null) {
                    showDefaultTimetable(
                        timetable = defaultTimetable,
                        term = defaultTerm,
                        availableTerms = availableTerms
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingTerms = false,
                            availableTerms = availableTerms,
                            selectedYear = selectedTerm.year,
                            selectedSemester = selectedTerm.semester,
                            termLoadError = null
                        )
                    }
                    loadTimetable(term = selectedTerm, forceReload = true)
                }
            }
            .onFailure { termsFailure ->
                if (defaultTimetable != null && defaultTerm != null) {
                    showDefaultTimetable(
                        timetable = defaultTimetable,
                        term = defaultTerm,
                        availableTerms = listOf(defaultTerm)
                    )
                } else {
                    showInitialLoadFailure(termsFailure)
                }
            }
    }

    private fun showDefaultTimetable(
        timetable: TimetableData,
        term: TimetableTerm,
        availableTerms: List<TimetableTerm>
    ) {
        _uiState.update {
            it.copy(
                isLoadingTerms = false,
                isLoading = false,
                availableTerms = availableTerms,
                selectedYear = term.year,
                selectedSemester = term.semester,
                year = timetable.year.ifBlank { term.year },
                semester = timetable.semester.ifBlank { term.semester.label },
                courses = timetable.courses,
                errorMessage = null,
                termLoadError = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            timetableRepository.updateCacheData(
                TimetableCacheData(
                    defaultTimetable = timetable,
                    availableTerms = availableTerms
                )
            )
        }
    }

    private fun showInitialLoadFailure(throwable: Throwable?) {
        val hasExistingCourses = _uiState.value.courses.isNotEmpty()
        if (hasExistingCourses) {
            _uiState.update {
                it.copy(
                    isLoadingTerms = false,
                    isLoading = false,
                    errorMessage = throwable?.toUserFriendlyMessage(),
                    loginRequired = throwable?.isLmsLoginRequired() == true
                )
            }
            return
        }

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
                termLoadError = throwable?.toUserFriendlyMessage() ?: "시간표를 불러오지 못했습니다.",
                loginRequired = throwable?.isLmsLoginRequired() == true
            )
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
                    selectedCourse = null,
                    selectedYear = term.year,
                    selectedSemester = term.semester
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

                    val currentCache = timetableRepository.getCachedData() ?: TimetableCacheData()
                    val updatedTimetables = currentCache.timetablesByTerm + ("${term.year}-${term.semester.name}" to timetableData)
                    timetableRepository.updateCacheData(
                        currentCache.copy(
                            timetablesByTerm = updatedTimetables,
                            availableTerms = _uiState.value.availableTerms
                        )
                    )
                }
                .onFailure { throwable ->
                    if (currentRequestId != timetableRequestId) return@onFailure

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.toUserFriendlyMessage(),
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
