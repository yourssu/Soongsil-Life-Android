package com.yourssu.soongsil.screen.chapel

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.data.dashboard.DashboardChapelTerm
import com.yourssu.data.dashboard.DashboardChapelWeeklyAttendance
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import java.time.DayOfWeek
import java.time.LocalDate

/** 서버에 결석 허용 횟수 필드가 없어 상수로 관리합니다. */
private const val CHAPEL_ALLOWED_ABSENT = 4

@Composable
fun ChapelScreen(
    modifier: Modifier = Modifier,
    viewModel: ChapelViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChapelScreenContent(
        uiState = uiState,
        onRetryClick = viewModel::retry,
        onSemesterSelect = viewModel::selectSemester,
        onAttendanceHistoryOpen = viewModel::loadAvailableChapelTerms,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun ChapelScreenContent(
    uiState: ChapelUiState,
    onRetryClick: () -> Unit,
    onSemesterSelect: (String, String) -> Unit,
    onAttendanceHistoryOpen: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            ChapelLoadingScreen(modifier = modifier)
        }

        uiState.error != null -> {
            ChapelErrorScreen(
                message = uiState.error,
                onRetryClick = onRetryClick,
                modifier = modifier,
            )
        }

        uiState.chapelData != null -> {
            ChapelSuccessScreen(
                uiState = uiState,
                onSemesterSelect = onSemesterSelect,
                onAttendanceHistoryOpen = onAttendanceHistoryOpen,
                onBackClick = onBackClick,
                modifier = modifier,
            )
        }

        else -> {
            ChapelErrorScreen(
                message = "채플 정보를 불러올 수 없습니다.",
                onRetryClick = onRetryClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ChapelSuccessScreen(
    uiState: ChapelUiState,
    onSemesterSelect: (String, String) -> Unit,
    onAttendanceHistoryOpen: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chapelData = uiState.chapelData ?: return
    var showAttendanceHistory by rememberSaveable { mutableStateOf(false) }
    val seatZoneCode = chapelData.seat
        .substringBefore("-")
        .trim()
        .uppercase()

    val seatZone = seatZoneCode
        .takeIf { it.isNotBlank() }
        ?.let { "${it}구역" }
        .orEmpty()

    val seatFloor = getSeatFloor(chapelData.seat)

    ChapelSeatScreen(
        chapelData = chapelData,
        seatNumber = chapelData.seat.ifBlank { "좌석 정보 없음" },
        seatFloor = seatFloor,
        seatZone = seatZone,
        attended = chapelData.attended,
        total = chapelData.required,
        late = chapelData.late,
        absent = chapelData.absent,
        remaining = chapelData.remaining,
        progress = chapelData.progress,
        nextAttendanceDate = getNextAttendanceDate(chapelData.weeklyAttendances),
        onBackClick = onBackClick,
        onAttendanceHistoryClick = {
            showAttendanceHistory = true
            onAttendanceHistoryOpen()
        },
        modifier = modifier,
    )

    if (showAttendanceHistory) {
        ChapelAttendanceHistorySheet(
            chapelData = chapelData,
            isLoading = uiState.isSemesterLoading,
            error = uiState.semesterError,
            availableTerms = uiState.availableTerms,
            isTermsLoading = uiState.isTermsLoading,
            termsError = uiState.termsError,
            onSemesterSelect = onSemesterSelect,
            onDismissRequest = { showAttendanceHistory = false },
        )
    }
}

private fun getSeatFloor(seat: String): String {
    val zone = seat
        .substringBefore("-")
        .trim()
        .uppercase()

    return when (zone) {
        "A", "B", "C", "D", "E" -> "1층"
        "F", "G", "H", "I", "J" -> "2층"
        else -> ""
    }
}

private fun getNextAttendanceDate(
    weeklyAttendances: List<DashboardChapelWeeklyAttendance>,
): String {
    val next = weeklyAttendances
        .filter { it.date.isNotBlank() }
        .firstOrNull { attendance ->
            val status = attendance.status.trim()
            status.isBlank() || status == "예정"
        }
        ?: return "-"

    // "2026.03.09" 형태를 "03 / 09 (월)" 로 변환합니다.
    return runCatching {
        val parts = next.date.split(".", "-", "/").map { it.trim() }
        val date = LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        val dayOfWeek = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "월"
            DayOfWeek.TUESDAY -> "화"
            DayOfWeek.WEDNESDAY -> "수"
            DayOfWeek.THURSDAY -> "목"
            DayOfWeek.FRIDAY -> "금"
            DayOfWeek.SATURDAY -> "토"
            DayOfWeek.SUNDAY -> "일"
        }
        "%02d / %02d (%s)".format(date.monthValue, date.dayOfMonth, dayOfWeek)
    }.getOrDefault(next.date)
}

@Composable
private fun ChapelLoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ChapelErrorScreen(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
        )

        Button(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(text = "다시 시도")
        }
    }
}

@Composable
fun ChapelHeader(
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_caret_left),
                contentDescription = "뒤로 가기",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() },
                tint = Color(0xFF0A0A0A)
            )
            Text(
                text = "채플",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A0A0A),
                letterSpacing = (-0.3).sp
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = "정보",
            modifier = Modifier
                .size(22.dp)
                .clickable { onInfoClick() },
            tint = Color(0xFF9CA3AF)
        )
    }
}

