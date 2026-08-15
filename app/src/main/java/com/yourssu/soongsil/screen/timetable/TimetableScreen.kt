package com.yourssu.soongsil.screen.timetable

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yourssu.data.timetable.TimetableCourse
import com.yourssu.data.timetable.TimetableDayOfWeek
import com.yourssu.data.timetable.TimetableSemester
import com.yourssu.data.timetable.TimetableTerm
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.ceil
import kotlin.math.max

private const val TIMETABLE_START_MINUTES = 8 * 60
private const val TIMETABLE_DEFAULT_END_MINUTES = 17 * 60
private const val TIMETABLE_BOTTOM_EXTRA_MINUTES = 30
private const val TIMETABLE_COMPACT_CARD_THRESHOLD_DP = 54
private const val TIMETABLE_MEDIUM_CARD_THRESHOLD_DP = 72
private const val TIMETABLE_BASE_DAY_COUNT = 5
private val TimetableGridLineStroke = 0.5.dp

private val TimetableHourHeight = 48.dp

private val TimetableTitleTextStyle = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 24.sp
)

private val TimetableSemesterTextStyle = TextStyle(
    fontSize = 13.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 15.6.sp
)

private val TimetableDayHeaderTextStyle = TextStyle(
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 13.2.sp
)

private val TimetableTimeLabelTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontSize = 9.5.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 11.sp,
    letterSpacing = 0.sp
)

private val TimetableCourseTitleTextStyle = TextStyle(
    fontSize = 9.5.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 10.5.sp,
    letterSpacing = 0.sp
)

private val TimetableCourseMetaTextStyle = TextStyle(
    fontSize = 7.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 8.sp,
    letterSpacing = 0.sp
)

private val TimetableBottomSheetTitleTextStyle = TextStyle(
    fontSize = 19.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 22.8.sp
)

private val TimetableBottomSheetProfessorTextStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 16.8.sp
)

private val TimetableBottomSheetLabelTextStyle = TextStyle(
    fontSize = 13.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 15.6.sp
)

private val TimetableBottomSheetValueTextStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 16.8.sp
)

private val TimetableCoursePalettes = listOf(
    TimetableCoursePalette(backgroundColor = Color(0xFFDBEAFE), textColor = Color(0xFF1D4ED8)),
    TimetableCoursePalette(backgroundColor = Color(0xFFFFEDD5), textColor = Color(0xFFC2410C)),
    TimetableCoursePalette(backgroundColor = Color(0xFFDCFCE7), textColor = Color(0xFF15803D)),
    TimetableCoursePalette(backgroundColor = Color(0xFFF3E8FF), textColor = Color(0xFF7E22CE)),
    TimetableCoursePalette(backgroundColor = Color(0xFFFEE2E2), textColor = Color(0xFFB91C1C)),
    TimetableCoursePalette(backgroundColor = Color(0xFFCFFAFE), textColor = Color(0xFF0E7490)),
    TimetableCoursePalette(backgroundColor = Color(0xFFFEF3C7), textColor = Color(0xFFA16207)),
    TimetableCoursePalette(backgroundColor = Color(0xFFE0E7FF), textColor = Color(0xFF4338CA)),
    TimetableCoursePalette(backgroundColor = Color(0xFFFCE7F3), textColor = Color(0xFFBE185D)),
    TimetableCoursePalette(backgroundColor = Color(0xFFE2E8F0), textColor = Color(0xFF334155))
)

private val TimetableDarkCoursePalettes = listOf(
    TimetableCoursePalette(
        backgroundColor = Color(0xFF2563EB).copy(alpha = 0.28f),
        textColor = Color(0xFF93C5FD)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFFD97706).copy(alpha = 0.28f),
        textColor = Color(0xFFFDBA74)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFF059669).copy(alpha = 0.28f),
        textColor = Color(0xFF6EE7B7)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFF7C3AED).copy(alpha = 0.28f),
        textColor = Color(0xFFD8B4FE)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFFDC2626).copy(alpha = 0.28f),
        textColor = Color(0xFFFCA5A5)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFF0891B2).copy(alpha = 0.28f),
        textColor = Color(0xFF67E8F9)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFFCA8A04).copy(alpha = 0.28f),
        textColor = Color(0xFFFDE68A)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFF4F46E5).copy(alpha = 0.28f),
        textColor = Color(0xFFA5B4FC)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFFDB2777).copy(alpha = 0.28f),
        textColor = Color(0xFFF9A8D4)
    ),
    TimetableCoursePalette(
        backgroundColor = Color(0xFF475569).copy(alpha = 0.28f),
        textColor = Color(0xFFCBD5E1)
    )
)

