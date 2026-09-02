package com.yourssu.soongsil.screen.chapel

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.data.dashboard.DashboardChapelWeeklyAttendance
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import java.time.DayOfWeek
import java.time.LocalDate

// 채플 화면의 진입점 Composable입니다.
@Composable
fun ChapelScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChapelScreenContent(
        uiState = uiState,
        onRetryClick = viewModel::retry,
        onRefresh = viewModel::refreshCurrentSemester,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

// 상태별(로딩, 성공, 에러 화면) 컨텐츠를 렌더링합니다.
@Composable
private fun ChapelScreenContent(
    uiState: ChapelUiState,
    onRetryClick: () -> Unit,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading && uiState.chapelData == null -> {
            ChapelLoadingScreen(modifier = modifier)
        }

        uiState.error != null && uiState.chapelData == null -> {
            ChapelErrorScreen(
                message = uiState.error,
                onRetryClick = onRetryClick,
                modifier = modifier
            )
        }

        else -> {
            ChapelSuccessScreen(
                uiState = uiState,
                onRefresh = onRefresh,
                onBackClick = onBackClick,
                modifier = modifier,
            )
        }
    }
}

// 채플 데이터를 정상적으로 불러왔을 때의 메인 화면입니다.
// 좌석 번호, 출석 요약 현황 및 좌석 배치도를 표시합니다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapelSuccessScreen(
    uiState: ChapelUiState,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chapelData = uiState.chapelData ?: DashboardChapelData()
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val pullToRefreshState = rememberPullToRefreshState()
    var showAbsenceInfoDialog by rememberSaveable { mutableStateOf(false) }

    val seatFloor = getSeatFloor(chapelData.seat)
    val totalWeeks = chapelData.required.takeIf { it > 0 }
        ?: chapelData.weeklyAttendances.size.takeIf { it > 0 }
        ?: 12
    val allowedAbsent = calculateAllowedAbsences(chapelData.year, totalWeeks)

    // 결석 기준 안내 다이얼로그
    if (showAbsenceInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAbsenceInfoDialog = false },
            title = {
                Text(
                    text = "채플 결석 기준 안내",
                    fontFamily = PretendardFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "• 2026학년도 이후: 학기당 최대 1회 결석 허용\n• 2026학년도 이전: 전체 수업의 1/3 결석 허용\n• 지각 3회는 결석 1회로 환산됩니다.",
                    fontFamily = PretendardFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAbsenceInfoDialog = false }) {
                    Text(
                        text = "확인",
                        fontFamily = PretendardFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 상단 바: 뒤로가기, 타이틀
        ChapelTopBar(
            onBackClick = onBackClick,
        )

        // 상단 에러 안내 뱃지
        val currentError = uiState.error
        if (currentError != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "경고",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = currentError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontFamily = PretendardFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = uiState.isSemesterLoading,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isSemesterLoading,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 24.dp + bottomBarPadding
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 내 자리 정보 (1층 A-1-2)
                SeatInfoHeader(
                    seatNumber = chapelData.seat.ifBlank { "좌석 정보 없음" },
                    floor = seatFloor,
                )

                // 출석/결석 현황 요약
                ChapelSummaryRows(
                    attended = chapelData.attended,
                    total = totalWeeks,
                    absent = chapelData.absent,
                    allowedAbsent = allowedAbsent,
                    nextAttendanceDate = getNextAttendanceDate(chapelData.weeklyAttendances),
                    onInfoClick = { showAbsenceInfoDialog = true }
                )

                // 좌석 배치도 카드 (STAGE 및 구역별 좌석 안내)
                ChapelSeatMapCard(
                    chapelData = chapelData,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Seat Header ───

// 상단 내 자리 정보 헤더입니다.
// @param seatNumber 좌석 번호 (예: "A-1-2")
// @param floor 층수 (예: "1층")
@Composable
fun SeatInfoHeader(
    seatNumber: String,
    floor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "내 자리",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = PretendardFontFamily,
            letterSpacing = (-0.2).sp
        )
        Text(
            text = listOf(floor, seatNumber)
                .filter { it.isNotBlank() }
                .joinToString(" "),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0062FF),
            fontFamily = PretendardFontFamily,
            letterSpacing = (-0.8).sp,
            lineHeight = 38.sp
        )
    }
}

// ─── Attendance Summary ───

// 출석, 결석 현황 및 다음 출석일 요약 행 목록입니다.
@Composable
fun ChapelSummaryRows(
    attended: Int,
    total: Int,
    absent: Int,
    allowedAbsent: Int,
    nextAttendanceDate: String,
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            onInfoClick = onInfoClick,
        )
        ChapelSummaryRow(
            label = "다음 출석일",
            value = nextAttendanceDate,
            suffix = null,
        )
    }
}