// ─── Seat Header ───

@Composable
fun SeatInfoHeader(
    seatNumber: String,
    floor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "내 자리",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280),
            letterSpacing = (-0.2).sp
        )
        Text(
            text = listOf(floor, seatNumber)
                .filter { it.isNotBlank() }
                .joinToString(" "),
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0062FF),
            letterSpacing = (-0.8).sp,
            lineHeight = 36.sp
        )
    }
}

// ─── Attendance Summary ───

@Composable
fun ChapelSummaryRows(
    attended: Int,
    total: Int,
    absent: Int,
    allowedAbsent: Int,
    nextAttendanceDate: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ChapelSummaryRow(
            label = "출석 현황",
            value = "$attended",
            suffix = "/ $total",
        )
        ChapelSummaryRow(
            label = "결석 현황",
            value = "$absent",
            suffix = "/ $allowedAbsent",
            showInfoIcon = true,
        )
        ChapelSummaryRow(
            label = "다음 출석일",
            value = nextAttendanceDate,
            suffix = null,
        )
    }
}

@Composable
private fun ChapelSummaryRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    suffix: String?,
    showInfoIcon: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                letterSpacing = (-0.2).sp
            )
            if (showInfoIcon) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = Color(0xFF9CA3AF)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A0A0A),
                letterSpacing = (-0.3).sp
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

// ─── Attendance History Button ───