@Composable
fun TimetableScreen(
    uiState: TimetableViewModel.TimetableUiState,
    onRetry: () -> Unit,
    onCourseClick: (TimetableCourse) -> Unit,
    onDismissCourseDetail: () -> Unit,
    onSelectTerm: ((String, TimetableSemester) -> Unit)? = null,
    currentDateTimeOverride: ZonedDateTime? = null,
    modifier: Modifier = Modifier
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val selectTermAction = onSelectTerm ?: hiltViewModel<TimetableViewModel>()::selectTerm
    val currentYear = uiState.selectedYear.ifBlank { uiState.year.toAcademicYearValue() }
    val currentSemester = uiState.semester.toTimetableSemesterOrNull()
        ?: uiState.selectedSemester.takeIf { currentYear.isNotBlank() }
    val selectedTerm = uiState.availableTerms.firstOrNull {
        it.year == currentYear && it.semester == currentSemester
    } ?: if (currentYear.isNotBlank() && currentSemester != null) {
        TimetableTerm(year = currentYear, semester = currentSemester)
    } else {
        null
    }
    var isTermSelectionVisible by remember { mutableStateOf(false) }
    var pendingTerm by remember(selectedTerm, uiState.availableTerms) {
        mutableStateOf(selectedTerm ?: uiState.availableTerms.firstOrNull())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 20.dp + bottomBarPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimetableHeader(
                year = currentYear,
                semester = currentSemester?.label.orEmpty(),
                onTermClick = {
                    pendingTerm = selectedTerm ?: uiState.availableTerms.firstOrNull()
                    isTermSelectionVisible = true
                }
            )

            when {
                uiState.isLoadingTerms || uiState.isLoading -> TimetableLoadingState()
                uiState.termLoadError != null -> TimetableAvailableTermsState(
                    message = uiState.termLoadError,
                    onRetry = onRetry
                )
                uiState.availableTerms.isEmpty() -> TimetableAvailableTermsState(
                    message = "수강 학기 정보를 불러오지 못했습니다.",
                    onRetry = onRetry
                )
                uiState.errorMessage != null -> TimetableErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
                uiState.courses.isEmpty() -> TimetableEmptyState(
                    year = currentYear,
                    semester = currentSemester?.label.orEmpty()
                )
                else -> TimetableSuccessState(
                    courses = uiState.courses,
                    selectedTerm = selectedTerm,
                    currentDateTime = currentDateTimeOverride,
                    onCourseClick = onCourseClick
                )
            }
        }

        uiState.selectedCourse?.let { selectedCourse ->
            TimetableCourseDetailBottomSheet(
                course = selectedCourse,
                onDismiss = onDismissCourseDetail
            )
        }

        if (isTermSelectionVisible && uiState.selectedCourse == null) {
            TimetableTermSelectionBottomSheet(
                availableTerms = uiState.availableTerms,
                selectedTerm = pendingTerm,
                isLoading = uiState.isLoadingTerms,
                errorMessage = uiState.termLoadError,
                onTermSelected = { pendingTerm = it },
                onRetry = onRetry,
                onDismiss = { isTermSelectionVisible = false },
                onApply = {
                    val appliedTerm = pendingTerm ?: return@TimetableTermSelectionBottomSheet
                    isTermSelectionVisible = false
                    selectTermAction(appliedTerm.year, appliedTerm.semester)
                }
            )
        }
    }
}

@Composable
private fun TimetableHeader(
    year: String,
    semester: String,
    onTermClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semesterTextColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onBackground
    } else {
        Color(0xFF0A0A0A)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "수업 시간표",
            style = TimetableTitleTextStyle,
            color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else Color(0xFF0A0A0A),
            maxLines = 1
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onTermClick)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildSemesterText(year = year, semester = semester),
                    style = TimetableSemesterTextStyle,
                    color = semesterTextColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = "학기 선택",
                    modifier = Modifier.width(18.dp),
                    tint = semesterTextColor,
                )
            }
        }
    }
}

@Composable
private fun TimetableLoadingState() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "시간표를 불러오는 중입니다.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TimetableErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "시간표를 불러오지 못했습니다.",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text(text = "다시 시도")
            }
        }
    }
}

@Composable
private fun TimetableAvailableTermsState(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "조회 가능한 학기가 없습니다",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text(text = "다시 시도")
            }
        }
    }
}

