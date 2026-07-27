package com.yourssu.soongsil.screen.timetable

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yourssu.data.timetable.TimetableCourse
import com.yourssu.data.timetable.TimetableDayOfWeek
import com.yourssu.data.timetable.TimetableSemester
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.max

private const val TIMETABLE_START_MINUTES = 8 * 60
private const val TIMETABLE_DEFAULT_END_MINUTES = 17 * 60
private const val TIMETABLE_BOTTOM_EXTRA_MINUTES = 30

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
    fontSize = 9.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 10.8.sp,
    letterSpacing = 0.sp
)

private val TimetableCourseTitleTextStyle = TextStyle(
    fontSize = 8.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 9.4.sp,
    letterSpacing = 0.sp
)

private val TimetableCourseMetaTextStyle = TextStyle(
    fontSize = 6.5.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 7.5.sp,
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

private val TimetableCardBackgroundColors = listOf(
    Color(0xFFDCE9FF),
    Color(0xFFECFDF5),
    Color(0xFFF3E8FF),
    Color(0xFFFFF7ED)
)

private val TimetableCardTextColors = listOf(
    Color(0xFF1D4ED8),
    Color(0xFF047857),
    Color(0xFF7E22CE),
    Color(0xFFC2610A)
)

@Composable
fun TimetableScreen(
    uiState: TimetableViewModel.TimetableUiState,
    onRetry: () -> Unit,
    onCourseClick: (TimetableCourse) -> Unit,
    onDismissCourseDetail: () -> Unit,
    onSelectTerm: ((String, TimetableSemester) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val selectTermAction = onSelectTerm ?: hiltViewModel<TimetableViewModel>()::selectTerm
    val currentYear = uiState.selectedYear.ifBlank { uiState.year.toAcademicYearValue() }
    val currentSemester = uiState.semester.toTimetableSemesterOrNull() ?: uiState.selectedSemester
    var isTermSelectionVisible by remember { mutableStateOf(false) }
    var pendingYear by remember { mutableStateOf(currentYear) }
    var pendingSemester by remember { mutableStateOf(currentSemester) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp + bottomBarPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimetableHeader(
                year = currentYear,
                semester = currentSemester.label,
                onTermClick = {
                    pendingYear = currentYear.ifBlank { buildSelectableYears().first() }
                    pendingSemester = currentSemester
                    isTermSelectionVisible = true
                }
            )

            when {
                uiState.isLoading -> TimetableLoadingState()
                uiState.errorMessage != null -> TimetableErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRetry
                )
                uiState.courses.isEmpty() -> TimetableEmptyState(
                    year = currentYear,
                    semester = currentSemester.label
                )
                else -> TimetableSuccessState(
                    courses = uiState.courses,
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

        if (isTermSelectionVisible) {
            TimetableTermSelectionBottomSheet(
                selectedYear = pendingYear,
                selectedSemester = pendingSemester,
                onYearSelected = { pendingYear = it },
                onSemesterSelected = { pendingSemester = it },
                onDismiss = { isTermSelectionVisible = false },
                onApply = {
                    isTermSelectionVisible = false
                    selectTermAction(pendingYear, pendingSemester)
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
                modifier = Modifier.clickable(onClick = onTermClick),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildSemesterText(year = year, semester = semester),
                    style = TimetableSemesterTextStyle,
                    color = semesterTextColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = "▼",
                    style = TimetableSemesterTextStyle,
                    color = semesterTextColor,
                    maxLines = 1
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
    onCourseClick: (TimetableCourse) -> Unit
) {
    val containerColor = if (isSystemInDarkTheme()) {
        Color(0xFFF2F4F6)
    } else {
        Color(0xFFF2F4F6)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        TimetableGrid(
            courses = courses,
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
    onCourseClick: (TimetableCourse) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayHeaders = TimetableDayOfWeek.entries
    val timeLabelColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        Color(0xFF6B7280)
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val topPadding = 8.dp
        val horizontalPadding = 8.dp
        val timeColumnWidth = 36.dp
        val columnGap = 2.dp
        val headerHeight = 24.dp
        val headerSpacing = 4.dp
        val bottomPadding = 8.dp
        val timetableEndMinutes = calculateTimetableEndMinutes(courses)
        val totalMinutes = timetableEndMinutes - TIMETABLE_START_MINUTES
        val hourLabels = (TIMETABLE_START_MINUTES..timetableEndMinutes step 60).toList()
        val gridTop = topPadding + headerHeight + headerSpacing
        val gridHeight = TimetableHourHeight * (totalMinutes / 60f)
        val containerHeight = topPadding + headerHeight + headerSpacing + gridHeight + bottomPadding
        val availableWidth = maxWidth - horizontalPadding * 2 - timeColumnWidth - columnGap * (dayHeaders.size - 1)
        val dayColumnWidth = availableWidth / dayHeaders.size
        val dayColumnStride = dayColumnWidth + columnGap
        val contentStartX = horizontalPadding + timeColumnWidth
        val gridLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        val visibleCourses = courses.filter {
            it.endMinutes > TIMETABLE_START_MINUTES && it.startMinutes < timetableEndMinutes
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight)
        ) {
            val topPaddingPx = topPadding.toPx()
            val headerHeightPx = headerHeight.toPx()
            val headerSpacingPx = headerSpacing.toPx()
            val gridTopPx = topPaddingPx + headerHeightPx + headerSpacingPx
            val horizontalPaddingPx = horizontalPadding.toPx()
            val timeColumnWidthPx = timeColumnWidth.toPx()
            val dayColumnWidthPx = dayColumnWidth.toPx()
            val columnGapPx = columnGap.toPx()
            val gridHeightPx = gridHeight.toPx()
            val contentStartXPx = horizontalPaddingPx + timeColumnWidthPx

            hourLabels.forEach { labelMinutes ->
                val minutesFromStart = (labelMinutes - TIMETABLE_START_MINUTES).toFloat()
                val y = gridTopPx + gridHeightPx * (minutesFromStart / totalMinutes)
                drawLine(
                    color = gridLineColor,
                    start = Offset(contentStartXPx, y),
                    end = Offset(size.width - horizontalPaddingPx, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            for (index in 0..dayHeaders.size) {
                val x = contentStartXPx + (dayColumnWidthPx + columnGapPx) * index - (columnGapPx * index.coerceAtMost(dayHeaders.size - 1))
                drawLine(
                    color = gridLineColor,
                    start = Offset(x, topPaddingPx),
                    end = Offset(x, gridTopPx + gridHeightPx),
                    strokeWidth = 1.dp.toPx()
                )
            }

            drawLine(
                color = gridLineColor,
                start = Offset(contentStartXPx, topPaddingPx + headerHeightPx),
                end = Offset(size.width - horizontalPaddingPx, topPaddingPx + headerHeightPx),
                strokeWidth = 1.dp.toPx()
            )
        }

        dayHeaders.forEachIndexed { index, dayOfWeek ->
            Box(
                modifier = Modifier
                    .width(dayColumnWidth)
                    .offset(x = contentStartX + dayColumnStride * index, y = topPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfWeek.shortLabel,
                    color = timeLabelColor,
                    style = TimetableDayHeaderTextStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        hourLabels.forEach { labelMinutes ->
            val minutesFromStart = (labelMinutes - TIMETABLE_START_MINUTES).toFloat()
            val offsetY = gridTop + gridHeight * (minutesFromStart / totalMinutes)
            Box(
                modifier = Modifier
                    .width(timeColumnWidth)
                    .offset(x = horizontalPadding, y = offsetY - 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%02d:00".format(labelMinutes / 60),
                    color = timeLabelColor,
                    style = TimetableTimeLabelTextStyle,
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
            val xOffset = contentStartX + dayColumnStride * course.dayOfWeek.ordinal

            TimetableCourseCard(
                course = course,
                cardHeight = cardHeight,
                modifier = Modifier
                    .width(dayColumnWidth)
                    .height(cardHeight)
                    .offset(x = xOffset, y = topOffset + 2.dp),
                onClick = { onCourseClick(course) }
            )
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coursePalette = course.toCoursePalette()
    val classroomText = course.classroom.formatClassroomForCard()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(coursePalette.backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Text(
            text = course.subject,
            color = coursePalette.textColor,
            style = course.toCourseTitleTextStyle(cardHeight),
            maxLines = cardHeight.toCourseTitleMaxLines(),
            softWrap = true,
            overflow = TextOverflow.Clip
        )
        if (classroomText.isNotBlank()) {
            Box(modifier = Modifier.height(0.5.dp))
            Text(
                text = classroomText,
                color = coursePalette.textColor,
                style = TimetableCourseMetaTextStyle,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Clip
            )
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
        containerColor = Color.White,
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
                    color = Color(0xFF0A0A0A),
                    style = TimetableBottomSheetTitleTextStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.professor.toProfessorDisplayName(),
                    color = Color(0xFF6B7280),
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
                    contentColor = Color.White
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
            color = Color(0xFF6B7280),
            style = TimetableBottomSheetLabelTextStyle
        )
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0A0A0A),
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
            .background(Color(0xFFD1D5DB))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableTermSelectionBottomSheet(
    selectedYear: String,
    selectedSemester: TimetableSemester,
    onYearSelected: (String) -> Unit,
    onSemesterSelected: (TimetableSemester) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val selectableYears = remember { buildSelectableYears() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White,
        dragHandle = { TimetableBottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp + navigationBarBottomPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "학기 선택",
                color = Color(0xFF0A0A0A),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            TimetableTermSection(title = "학년도 선택") {
                selectableYears.forEach { year ->
                    TimetableTermOption(
                        text = buildSemesterText(year = year, semester = ""),
                        isSelected = year == selectedYear,
                        onClick = { onYearSelected(year) }
                    )
                }
            }

            TimetableTermSection(title = "학기 선택") {
                TimetableSemester.entries.forEach { semester ->
                    TimetableTermOption(
                        text = semester.label,
                        isSelected = semester == selectedSemester,
                        onClick = { onSemesterSelected(semester) }
                    )
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
                        containerColor = Color(0xFFE5E7EB),
                        contentColor = Color(0xFF0A0A0A)
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
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
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
private fun TimetableTermSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            color = Color(0xFF6B7280),
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
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color(0xFFF2F4F6)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF0A0A0A),
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
        trimmedValue.startsWith("여름") -> "여름학기"
        trimmedValue.startsWith("겨울") -> "겨울학기"
        else -> trimmedValue
    }
}

private fun String.toTimetableSemesterOrNull(): TimetableSemester? {
    return TimetableSemester.fromName(this)
}

private fun buildSelectableYears(currentDate: LocalDate = LocalDate.now()): List<String> {
    return (currentDate.year downTo currentDate.year - 5).map { it.toString() }
}

private fun TimetableCourse.toCoursePalette(): TimetableCoursePalette {
    return when {
        subject == "운영체제" -> TimetableCoursePalette(
            backgroundColor = Color(0xFFDCE9FF),
            textColor = Color(0xFF1D4ED8)
        )
        subject == "데이터베이스응용" -> TimetableCoursePalette(
            backgroundColor = Color(0xFFECFDF5),
            textColor = Color(0xFF047857)
        )
        subject == "네트워크보안" -> TimetableCoursePalette(
            backgroundColor = Color(0xFFF3E8FF),
            textColor = Color(0xFF7E22CE)
        )
        subject == "UI/UX설계및실습" -> TimetableCoursePalette(
            backgroundColor = Color(0xFFFFF7ED),
            textColor = Color(0xFFC2610A)
        )
        subject == "소프트웨어분석및설계" -> TimetableCoursePalette(
            backgroundColor = Color(0xFFECFDF5),
            textColor = Color(0xFF047857)
        )
        subject == "비전체플" -> TimetableCoursePalette(
            backgroundColor = Color(0xFFFFF7ED),
            textColor = Color(0xFFC2610A)
        )
        subject == "캡스톤디자인종합프로젝트1" && dayOfWeek == TimetableDayOfWeek.MONDAY -> TimetableCoursePalette(
            backgroundColor = Color(0xFFF3E8FF),
            textColor = Color(0xFF7E22CE)
        )
        subject == "캡스톤디자인종합프로젝트1" -> TimetableCoursePalette(
            backgroundColor = Color(0xFFFFF7ED),
            textColor = Color(0xFFC2610A)
        )
        else -> {
            val paletteIndex = ((subject.hashCode() % TimetableCardBackgroundColors.size) + TimetableCardBackgroundColors.size) %
                TimetableCardBackgroundColors.size
            TimetableCoursePalette(
                backgroundColor = TimetableCardBackgroundColors[paletteIndex],
                textColor = TimetableCardTextColors[paletteIndex]
            )
        }
    }
}

private fun TimetableCourse.toCourseTitleTextStyle(cardHeight: Dp): TextStyle {
    val fontSize = calculateCourseTitleFontSize(subject = subject, cardHeight = cardHeight)
    val lineHeight = when (fontSize) {
        8.sp -> 9.4.sp
        7.5.sp -> 8.8.sp
        else -> 8.2.sp
    }
    return TimetableCourseTitleTextStyle.copy(
        fontSize = fontSize,
        lineHeight = lineHeight
    )
}

private fun calculateCourseTitleFontSize(subject: String, cardHeight: Dp) = when {
    cardHeight >= 60.dp && subject.length <= 8 -> 8.sp
    subject.length <= 8 -> 8.sp
    subject.length <= 13 -> 7.5.sp
    else -> 7.sp
}

private fun Dp.toCourseTitleMaxLines(): Int {
    return if (this >= 60.dp) 3 else 2
}

private fun String.formatClassroomForCard(): String {
    val trimmedClassroom = trim()
    if (trimmedClassroom.isBlank()) return ""

    val lastWhitespaceIndex = trimmedClassroom.lastIndexOf(' ')
    if (lastWhitespaceIndex == -1) return trimmedClassroom

    val trailingPart = trimmedClassroom.substring(lastWhitespaceIndex + 1)
    return if (trailingPart.any { it.isDigit() }) {
        trimmedClassroom.substring(0, lastWhitespaceIndex) + "\n" + trailingPart
    } else {
        trimmedClassroom
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

private data class TimetableCoursePalette(
    val backgroundColor: Color,
    val textColor: Color
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
        subject = "기초컴퓨터프로그래밍및실습",
        professor = "김교수",
        classroom = "형남공학관 3207",
        dayOfWeek = TimetableDayOfWeek.MONDAY,
        startMinutes = 9 * 60,
        endMinutes = 9 * 60 + 50,
        periodText = "09:00-09:50"
    ),
    TimetableCourse(
        subject = "네트워크프로그래밍",
        professor = "이교수",
        classroom = "정보과학관 21303",
        dayOfWeek = TimetableDayOfWeek.WEDNESDAY,
        startMinutes = 10 * 60 + 30,
        endMinutes = 11 * 60 + 45,
        periodText = "10:30-11:45"
    ),
    TimetableCourse(
        subject = "고급소프트웨어분석설계",
        professor = "박교수",
        classroom = "조만식기념관 세미나실",
        dayOfWeek = TimetableDayOfWeek.FRIDAY,
        startMinutes = 13 * 60,
        endMinutes = 13 * 60 + 50,
        periodText = "13:00-13:50"
    )
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
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND,
                courses = previewCourses
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> }
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
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> }
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
@Composable
private fun TimetableBottomSheetPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
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

@Preview(name = "Timetable Long Text", showBackground = true, widthDp = 402)
@Composable
private fun TimetableLongTextPreview() {
    SoongsilLifeAndroidTheme {
        TimetableScreen(
            uiState = TimetableViewModel.TimetableUiState(
                year = "2026학년도",
                semester = "2학기",
                selectedYear = "2026",
                selectedSemester = TimetableSemester.SECOND,
                courses = previewCoursesWithLongText
            ),
            onRetry = {},
            onCourseClick = {},
            onDismissCourseDetail = {},
            onSelectTerm = { _, _ -> }
        )
    }
}



