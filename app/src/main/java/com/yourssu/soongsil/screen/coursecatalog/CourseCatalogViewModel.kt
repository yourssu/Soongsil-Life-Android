package com.yourssu.soongsil.screen.coursecatalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.data.coursecatalog.CourseCatalogCategoryData
import com.yourssu.data.coursecatalog.CourseCatalogCourseData
import com.yourssu.data.coursecatalog.CourseCatalogData
import com.yourssu.data.coursecatalog.CourseCatalogFilterData
import com.yourssu.data.coursecatalog.CourseCatalogFilterOptionData
import com.yourssu.data.coursecatalog.CourseCatalogSelectedFilterData
import com.yourssu.data.coursecatalog.CourseCatalogSemester
import com.yourssu.soongsil.data.CourseCatalogRepository
import com.yourssu.soongsil.data.LmsAuthRepository
import com.yourssu.soongsil.data.isLmsLoginRequired
import com.yourssu.soongsil.screen.plan.PlanPdfData
import com.yourssu.soongsil.screen.plan.PlanPdfUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CourseCatalogUiState(
    val data: CourseCatalogData? = null,
    val year: String = LocalDate.now().year.toString(),
    val semester: CourseCatalogSemester = if (LocalDate.now().monthValue <= 6) {
        CourseCatalogSemester.FIRST
    } else {
        CourseCatalogSemester.SECOND
    },
    val category: CourseCatalogCategoryData = CourseCatalogCategoryData.DEPARTMENT,
    val filters: List<CourseCatalogFilterData> = emptyList(),
    val acceptsKeyword: Boolean = false,
    val keyword: String = "",
    val isLoading: Boolean = true,
    val isOptionsLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val optionsErrorMessage: String? = null,
    val planPdfState: PlanPdfUiState = PlanPdfUiState(),
    val loginRequired: Boolean = false
)