@Composable
private fun TimetableEmptyState(
    year: String,
    semester: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "등록된 시간표가 없습니다",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "다른 학기를 선택해 확인해 보세요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            if (year.isNotBlank() || semester.isNotBlank()) {
                Text(
                    text = buildSemesterText(year = year, semester = semester),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TimetableSuccessState(
    courses: List<TimetableCourse>,
    selectedTerm: TimetableTerm?,
    currentDateTime: ZonedDateTime?,
    onCourseClick: (TimetableCourse) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        TimetableGrid(
            courses = courses,
            selectedTerm = selectedTerm,
            currentDateTime = currentDateTime,
            onCourseClick = onCourseClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        )
    }
}

@Composable
private fun TimetableGrid(
    courses: List<TimetableCourse>,
    selectedTerm: TimetableTerm?,
    currentDateTime: ZonedDateTime?,
    onCourseClick: (TimetableCourse) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayHeaders = TimetableDayOfWeek.entries
    val coursePaletteIndices = remember(courses) { buildTimetableCoursePaletteIndices(courses) }
    val timetableSurfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val todayColumnColor = MaterialTheme.colorScheme.primary.copy(
        alpha = if (isSystemInDarkTheme()) 0.16f else 0.08f
    )
    val currentIndicatorColor = MaterialTheme.colorScheme.primary
    val horizontalScrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val topPadding = 8.dp
        val horizontalPadding = 6.dp
        val timeColumnWidth = 34.dp
        val columnGap = 1.dp
        val headerHeight = 24.dp
        val headerSpacing = 4.dp
        val bottomPadding = 8.dp
        val timetableEndMinutes = calculateTimetableEndMinutes(courses)
        val totalMinutes = timetableEndMinutes - TIMETABLE_START_MINUTES
        val hourLabels = (TIMETABLE_START_MINUTES..timetableEndMinutes step 60).toList()
        val gridTop = topPadding + headerHeight + headerSpacing
        val gridHeight = TimetableHourHeight * (totalMinutes / 60f)
        val containerHeight = topPadding + headerHeight + headerSpacing + gridHeight + bottomPadding
        val weekdayColumnsWidth = maxWidth -
            horizontalPadding * 2 -
            timeColumnWidth -
            columnGap * (TIMETABLE_BASE_DAY_COUNT - 1)
        val dayColumnWidth = weekdayColumnsWidth / TIMETABLE_BASE_DAY_COUNT
        val dayColumnStride = dayColumnWidth + columnGap
        val contentStartX = horizontalPadding + timeColumnWidth
        val gridWidth = dayColumnWidth * dayHeaders.size + columnGap * (dayHeaders.size - 1)
        val scrollViewportWidth = maxWidth - contentStartX - horizontalPadding
        val effectiveCurrentDateTime = currentDateTime ?: remember { ZonedDateTime.now() }
        val currentState = remember(selectedTerm, effectiveCurrentDateTime) {
            buildTimetableCurrentState(
                selectedTerm = selectedTerm,
                now = effectiveCurrentDateTime
            )
        }
        val visibleCourses = courses.filter {
            it.endMinutes > TIMETABLE_START_MINUTES && it.startMinutes < timetableEndMinutes
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight)
        ) {
            Box(
                modifier = Modifier
                    .width(scrollViewportWidth)
                    .height(containerHeight)
                    .offset(x = contentStartX)
                    .horizontalScroll(horizontalScrollState)
            ) {
                Box(
                    modifier = Modifier
                        .width(gridWidth)
                        .height(containerHeight)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val topPaddingPx = topPadding.toPx()
                        val headerHeightPx = headerHeight.toPx()
                        val headerSpacingPx = headerSpacing.toPx()
                        val gridTopPx = topPaddingPx + headerHeightPx + headerSpacingPx
                        val dayColumnWidthPx = dayColumnWidth.toPx()
                        val columnGapPx = columnGap.toPx()
                        val gridHeightPx = gridHeight.toPx()
                        val gridStrokeWidth = TimetableGridLineStroke.toPx()

                        currentState.todayDayOfWeek?.let { todayDayOfWeek ->
                            drawRect(
                                color = todayColumnColor,
                                topLeft = Offset(
                                    x = dayColumnStride.toPx() * todayDayOfWeek.ordinal,
                                    y = gridTopPx
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    width = dayColumnWidthPx,
                                    height = gridHeightPx
                                )
                            )
                        }

                        hourLabels.forEach { labelMinutes ->
                            val minutesFromStart = (labelMinutes - TIMETABLE_START_MINUTES).toFloat()
                            val y = gridTopPx + gridHeightPx * (minutesFromStart / totalMinutes)
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(gridWidth.toPx(), y),
                                strokeWidth = gridStrokeWidth
                            )
                        }

                        for (index in 1 until dayHeaders.size) {
                            val x = dayColumnWidthPx * index + columnGapPx * (index - 0.5f)
                            drawLine(
                                color = gridLineColor,
                                start = Offset(x, gridTopPx),
                                end = Offset(x, gridTopPx + gridHeightPx),
                                strokeWidth = gridStrokeWidth
                            )
                        }
                    }

                    dayHeaders.forEachIndexed { index, dayOfWeek ->
                        Box(
                            modifier = Modifier
                                .width(dayColumnWidth)
                                .offset(x = dayColumnStride * index, y = topPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayOfWeek.shortLabel,
                                color = if (currentState.todayDayOfWeek == dayOfWeek) {
                                    currentIndicatorColor
                                } else {
                                    labelColor
                                },
                                style = TimetableDayHeaderTextStyle,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    visibleCourses.forEach { course ->
                        val startMinutes = max(course.startMinutes, TIMETABLE_START_MINUTES)
                        val endMinutes = max(startMinutes, minOf(course.endMinutes, timetableEndMinutes))
                        if (endMinutes <= startMinutes) return@forEach
                        val topOffset = gridTop + TimetableHourHeight * ((startMinutes - TIMETABLE_START_MINUTES) / 60f)
                        val cardHeight = TimetableHourHeight * ((endMinutes - startMinutes) / 60f)
                        val xOffset = dayColumnStride * course.dayOfWeek.ordinal

                        TimetableCourseCard(
                            course = course,
                            cardHeight = cardHeight,
                            coursePaletteIndex = coursePaletteIndices[course.subject] ?: 0,
                            modifier = Modifier
                                .width(dayColumnWidth)
                                .height(cardHeight)
                                .offset(x = xOffset, y = topOffset + 2.dp),
                            onClick = { onCourseClick(course) }
                        )
                    }
                }
            }

            hourLabels.forEach { labelMinutes ->
                val minutesFromStart = (labelMinutes - TIMETABLE_START_MINUTES).toFloat()
                val offsetY = gridTop + gridHeight * (minutesFromStart / totalMinutes)
                Box(
                    modifier = Modifier
                        .width(timeColumnWidth)
                        .offset(x = horizontalPadding, y = offsetY - 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d:00".format(labelMinutes / 60),
                        color = labelColor,
                        style = TimetableTimeLabelTextStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .background(timetableSurfaceColor)
                            .padding(horizontal = 1.dp)
                    )
                }
            }
        }
    }
}