@Composable
fun AttendanceHistoryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "주차별 출석 현황 확인하기",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0A0A0A),
            letterSpacing = (-0.2).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            painter = painterResource(R.drawable.ic_caret_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF9CA3AF)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChapelAttendanceHistorySheet(
    chapelData: DashboardChapelData,
    isLoading: Boolean,
    error: String?,
    availableTerms: List<DashboardChapelTerm>,
    isTermsLoading: Boolean,
    termsError: String?,
    onSemesterSelect: (String, String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        ChapelAttendanceHistoryContent(
            chapelData = chapelData,
            isLoading = isLoading,
            error = error,
            availableTerms = availableTerms,
            isTermsLoading = isTermsLoading,
            termsError = termsError,
            onSemesterSelect = onSemesterSelect,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun ChapelAttendanceHistoryContent(
    chapelData: DashboardChapelData,
    isLoading: Boolean,
    error: String?,
    availableTerms: List<DashboardChapelTerm>,
    isTermsLoading: Boolean,
    termsError: String?,
    onSemesterSelect: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "주차별 출석 현황",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "학기를 선택해 채플 출석 기록을 확인하세요.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }

        ChapelSemesterSelector(
            selectedYear = chapelData.year,
            selectedSemester = chapelData.semester,
            options = availableTerms,
            enabled = !isLoading && !isTermsLoading && availableTerms.isNotEmpty(),
            onSelect = onSemesterSelect,
        )

        if (isLoading || isTermsLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        if (isTermsLoading) {
            Text(
                text = "학번을 기준으로 조회 가능한 채플 학기를 찾고 있습니다.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }

        val visibleError = error ?: termsError
        if (visibleError != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = visibleError,
                    modifier = Modifier.padding(14.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            chapelData.weeklyAttendances.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "해당 학기의 출석 기록이 없습니다.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = chapelData.weeklyAttendances,
                        key = { attendance -> "${attendance.week}-${attendance.date}" },
                    ) { attendance ->
                        ChapelWeeklyAttendanceItem(attendance = attendance)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapelSemesterSelector(
    selectedYear: String,
    selectedSemester: String,
    options: List<DashboardChapelTerm>,
    enabled: Boolean,
    onSelect: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = if (selectedYear.isBlank() || selectedSemester.isBlank()) {
        "학기 선택"
    } else {
        "${selectedYear}학년도 $selectedSemester"
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "⌄",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${option.year}학년도 ${option.semester}",
                            fontWeight = if (
                                option.year == selectedYear &&
                                option.semester == selectedSemester
                            ) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option.year, option.semester)
                    },
                )
            }
        }
    }
}

@Composable
private fun ChapelWeeklyAttendanceItem(
    attendance: DashboardChapelWeeklyAttendance,
    modifier: Modifier = Modifier,
) {
    val normalizedStatus = attendance.status.trim()
    val statusText = normalizedStatus.ifBlank { "예정" }
    val statusContainerColor = when {
        normalizedStatus.contains("결석") || normalizedStatus.contains("미출석") -> {
            MaterialTheme.colorScheme.errorContainer
        }
        normalizedStatus.startsWith("출석") -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        normalizedStatus.startsWith("지각") -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val statusContentColor = when {
        normalizedStatus.contains("결석") || normalizedStatus.contains("미출석") -> {
            MaterialTheme.colorScheme.onErrorContainer
        }
        normalizedStatus.startsWith("출석") -> MaterialTheme.colorScheme.tertiary
        normalizedStatus.startsWith("지각") -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val description = listOf(
        attendance.lectureType,
        attendance.speaker,
        attendance.title,
    ).filter { it.isNotBlank() }.joinToString(" · ")

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .size(width = 58.dp, height = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "${attendance.week}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 19.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = "주차",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        maxLines = 1,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = attendance.date.ifBlank { "수업일 미정" },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Surface(
                color = statusContainerColor,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = statusContentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

// ─── Screen ───

@Composable
fun ChapelSeatScreen(
    modifier: Modifier = Modifier,
    chapelData: DashboardChapelData,
    seatNumber: String = "B-12",
    seatFloor: String = "1층",
    seatZone: String = "A구역",
    attended: Int = 5,
    total: Int = 8,
    late: Int = 1,
    absent: Int = 1,
    remaining: Int = 1,
    progress: Float = 0.6f,
    nextAttendanceDate: String = "-",
    onBackClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onAttendanceHistoryClick: () -> Unit = {},
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ChapelHeader(
            onBackClick = onBackClick,
            onInfoClick = onInfoClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    // 화면을 줄이지 않고 스크롤 콘텐츠 끝에 바텀바 여백을 추가합니다.
                    bottom = 16.dp + bottomBarPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SeatInfoHeader(
                seatNumber = seatNumber,
                floor = seatFloor,
            )
            ChapelSummaryRows(
                attended = attended,
                total = total,
                absent = absent,
                allowedAbsent = CHAPEL_ALLOWED_ABSENT,
                nextAttendanceDate = nextAttendanceDate,
            )
            AttendanceHistoryButton(
                onClick = onAttendanceHistoryClick,
                modifier = Modifier.padding(top = 4.dp),
            )
            ChapelSeatMapCard(
                chapelData = chapelData,
            )
        }
    }
}

@Preview(name = "Attendance History - Light", showBackground = true)
@Preview(
    name = "Attendance History - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChapelAttendanceHistoryPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChapelAttendanceHistoryContent(
                chapelData = DashboardChapelData(
                    year = "2026",
                    semester = "1학기",
                    weeklyAttendances = listOf(
                        DashboardChapelWeeklyAttendance(
                            week = 1,
                            date = "2026.03.09",
                            lectureType = "정규채플",
                            speaker = "홍길동",
                            title = "새로운 시작",
                            status = "출석",
                        ),
                        DashboardChapelWeeklyAttendance(
                            week = 2,
                            date = "2026.03.16",
                            lectureType = "정규채플",
                            status = "지각",
                        ),
                        DashboardChapelWeeklyAttendance(
                            week = 3,
                            date = "2026.03.23",
                            lectureType = "정규채플",
                            status = "결석",
                        ),
                    ),
                ),
                isLoading = false,
                error = null,
                availableTerms = listOf(
                    DashboardChapelTerm(year = "2026", semester = "1학기"),
                    DashboardChapelTerm(year = "2025", semester = "2학기"),
                ),
                isTermsLoading = false,
                termsError = null,
                onSemesterSelect = { _, _ -> },
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}