// 출결 요약 단일 항목 행 컴포넌트입니다.
@Composable
private fun ChapelSummaryRow(
    label: String,
    value: String,
    suffix: String?,
    modifier: Modifier = Modifier,
    showInfoIcon: Boolean = false,
    onInfoClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PretendardFontFamily,
                letterSpacing = (-0.2).sp
            )
            if (showInfoIcon) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "결석 기준 안내",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onInfoClick),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = PretendardFontFamily,
                letterSpacing = (-0.3).sp
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = PretendardFontFamily,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

// 상단 뒤로가기 버튼과 화면 타이틀을 표시하는 TopBar입니다.
@Composable
private fun ChapelTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_caret_left),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBackClick),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "채플",
            fontFamily = PretendardFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// 좌석 번호로부터 층 정보를 계산합니다.
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

// 문자열 날짜를 LocalDate로 파싱합니다.
// 지원 포맷: "2026.03.09", "2026-03-09", "2026/03/09", "03.09" 등
private fun parseLocalDate(rawDate: String): LocalDate? {
    if (rawDate.isBlank()) return null
    return runCatching {
        val digits = Regex("""\d+""").findAll(rawDate).map { it.value.toInt() }.toList()
        when {
            digits.size >= 3 -> {
                val year = if (digits[0] < 100) digits[0] + 2000 else digits[0]
                LocalDate.of(year, digits[1], digits[2])
            }
            digits.size == 2 -> {
                LocalDate.of(LocalDate.now().year, digits[0], digits[1])
            }
            else -> null
        }
    }.getOrNull()
}

// 주차별 출석 목록으로부터 다음 출석일을 계산합니다.
// 오늘 날짜 이후의 예정된 수업 중 가장 빠른 날짜를 반환하며, 이미 지난 학기이거나 예정 수업이 없으면 "-"를 반환합니다.
private fun getNextAttendanceDate(
    weeklyAttendances: List<DashboardChapelWeeklyAttendance>,
): String {
    val today = LocalDate.now()

    val upcomingDate = weeklyAttendances
        .mapNotNull { attendance ->
            val parsedDate = parseLocalDate(attendance.date) ?: return@mapNotNull null
            val status = attendance.status.trim()
            val isRecorded = status.contains("출석") || status.contains("결석") || status.contains("지각")

            // 이미 출결 처리되었거나 오늘 이전의 지난 수업인 경우 제외
            if (isRecorded || parsedDate.isBefore(today)) {
                null
            } else {
                parsedDate
            }
        }
        .minOrNull() ?: return "-"

    val dayOfWeek = when (upcomingDate.dayOfWeek) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

    return "%02d / %02d (%s)".format(upcomingDate.monthValue, upcomingDate.dayOfMonth, dayOfWeek)
}

// 연도 및 전체 주차 수에 따른 허용 결석 횟수를 계산합니다.
private fun calculateAllowedAbsences(year: String, totalWeeks: Int): Int {
    val yearInt = year.filter { it.isDigit() }.toIntOrNull() ?: 2026
    return if (yearInt >= 2026) {
        1
    } else {
        (totalWeeks / 3).coerceAtLeast(1)
    }
}

// 로딩 화면입니다.
@Composable
private fun ChapelLoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