private fun calculateTimetableEndMinutes(courses: List<TimetableCourse>): Int {
    val lastCourseEndMinutes = courses.maxOfOrNull { it.endMinutes } ?: TIMETABLE_DEFAULT_END_MINUTES
    val endMinutesWithPadding = max(TIMETABLE_DEFAULT_END_MINUTES, lastCourseEndMinutes) + TIMETABLE_BOTTOM_EXTRA_MINUTES
    return ceil(endMinutesWithPadding / 30f).toInt() * 30
}

@Composable
private fun TimetableCourseCard(
    course: TimetableCourse,
    cardHeight: Dp,
    coursePaletteIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coursePalette = getTimetableCoursePalette(
        paletteIndex = coursePaletteIndex,
        isDarkTheme = isSystemInDarkTheme()
    )
    val classroomDisplay = splitClassroom(course.classroom)
    val cardContentSpec = buildCourseCardContentSpec(cardHeight)
    val locationLines = buildCourseCardLocationLines(
        classroomDisplay = classroomDisplay,
        cardContentSpec = cardContentSpec
    )
    val titleFontSpec = courseTitleFontSpec(course.subject)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(coursePalette.backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        contentAlignment = if (cardContentSpec.isCompact) Alignment.CenterStart else Alignment.TopStart
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = course.subject,
                color = coursePalette.textColor,
                style = TimetableCourseTitleTextStyle.copy(
                    fontSize = titleFontSpec.maxFontSize,
                    lineHeight = titleFontSpec.lineHeight,
                    textAlign = TextAlign.Start
                ),
                autoSize = titleFontSpec.autoSize,
                maxLines = cardContentSpec.titleMaxLines,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )

            locationLines.forEach { line ->
                Text(
                    text = line,
                    color = coursePalette.textColor,
                    style = TimetableCourseMetaTextStyle.copy(textAlign = TextAlign.Start),
                    maxLines = if (cardContentSpec.isCompact) 2 else 1,
                    softWrap = true,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableCourseDetailBottomSheet(
    course: TimetableCourse,
    onDismiss: () -> Unit
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { TimetableBottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp + navigationBarBottomPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = course.subject.ifBlank { "과목 정보 없음" },
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TimetableBottomSheetTitleTextStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.professor.toProfessorDisplayName(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TimetableBottomSheetProfessorTextStyle
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimetableDetailRow(label = "요일", value = course.dayOfWeek.toDisplayDayLabel())
                TimetableDetailRow(label = "시간", value = course.toDisplayPeriodText())
                TimetableDetailRow(label = "강의실", value = course.classroom.ifBlank { "강의실 정보 없음" })
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(
                    text = "닫기",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TimetableDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = TimetableBottomSheetLabelTextStyle
        )
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            style = TimetableBottomSheetValueTextStyle,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TimetableBottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 6.dp, bottom = 14.dp)
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableTermSelectionBottomSheet(
    availableTerms: List<TimetableTerm>,
    selectedTerm: TimetableTerm?,
    isLoading: Boolean,
    errorMessage: String?,
    onTermSelected: (TimetableTerm) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { TimetableBottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp + navigationBarBottomPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "학기 선택",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            when {
                isLoading -> TimetableTermSelectionLoadingState()
                errorMessage != null -> TimetableTermSelectionUnavailableState(
                    message = errorMessage,
                    onRetry = onRetry
                )
                availableTerms.isEmpty() -> TimetableTermSelectionUnavailableState(
                    message = "수강 학기 정보를 불러오지 못했습니다.",
                    onRetry = onRetry
                )
                else -> TimetableTermSection(title = "조회 가능한 학기") {
                    availableTerms.forEach { term ->
                        TimetableTermOption(
                            text = buildSemesterText(year = term.year, semester = term.semester.label),
                            isSelected = term == selectedTerm,
                            onClick = { onTermSelected(term) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "취소",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isLoading && errorMessage == null && availableTerms.isNotEmpty() && selectedTerm != null,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "적용",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TimetableTermSelectionLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            text = "수강 학기 정보를 불러오는 중입니다.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TimetableTermSelectionUnavailableState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "조회 가능한 학기가 없습니다",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(text = "다시 시도")
        }
    }
}

@Composable
private fun TimetableTermSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun TimetableTermOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

private fun buildSemesterText(year: String, semester: String): String {
    val normalizedYear = year.trim().let { trimmedYear ->
        when {
            trimmedYear.isBlank() -> ""
            trimmedYear.endsWith("학년도") -> trimmedYear
            trimmedYear.all { it.isDigit() } -> "${trimmedYear}학년도"
            else -> trimmedYear
        }
    }

    return listOf(normalizedYear, semester.toDisplaySemesterLabel())
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "학기 정보 없음" }
}

private fun String.toAcademicYearValue(): String {
    val trimmedValue = trim()
    val digits = trimmedValue.filter { it.isDigit() }
    return if (digits.length == 4) digits else trimmedValue.removeSuffix("학년도").trim()
}

private fun String.toDisplaySemesterLabel(): String {
    val trimmedValue = trim().replace(" ", "")
    return when {
        trimmedValue.isBlank() -> ""
        trimmedValue.endsWith("학기") -> trimmedValue
        trimmedValue == "1" -> "1학기"
        trimmedValue == "2" -> "2학기"
        trimmedValue.startsWith("하계") -> "여름학기"
        trimmedValue.startsWith("여름") -> "여름학기"
        trimmedValue.startsWith("동계") -> "겨울학기"
        trimmedValue.startsWith("겨울") -> "겨울학기"
        else -> trimmedValue
    }
}

private fun String.toTimetableSemesterOrNull(): TimetableSemester? {
    return TimetableSemester.fromName(this)
}

internal fun buildTimetableCoursePaletteIndices(
    courses: List<TimetableCourse>,
    paletteSize: Int = TimetableCoursePalettes.size
): Map<String, Int> {
    if (paletteSize <= 0) return emptyMap()

    return courses
        .map { it.subject }
        .distinct()
        .sorted()
        .mapIndexed { index, subject -> subject to index % paletteSize }
        .toMap()
}

internal fun getTimetableCoursePalette(
    paletteIndex: Int,
    isDarkTheme: Boolean
): TimetableCoursePalette {
    val palettes = if (isDarkTheme) TimetableDarkCoursePalettes else TimetableCoursePalettes
    return palettes.getOrElse(paletteIndex) { palettes.first() }
}

internal fun courseTitleFontSize(subject: String) = when (subject.trim().length) {
    in 0..8 -> 9.5.sp
    in 9..13 -> 8.5.sp
    else -> 7.5.sp
}

private fun courseTitleFontSpec(subject: String): TimetableCourseTitleFontSpec {
    val maxFontSize = courseTitleFontSize(subject)
    val lineHeight = when (maxFontSize) {
        9.5.sp -> 10.5.sp
        8.5.sp -> 9.5.sp
        else -> 8.5.sp
    }

    return TimetableCourseTitleFontSpec(
        maxFontSize = maxFontSize,
        lineHeight = lineHeight,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 7.5.sp,
            maxFontSize = maxFontSize,
            stepSize = 0.5.sp
        )
    )
}

internal fun splitClassroom(classroom: String?): ClassroomDisplay {
    val trimmedClassroom = classroom?.trim().orEmpty()
    if (trimmedClassroom.isBlank()) return ClassroomDisplay()

    val lastWhitespaceIndex = trimmedClassroom.lastIndexOf(' ')
    if (lastWhitespaceIndex == -1) {
        return ClassroomDisplay(building = trimmedClassroom)
    }

    val building = trimmedClassroom.substring(0, lastWhitespaceIndex).trim().ifBlank { null }
    val room = trimmedClassroom.substring(lastWhitespaceIndex + 1).trim().ifBlank { null }

    return if (room != null && room.any { it.isDigit() }) {
        ClassroomDisplay(building = building, room = room)
    } else {
        ClassroomDisplay(building = trimmedClassroom)
    }
}

internal fun isCurrentTimetableTerm(
    term: TimetableTerm?,
    now: Instant
): Boolean {
    val startAt = term?.startAt.toInstantOrNull() ?: return false
    val endAt = term?.endAt.toInstantOrNull() ?: return false
    return !now.isBefore(startAt) && !now.isAfter(endAt)
}

private fun buildTimetableCurrentState(
    selectedTerm: TimetableTerm?,
    now: ZonedDateTime
): TimetableCurrentState {
    val currentTerm = isCurrentTimetableTerm(selectedTerm, now.toInstant())
    val todayDayOfWeek = if (currentTerm) now.dayOfWeek.toTimetableDayOfWeekOrNull() else null
    return TimetableCurrentState(
        isCurrentTerm = currentTerm,
        todayDayOfWeek = todayDayOfWeek
    )
}

private fun String?.toInstantOrNull(): Instant? {
    val trimmedValue = this?.trim().orEmpty()
    if (trimmedValue.isBlank()) return null
    return runCatching { Instant.parse(trimmedValue) }.getOrNull()
}

internal fun DayOfWeek.toTimetableDayOfWeekOrNull(): TimetableDayOfWeek? {
    return when (this) {
        DayOfWeek.MONDAY -> TimetableDayOfWeek.MONDAY
        DayOfWeek.TUESDAY -> TimetableDayOfWeek.TUESDAY
        DayOfWeek.WEDNESDAY -> TimetableDayOfWeek.WEDNESDAY
        DayOfWeek.THURSDAY -> TimetableDayOfWeek.THURSDAY
        DayOfWeek.FRIDAY -> TimetableDayOfWeek.FRIDAY
        DayOfWeek.SATURDAY -> TimetableDayOfWeek.SATURDAY
        DayOfWeek.SUNDAY -> null
    }
}

private fun String.toProfessorDisplayName(): String {
    val trimmedProfessor = trim()
    return when {
        trimmedProfessor.isBlank() -> "교수 정보 없음"
        trimmedProfessor.endsWith("교수") -> trimmedProfessor
        else -> "$trimmedProfessor 교수"
    }
}

private fun TimetableDayOfWeek.toDisplayDayLabel(): String {
    return when (this) {
        TimetableDayOfWeek.MONDAY -> "월요일"
        TimetableDayOfWeek.TUESDAY -> "화요일"
        TimetableDayOfWeek.WEDNESDAY -> "수요일"
        TimetableDayOfWeek.THURSDAY -> "목요일"
        TimetableDayOfWeek.FRIDAY -> "금요일"
        TimetableDayOfWeek.SATURDAY -> "토요일"
    }
}

private fun TimetableCourse.toDisplayPeriodText(): String {
    val trimmedPeriodText = periodText.trim()
    return when {
        trimmedPeriodText.isNotBlank() -> trimmedPeriodText.replace(" ", "")
        startMinutes < endMinutes -> "${startMinutes.toTimeText()}-${endMinutes.toTimeText()}"
        else -> "시간 정보 없음"
    }
}

private fun Int.toTimeText(): String {
    val hour = this / 60
    val minute = this % 60
    return "%02d:%02d".format(hour, minute)
}

internal data class TimetableCoursePalette(
    val backgroundColor: Color,
    val textColor: Color
)

private data class TimetableCourseTitleFontSpec(
    val maxFontSize: androidx.compose.ui.unit.TextUnit,
    val lineHeight: androidx.compose.ui.unit.TextUnit,
    val autoSize: TextAutoSize
)

internal data class ClassroomDisplay(
    val building: String? = null,
    val room: String? = null
)

private data class TimetableCurrentState(
    val isCurrentTerm: Boolean,
    val todayDayOfWeek: TimetableDayOfWeek? = null
)

internal data class TimetableCourseCardContentSpec(
    val titleMaxLines: Int,
    val locationLineCount: Int,
    val isCompact: Boolean
)

internal fun buildCourseCardContentSpec(cardHeight: Dp): TimetableCourseCardContentSpec {
    return when {
        cardHeight < TIMETABLE_COMPACT_CARD_THRESHOLD_DP.dp -> TimetableCourseCardContentSpec(
            titleMaxLines = 2,
            locationLineCount = 2,
            isCompact = true
        )
        cardHeight < TIMETABLE_MEDIUM_CARD_THRESHOLD_DP.dp -> TimetableCourseCardContentSpec(
            titleMaxLines = 2,
            locationLineCount = 1,
            isCompact = false
        )
        else -> TimetableCourseCardContentSpec(
            titleMaxLines = 2,
            locationLineCount = 2,
            isCompact = false
        )
    }
}

internal fun buildCourseCardLocationLines(
    classroomDisplay: ClassroomDisplay,
    cardContentSpec: TimetableCourseCardContentSpec
): List<String> {
    return classroomDisplay
        .toCardLocationLines(isCompactCard = cardContentSpec.isCompact)
        .take(cardContentSpec.locationLineCount)
}

private fun ClassroomDisplay.toCardLocationLines(isCompactCard: Boolean): List<String> {
    if (isCompactCard) {
        return listOfNotNull(formatCompactClassroom(building = building, room = room))
    }

    return buildList {
        building?.let(::add)
        room?.let(::add)
    }
}

private fun formatCompactClassroom(
    building: String?,
    room: String?,
): String? {
    return listOfNotNull(building, room)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
        .ifBlank { null }
}

private val previewAvailableTerms = listOf(
    TimetableTerm(
        year = "2026",
        semester = TimetableSemester.SECOND,
        sourceName = "2026년 2학기",
        startAt = "2026-07-01T00:00:00Z",
        endAt = "2026-12-31T23:59:59Z"
    ),
    TimetableTerm(
        year = "2026",
        semester = TimetableSemester.FIRST,
        sourceName = "2026년 1학기",
        startAt = "2026-01-01T00:00:00Z",
        endAt = "2026-06-30T23:59:59Z"
    ),
    TimetableTerm(
        year = "2025",
        semester = TimetableSemester.SECOND,
        sourceName = "2025년 2학기",
        startAt = "2025-07-01T00:00:00Z",
        endAt = "2025-12-31T23:59:59Z"
    ),
    TimetableTerm(
        year = "2025",
        semester = TimetableSemester.FIRST,
        sourceName = "2025년 1학기",
        startAt = "2025-01-01T00:00:00Z",
        endAt = "2025-06-30T23:59:59Z"
    ),
    TimetableTerm(year = "2024", semester = TimetableSemester.SUMMER, sourceName = "2024-하계계절제"),
    TimetableTerm(year = "2023", semester = TimetableSemester.WINTER, sourceName = "2023-동계계절제")
)

private val previewCourses = listOf(
    TimetableCourse(
        subject = "자료구조",
        professor = "홍길동",
        classroom = "정보과학관 101호",
        dayOfWeek = TimetableDayOfWeek.MONDAY,
        startMinutes = 9 * 60,
        endMinutes = 10 * 60 + 15,
        periodText = "09:00-10:15"
    ),
    TimetableCourse(
        subject = "운영체제",
        professor = "김교수",
        classroom = "형남공학관 305호",
        dayOfWeek = TimetableDayOfWeek.WEDNESDAY,
        startMinutes = 13 * 60,
        endMinutes = 14 * 60 + 15,
        periodText = "13:00-14:15"
    ),
    TimetableCourse(
        subject = "모바일프로그래밍",
        professor = "이교수",
        classroom = "정보과학관 202호",
        dayOfWeek = TimetableDayOfWeek.FRIDAY,
        startMinutes = 15 * 60,
        endMinutes = 16 * 60 + 15,
        periodText = "15:00-16:15"
    )
)

private val previewCoursesWithLateClass = listOf(
    TimetableCourse(
        subject = "자료구조",
        professor = "홍길동",
        classroom = "정보과학관 101호",
        dayOfWeek = TimetableDayOfWeek.MONDAY,
        startMinutes = 9 * 60,
        endMinutes = 10 * 60 + 15,
        periodText = "09:00-10:15"
    ),
    TimetableCourse(
        subject = "캡스톤디자인종합프로젝트1",
        professor = "박교수",
        classroom = "형남공학관 410호",
        dayOfWeek = TimetableDayOfWeek.THURSDAY,
        startMinutes = 16 * 60,
        endMinutes = 17 * 60 + 15,
        periodText = "16:00-17:15"
    )
)

private val previewCoursesWithLongText = listOf(
    TimetableCourse(
        subject = "고급컴퓨터프로그래밍및실습",
        professor = "김교수",
        classroom = "정보과학관 101호",
        dayOfWeek = TimetableDayOfWeek.MONDAY,
        startMinutes = 9 * 60,
        endMinutes = 9 * 60 + 50,
        periodText = "09:00-09:50"
    ),
    TimetableCourse(
        subject = "시스템소프트웨어설계",
        professor = "이교수",
        classroom = "형남공학관 305호",
        dayOfWeek = TimetableDayOfWeek.WEDNESDAY,
        startMinutes = 10 * 60 + 30,
        endMinutes = 11 * 60 + 45,
        periodText = "10:30-11:45"
    ),
    TimetableCourse(
        subject = "데이터베이스응용",
        professor = "박교수",
        classroom = "미래관 202호",
        dayOfWeek = TimetableDayOfWeek.FRIDAY,
        startMinutes = 13 * 60,
        endMinutes = 13 * 60 + 50,
        periodText = "13:00-13:50"
    ),
    TimetableCourse(
        subject = "온라인세미나",
        professor = "정교수",
        classroom = "온라인",
        dayOfWeek = TimetableDayOfWeek.THURSDAY,
        startMinutes = 15 * 60,
        endMinutes = 15 * 60 + 50,
        periodText = "15:00-15:50"
    ),
    TimetableCourse(
        subject = "캡스톤설계",
        professor = "조교수",
        classroom = "",
        dayOfWeek = TimetableDayOfWeek.TUESDAY,
        startMinutes = 16 * 60,
        endMinutes = 16 * 60 + 50,
        periodText = "16:00-16:50"
    )
)

private val previewTenCourses = listOf(
    TimetableCourse("고급컴퓨터프로그래밍및실습", "김교수", "정보과학관 21303", TimetableDayOfWeek.MONDAY, 9 * 60, 9 * 60 + 50, "09:00-09:50"),
    TimetableCourse("시스템소프트웨어설계", "이교수", "형남공학관 3207", TimetableDayOfWeek.MONDAY, 11 * 60, 12 * 60 + 15, "11:00-12:15"),
    TimetableCourse("데이터베이스응용", "박교수", "정보과학관 21303", TimetableDayOfWeek.TUESDAY, 9 * 60, 10 * 60 + 15, "09:00-10:15"),
    TimetableCourse("알고리즘", "최교수", "정보과학관 20302", TimetableDayOfWeek.TUESDAY, 15 * 60, 15 * 60 + 50, "15:00-15:50"),
    TimetableCourse("컴퓨터구조", "정교수", "형남공학관 410호", TimetableDayOfWeek.WEDNESDAY, 10 * 60 + 30, 11 * 60 + 45, "10:30-11:45"),
    TimetableCourse("운영체제", "이교수", "형남공학관 305호", TimetableDayOfWeek.WEDNESDAY, 15 * 60, 15 * 60 + 50, "15:00-15:50"),
    TimetableCourse("네트워크보안", "김교수", "정보과학관 21404", TimetableDayOfWeek.THURSDAY, 12 * 60, 12 * 60 + 50, "12:00-12:50"),
    TimetableCourse("모바일프로그래밍", "조교수", "정보과학관 202호", TimetableDayOfWeek.THURSDAY, 15 * 60, 16 * 60 + 15, "15:00-16:15"),
    TimetableCourse("인공지능개론", "박교수", "조만식기념관 501호", TimetableDayOfWeek.FRIDAY, 9 * 60, 9 * 60 + 50, "09:00-09:50"),
    TimetableCourse("캡스톤디자인", "송교수", "형남공학관 410호", TimetableDayOfWeek.FRIDAY, 12 * 60, 13 * 60 + 15, "12:00-13:15")
)

private val previewBottomSheetCourse = TimetableCourse(
    subject = "데이터베이스응용",
    professor = "이상호",
    classroom = "정보과학관 21303",
    dayOfWeek = TimetableDayOfWeek.MONDAY,
    startMinutes = 10 * 60 + 30,
    endMinutes = 11 * 60 + 45,
    periodText = "10:30-11:45"
)

private val previewCurrentDateTime = ZonedDateTime.of(
    2026,
    8,
    3,
    11,
    20,
    0,
    0,
    ZoneId.of("Asia/Seoul")
)

@Preview(name = "Timetable Light", showBackground = true, widthDp = 402)
@Preview(
    name = "Timetable Dark",
    showBackground = true,
    widthDp = 402,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TimetableScreenPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
                availableTerms = previewAvailableTerms,
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND,
                courses = previewCourses
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> },
            currentDateTimeOverride = previewCurrentDateTime
        )
    }
}

@Preview(name = "Timetable Empty", showBackground = true, widthDp = 402)
@Composable
private fun TimetableEmptyPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
                availableTerms = previewAvailableTerms,
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> },
            currentDateTimeOverride = previewCurrentDateTime
        )
    }
}

