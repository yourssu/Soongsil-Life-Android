package com.yourssu.soongsil.screen.chapel

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.data.dashboard.DashboardChapelTerm
import com.yourssu.data.dashboard.DashboardChapelWeeklyAttendance
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

@Composable
fun ChapelScreen(
    modifier: Modifier = Modifier,
    viewModel: ChapelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChapelScreenContent(
        uiState = uiState,
        onRetryClick = viewModel::retry,
        onSemesterSelect = viewModel::selectSemester,
        onAttendanceHistoryOpen = viewModel::loadAvailableChapelTerms,
        modifier = modifier,
    )
}

@Composable
private fun ChapelScreenContent(
    uiState: ChapelUiState,
    onRetryClick: () -> Unit,
    onSemesterSelect: (String, String) -> Unit,
    onAttendanceHistoryOpen: () -> Unit,
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

// ─── Hero Card ───

@Composable
fun SeatHeroCard(
    seatNumber: String,
    floor: String,
    zone: String,
    onAttendanceHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0062FF), RoundedCornerShape(20.dp))
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "내 자리",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xCCFFFFFF)
        )
        Text(
            text = seatNumber,
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-3).sp,
            lineHeight = 72.sp
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0x1FFFFFFF)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = listOf(floor, zone) //수정
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xCCFFFFFF)
            )
            TextButton(
                onClick = onAttendanceHistoryClick,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "주차별 출석 현황 확인하기",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_caret_right),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

// ─── Attendance Gauge ───

@Composable
fun AttendanceGauge(
    attended: Int,
    total: Int,
    late: Int,
    absent: Int,
    remaining: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 상단: 라벨 + 횟수
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "이번 학기 남은 출석",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0A0A0A),
                letterSpacing = (-0.2).sp
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "$remaining",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0062FF),
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = "/ ${total}회",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        // 프로그레스 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFF1F5F9), RoundedCornerShape(9999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color(0xFF0062FF), RoundedCornerShape(9999.dp))
            )
        }

        // 하단: 출석 현황 + 퍼센트
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${attended}회 출석 · ${late}회 지각 · ${absent}회 결석", //수정
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0062FF)
            )
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true },
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

// ─── CTA Button ───

/*@Composable
fun AttendanceCta(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0A0A0A)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_qr_code),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Text(
                    text = "출석 인증하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.2).sp
                )
            }
        }
        Text(
            text = "입실 후 좌석 QR을 스캔해주세요",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF)
        )
    }
}*/

// ─── Screen ───
@Composable
//@Preview
fun ChapelSeatScreen(
    chapelData: DashboardChapelData,
    seatNumber: String = "B-12",
    seatFloor: String = "1층 앞자리",
    seatZone: String = "A구역",
    attended: Int = 5,
    total: Int = 8,
    late: Int = 1,
    absent: Int = 1,
    remaining: Int = 1,
    progress: Float = 0.6f,
    onBackClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onAttendanceHistoryClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(bottom = bottomBarPadding)
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
                    bottom = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SeatHeroCard(
                seatNumber = seatNumber,
                floor = seatFloor,
                zone = seatZone,
                onAttendanceHistoryClick = onAttendanceHistoryClick,
            )
            AttendanceGauge(
                attended = attended,
                total = total,
                late = late,
                absent = absent,
                remaining = remaining,
                progress = progress,
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