// 에러 화면입니다.
@Composable
private fun ChapelErrorScreen(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            fontFamily = PretendardFontFamily,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(
                text = "다시 시도",
                fontFamily = PretendardFontFamily
            )
        }
    }
}

// ─── Previews ───

// 메인 채플 화면 프리뷰입니다 (사용자 첨부 이미지 기준: 1층 A-1-2, 4/12 출석, 2/4 결석, 10/02 다음 출석일).
@Preview(name = "Chapel Screen - Light", showBackground = true)
@Preview(
    name = "Chapel Screen - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChapelScreenPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChapelSuccessScreen(
                uiState = ChapelUiState(
                    isLoading = false,
                    chapelData = DashboardChapelData(
                        year = "2026",
                        semester = "1학기",
                        seat = "A-1-2",
                        seatDescription = "한경직기념관 · 월 10:30",
                        required = 12,
                        attended = 4,
                        late = 0,
                        absent = 2,
                        remaining = 6,
                        weeklyAttendances = listOf(
                            DashboardChapelWeeklyAttendance(1, "2026.09.04", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(2, "2026.09.11", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(3, "2026.09.18", "", "", "", "결석"),
                            DashboardChapelWeeklyAttendance(4, "2026.09.25", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(5, "2026.10.02", "", "", "", "예정")
                        )
                    )
                ),
                onRefresh = {},
                onBackClick = {}
            )
        }
    }
}

// 에러 뱃지가 표시되는 채플 화면 프리뷰입니다.
@Preview(name = "Chapel Screen With Error Badge - Light", showBackground = true)
@Preview(
    name = "Chapel Screen With Error Badge - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChapelScreenWithErrorBadgePreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChapelSuccessScreen(
                uiState = ChapelUiState(
                    isLoading = false,
                    error = "유세인트 통신 상태가 원활하지 않아 캐시된 데이터를 표시합니다.",
                    chapelData = DashboardChapelData(
                        year = "2026",
                        semester = "1학기",
                        seat = "A-1-2",
                        seatDescription = "한경직기념관 · 월 10:30",
                        required = 12,
                        attended = 4,
                        late = 0,
                        absent = 2,
                        remaining = 6
                    )
                ),
                onRefresh = {},
                onBackClick = {}
            )
        }
    }
}

// 이미 종강/완료된 과거 학기 채플 화면 프리뷰입니다 (다음 출석일이 '-'로 표시됨).
@Preview(name = "Chapel Screen Ended Semester - Light", showBackground = true)
@Preview(
    name = "Chapel Screen Ended Semester - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChapelScreenEndedSemesterPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChapelSuccessScreen(
                uiState = ChapelUiState(
                    isLoading = false,
                    chapelData = DashboardChapelData(
                        year = "2025",
                        semester = "2학기",
                        seat = "C-13-4",
                        seatDescription = "한경직기념관 · 월 15:00",
                        required = 8,
                        attended = 8,
                        late = 0,
                        absent = 0,
                        remaining = 0,
                        weeklyAttendances = listOf(
                            DashboardChapelWeeklyAttendance(1, "2025.09.01", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(2, "2025.09.08", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(3, "2025.09.15", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(4, "2025.09.22", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(5, "2025.09.29", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(6, "2025.10.06", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(7, "2025.10.13", "", "", "", "출석"),
                            DashboardChapelWeeklyAttendance(8, "2025.10.20", "", "", "", "출석")
                        )
                    )
                ),
                onRefresh = {},
                onBackClick = {}
            )
        }
    }
}

// 데이터가 없는 채플 화면 프리뷰입니다.
@Preview(name = "Chapel Screen Empty - Light", showBackground = true)
@Preview(
    name = "Chapel Screen Empty - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChapelScreenEmptyPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChapelSuccessScreen(
                uiState = ChapelUiState(
                    isLoading = false,
                    chapelData = DashboardChapelData()
                ),
                onRefresh = {},
                onBackClick = {}
            )
        }
    }
}