@Preview(name = "Timetable Error", showBackground = true, widthDp = 402)
@Composable
private fun TimetableErrorPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
                availableTerms = previewAvailableTerms,
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND,
                errorMessage = "LMS 세션이 만료되어 시간표를 불러오지 못했습니다."
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> }
        )
    }
}

@Preview(name = "Timetable Sheet", showBackground = true, widthDp = 402)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 402)
@Composable
private fun TimetableBottomSheetPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
                availableTerms = previewAvailableTerms,
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND,
                courses = previewCourses,
                selectedCourse = previewBottomSheetCourse
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> }
        )
    }
}

@Preview(name = "Timetable Late Class", showBackground = true, widthDp = 402)
@Composable
private fun TimetableLateClassPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
                availableTerms = previewAvailableTerms,
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND,
                courses = previewCoursesWithLateClass
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> }
        )
    }
}

@Preview(name = "Timetable Long Course", showBackground = true, widthDp = 402)
@Composable
private fun TimetableLongTextPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
                availableTerms = previewAvailableTerms,
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND,
                courses = previewCoursesWithLongText
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> },
            currentDateTimeOverride = previewCurrentDateTime
        )
    }
}

@Preview(name = "Timetable Ten Courses", showBackground = true, widthDp = 402)
@Composable
private fun TimetableTenCoursesPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
                availableTerms = previewAvailableTerms,
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND,
                courses = previewTenCourses
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> },
            currentDateTimeOverride = previewCurrentDateTime
        )
    }
}

@Preview(name = "Timetable No Terms", showBackground = true, widthDp = 402)
@Composable
private fun TimetableNoTermsPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> }
        )
    }
}

@Preview(name = "Timetable Term Sheet", showBackground = true, widthDp = 402)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 402)
@Composable
private fun TimetableTermSelectionBottomSheetPreview() {
    SoongsilLifeAndroidTheme {
        TimetableTermSelectionBottomSheet(
            availableTerms = previewAvailableTerms,
            selectedTerm = previewAvailableTerms.first(),
            isLoading = false,
            errorMessage = null,
            onTermSelected = {},
            onRetry = {},
            onDismiss = {},
            onApply = {}
        )
    }
}