@HiltViewModel
class CourseCatalogViewModel @Inject constructor(
    private val courseCatalogRepository: CourseCatalogRepository,
    private val lmsAuthRepository: LmsAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CourseCatalogUiState())
    val uiState: StateFlow<CourseCatalogUiState> = _uiState.asStateFlow()

    private var optionsJob: Job? = null
    private var searchJob: Job? = null
    private var planJob: Job? = null

    init {
        loadInitialData()
    }

    fun setYear(year: String) {
        val normalizedYear = year.filter(Char::isDigit).take(4)
        if (_uiState.value.year == normalizedYear) return
        cancelActiveSearch()
        _uiState.update {
            it.copy(
                year = normalizedYear,
                filters = if (normalizedYear.length == 4) it.filters else emptyList(),
                optionsErrorMessage = null
            )
        }
        if (normalizedYear.length == 4) {
            loadSearchOptions()
        } else {
            optionsJob?.cancel()
            _uiState.update { it.copy(isOptionsLoading = false) }
        }
    }

    fun setSemester(semester: CourseCatalogSemester) {
        if (_uiState.value.semester == semester) return
        cancelActiveSearch()
        _uiState.update {
            it.copy(
                semester = semester,
                filters = emptyList(),
                optionsErrorMessage = null
            )
        }
        loadSearchOptions()
    }

    fun setCategory(category: CourseCatalogCategoryData) {
        if (_uiState.value.category == category) return
        cancelActiveSearch()
        _uiState.update {
            it.copy(
                category = category,
                filters = emptyList(),
                acceptsKeyword = category.acceptsKeyword,
                keyword = "",
                optionsErrorMessage = null
            )
        }
        loadSearchOptions()
    }

    fun selectFilter(index: Int, option: CourseCatalogFilterOptionData) {
        val state = _uiState.value
        val selectedPosition = state.filters.indexOfFirst { it.index == index }
        if (selectedPosition < 0) return
        if (state.filters[selectedPosition].selectedKey == option.key) return
        cancelActiveSearch()

        val updatedFilters = state.filters.mapIndexed { position, filter ->
            when {
                position == selectedPosition -> filter.copy(
                    selectedKey = option.key,
                    selectedLabel = option.label
                )

                position > selectedPosition -> filter.copy(
                    selectedKey = "",
                    selectedLabel = "",
                    options = emptyList()
                )

                else -> filter
            }
        }
        _uiState.update { it.copy(filters = updatedFilters, optionsErrorMessage = null) }
        loadSearchOptions(filterKeys = updatedFilters.selectedKeys())
    }

    fun setKeyword(keyword: String) {
        if (_uiState.value.keyword == keyword) return
        cancelActiveSearch()
        _uiState.update { it.copy(keyword = keyword) }
    }

    fun retrySearchOptions() {
        loadSearchOptions(filterKeys = _uiState.value.filters.selectedKeys())
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(loginRequired = false) }
    }

    fun search() {
        if (searchJob?.isActive == true) return

        val state = _uiState.value
        val errorMessage = validateQuery(state)
        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return
        }

        _uiState.update { it.copy(isSearching = true, errorMessage = null) }
        searchJob = viewModelScope.launch {
            requestCourses(
                year = state.year,
                semester = state.semester,
                category = state.category,
                filterKeys = state.filters.selectedKeys(),
                selectedFilters = state.filters.selectedFilters(),
                keyword = state.keyword.trim()
            )
        }
    }

    fun refresh() {
        if (searchJob?.isActive == true) return
        val data = _uiState.value.data ?: return

        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        searchJob = viewModelScope.launch {
            requestCourses(
                year = data.year,
                semester = data.semester,
                category = data.category,
                filterKeys = data.filterKeys,
                selectedFilters = data.selectedFilters,
                keyword = data.keyword
            )
        }
    }

    fun loadPlan(course: CourseCatalogCourseData) {
        if (planJob?.isActive == true) return

        _uiState.update {
            it.copy(
                planPdfState = PlanPdfUiState(
                    isLoading = true,
                    loadingTitle = course.subjectName
                )
            )
        }
        planJob = viewModelScope.launch {
            lmsAuthRepository.ensureActiveSession()
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            planPdfState = PlanPdfUiState(errorMessage = throwable.message),
                            loginRequired = throwable.isLmsLoginRequired()
                        )
                    }
                    return@launch
                }

            courseCatalogRepository.loadPlan(course)
                .onSuccess { bytes ->
                    if (!isActive) return@onSuccess
                    _uiState.update {
                        it.copy(
                            planPdfState = PlanPdfUiState(
                                pdf = PlanPdfData(
                                    title = course.subjectName,
                                    bytes = bytes
                                )
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) return@onFailure
                    _uiState.update {
                        it.copy(
                            planPdfState = PlanPdfUiState(
                                errorMessage = throwable.message
                                    ?: "강의계획서를 불러오지 못했습니다."
                            )
                        )
                    }
                }
        }
    }

    fun cancelPlanLoading() {
        planJob?.cancel()
        planJob = null
        _uiState.update { it.copy(planPdfState = PlanPdfUiState()) }
    }

    fun closePlan() {
        _uiState.update { it.copy(planPdfState = PlanPdfUiState()) }
    }

    fun dismissPlanError() {
        _uiState.update { it.copy(planPdfState = PlanPdfUiState()) }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val cachedData = courseCatalogRepository.getCachedData()
            _uiState.update {
                it.copy(
                    data = cachedData,
                    year = cachedData?.year ?: it.year,
                    semester = cachedData?.semester ?: it.semester,
                    category = cachedData?.category ?: it.category,
                    acceptsKeyword = cachedData?.category?.acceptsKeyword
                        ?: it.category.acceptsKeyword,
                    keyword = cachedData?.keyword ?: it.keyword,
                    isLoading = false
                )
            }
            loadSearchOptions(filterKeys = cachedData?.filterKeys.orEmpty())
        }
    }

    private fun loadSearchOptions(filterKeys: List<String> = emptyList()) {
        val state = _uiState.value
        if (!state.year.matches(Regex("""\d{4}"""))) return

        optionsJob?.cancel()
        val year = state.year
        val semester = state.semester
        val category = state.category
        _uiState.update {
            it.copy(isOptionsLoading = true, optionsErrorMessage = null)
        }
        optionsJob = viewModelScope.launch {
            lmsAuthRepository.ensureActiveSession()
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isOptionsLoading = false,
                            optionsErrorMessage = throwable.message,
                            loginRequired = throwable.isLmsLoginRequired()
                        )
                    }
                    return@launch
                }

            courseCatalogRepository.getSearchOptions(
                year = year,
                semester = semester,
                category = category,
                filterKeys = filterKeys
            ).onSuccess { options ->
                if (!isCurrentQuery(year, semester, category)) return@onSuccess
                _uiState.update {
                    it.copy(
                        filters = options.filters,
                        acceptsKeyword = options.acceptsKeyword,
                        isOptionsLoading = false,
                        optionsErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) return@onFailure
                if (!isCurrentQuery(year, semester, category)) return@onFailure
                _uiState.update {
                    it.copy(
                        isOptionsLoading = false,
                        optionsErrorMessage = throwable.message
                            ?: "검색 조건을 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    private suspend fun requestCourses(
        year: String,
        semester: CourseCatalogSemester,
        category: CourseCatalogCategoryData,
        filterKeys: List<String>,
        selectedFilters: List<CourseCatalogSelectedFilterData>,
        keyword: String
    ) {
        lmsAuthRepository.ensureActiveSession()
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        isRefreshing = false,
                        errorMessage = throwable.message,
                        loginRequired = throwable.isLmsLoginRequired()
                    )
                }
                return
            }

        courseCatalogRepository.search(
            year = year,
            semester = semester,
            category = category,
            filterKeys = filterKeys,
            selectedFilters = selectedFilters,
            keyword = keyword
        ).onSuccess { data ->
            _uiState.update {
                it.copy(
                    data = data,
                    isSearching = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    isSearching = false,
                    isRefreshing = false,
                    errorMessage = throwable.message ?: "강의시간표를 불러오지 못했습니다."
                )
            }
        }
    }

    private fun isCurrentQuery(
        year: String,
        semester: CourseCatalogSemester,
        category: CourseCatalogCategoryData
    ): Boolean = _uiState.value.let { state ->
        state.year == year && state.semester == semester && state.category == category
    }

    private fun cancelActiveSearch() {
        if (searchJob?.isActive != true) return
        searchJob?.cancel()
        searchJob = null
        _uiState.update {
            it.copy(isSearching = false, isRefreshing = false)
        }
    }

    private fun validateQuery(state: CourseCatalogUiState): String? = when {
        !state.year.matches(Regex("""\d{4}""")) ->
            "학년도를 네 자리 숫자로 입력해 주세요."

        state.isOptionsLoading -> "검색 조건을 불러온 뒤 조회해 주세요."
        state.acceptsKeyword && state.keyword.isBlank() -> "검색어를 입력해 주세요."
        state.category.requiresScopeSelection && state.filters.selectedKeys().isEmpty() ->
            "${state.filters.firstOrNull()?.name ?: "검색 조건"}을 선택해 조회 범위를 줄여 주세요."

        else -> null
    }
}

private fun List<CourseCatalogFilterData>.selectedKeys(): List<String> =
    map(CourseCatalogFilterData::selectedKey).takeWhile(String::isNotBlank)

private fun List<CourseCatalogFilterData>.selectedFilters():
    List<CourseCatalogSelectedFilterData> = mapNotNull { filter ->
    filter.selectedKey.takeIf(String::isNotBlank)?.let {
        CourseCatalogSelectedFilterData(
            name = filter.name,
            key = filter.selectedKey,
            label = filter.selectedLabel
        )
    }
}
