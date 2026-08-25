package com.yourssu.soongsil.screen.chapel

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

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
        onSemesterSelect = viewModel::selectSemester,
        onRefresh = viewModel::refreshCurrentSemester,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

// 상태별(로딩, 성공 화면) 컨텐츠를 렌더링합니다. 에러는 화면 차단 없이 상단 뱃지로 표시됩니다.
@Composable
private fun ChapelScreenContent(
    uiState: ChapelUiState,
    onRetryClick: () -> Unit,
    onSemesterSelect: (String, String) -> Unit,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading && uiState.chapelData == null -> {
            ChapelLoadingScreen(modifier = modifier)
        }

        else -> {
            ChapelSuccessScreen(
                uiState = uiState,
                onSemesterSelect = onSemesterSelect,
                onRefresh = onRefresh,
                onBackClick = onBackClick,
                modifier = modifier,
            )
        }
    }
}

// 채플 데이터를 정상적으로 불러왔을 때의 메인 화면입니다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapelSuccessScreen(
    uiState: ChapelUiState,
    onSemesterSelect: (String, String) -> Unit,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chapelData = uiState.chapelData ?: DashboardChapelData()
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val pullToRefreshState = rememberPullToRefreshState()

    val seatFloor = getSeatFloor(chapelData.seat)
    val hasData = chapelData.required > 0 || chapelData.weeklyAttendances.isNotEmpty() || chapelData.seat.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 상단 바: 뒤로가기, 타이틀, 학기 선택 드롭다운
        ChapelTopBar(
            selectedYear = uiState.selectedYear.ifBlank { chapelData.year },
            selectedSemester = uiState.selectedSemester.ifBlank { chapelData.semester },
            availableTerms = uiState.availableTerms,
            isSemesterLoading = uiState.isSemesterLoading,
            onSemesterSelect = onSemesterSelect,
            onBackClick = onBackClick,
        )

        // 상단 에러 안내 뱃지 (탑바와의 간격을 줄여 상단에 자연스럽게 이어지도록 배치)
        val currentError = uiState.error ?: uiState.semesterError ?: uiState.termsError
        if (currentError != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 6.dp),
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
                    .padding(bottom = 24.dp + bottomBarPadding),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 상단 요약 & 세그먼트 Progress Bar 섹션
                ChapelOverviewCard(
                    chapelData = chapelData,
                    hasData = hasData,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // 섹션 구분선
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 8.dp
                )

                // 좌석 정보 섹션
                ChapelSeatInfoSection(
                    chapelData = chapelData,
                    seatFloor = seatFloor,
                    hasData = hasData,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // 섹션 구분선
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 8.dp
                )

                // 출석 정보 섹션 (주차별 출석 목록)
                ChapelAttendanceHistorySection(
                    weeklyAttendances = chapelData.weeklyAttendances,
                    isLoading = uiState.isSemesterLoading,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

// 상단 타이틀 및 우측 학기 선택 드롭다운 바를 표시합니다.
@Composable
private fun ChapelTopBar(
    selectedYear: String,
    selectedSemester: String,
    availableTerms: List<DashboardChapelTerm>,
    isSemesterLoading: Boolean,
    onSemesterSelect: (String, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
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

        // 우측 학기 선택 드롭다운 칩 버튼
        ChapelSemesterDropdown(
            selectedYear = selectedYear,
            selectedSemester = selectedSemester,
            options = availableTerms,
            isLoading = isSemesterLoading,
            onSelect = onSemesterSelect
        )
    }
}

// 학기 선택 둥근 드롭다운 버튼 및 메뉴를 표시합니다.
@Composable
private fun ChapelSemesterDropdown(
    selectedYear: String,
    selectedSemester: String,
    options: List<DashboardChapelTerm>,
    isLoading: Boolean,
    onSelect: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = if (selectedYear.isNotBlank() && selectedSemester.isNotBlank()) {
        "${selectedYear}-${selectedSemester}"
    } else {
        "학기 선택"
    }

    Box(modifier = modifier) {
        Surface(
            onClick = { if (!isLoading) expanded = true },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.5.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = displayText,
                    fontFamily = PretendardFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "학기 선택",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                val isSelected = option.year == selectedYear && option.semester == selectedSemester
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${option.year}-${option.semester}",
                            fontFamily = PretendardFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option.year, option.semester)
                    }
                )
            }
        }
    }
}

// 상단 헤드라인, 세그먼트 Progress Bar 및 출석 요약 정보를 표시하는 섹션입니다.
@Composable
private fun ChapelOverviewCard(
    chapelData: DashboardChapelData,
    hasData: Boolean,
    modifier: Modifier = Modifier
) {
    val totalWeeks = chapelData.required.takeIf { it > 0 }
        ?: chapelData.weeklyAttendances.size.takeIf { it > 0 }
        ?: 8

    // 2026년부터는 1회만 결석 가능, 2026년 이전에는 1/3 비율까지 결석 가능
    // 지각 3회는 결석 1회로 환산
    val allowedAbsence = calculateAllowedAbsences(chapelData.year, totalWeeks)
    val effectiveAbsents = chapelData.absent + (chapelData.late / 3)
    val passRequiredAttendances = (totalWeeks - allowedAbsence).coerceAtLeast(1)

    val isPassed = hasData && chapelData.attended >= passRequiredAttendances && totalWeeks > 0
    val isFailed = hasData && effectiveAbsents > allowedAbsence && totalWeeks > 0
    val remaining = (passRequiredAttendances - chapelData.attended).coerceAtLeast(0)

    val headlineText = when {
        !hasData -> "이번 학기 채플 정보가 없어요"
        isPassed -> "축하해요! 이번 학기 PASS했어요!"
        isFailed -> "이번 학기 결석 횟수를 초과했어요"
        remaining > 0 -> "이번 학기 PASS까지 ${remaining}회 남았어요!"
        else -> "이번 학기 PASS까지 0회 남았어요!"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 헤드라인 문구
        Text(
            text = headlineText,
            fontFamily = PretendardFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 주차별 분할된 세그먼트 Progress Bar
        ChapelSegmentedProgressBar(
            totalWeeks = totalWeeks,
            passRequiredAttendances = passRequiredAttendances,
            effectiveAbsentCount = effectiveAbsents,
            weeklyAttendances = chapelData.weeklyAttendances,
            hasData = hasData
        )

        // 하단 요약 정보 (강의시간/장소 및 출석/결석 요약)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chapelData.seatDescription.ifBlank { "강의 정보 없음" },
                fontFamily = PretendardFontFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (hasData) {
                    "${chapelData.attended}/$totalWeeks 출석 / 결석 ${chapelData.absent}회"
                } else {
                    "-/- 출석 / 결석 -회"
                },
                fontFamily = PretendardFontFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// 연도 및 전체 주차 수에 따른 허용 결석 횟수를 계산합니다.
// 2026년부터는 최대 1회, 2026년 이전에는 1/3 비율(주차 / 3)까지 결석 가능합니다.
private fun calculateAllowedAbsences(year: String, totalWeeks: Int): Int {
    val yearInt = year.filter { it.isDigit() }.toIntOrNull() ?: 2026
    return if (yearInt >= 2026) {
        1
    } else {
        (totalWeeks / 3).coerceAtLeast(1)
    }
}

// 주차별 세그먼트 Progress Bar 및 PASS 기준선을 표시하는 컴포넌트입니다.
@Composable
private fun ChapelSegmentedProgressBar(
    totalWeeks: Int,
    passRequiredAttendances: Int,
    effectiveAbsentCount: Int,
    weeklyAttendances: List<DashboardChapelWeeklyAttendance>,
    hasData: Boolean,
    modifier: Modifier = Modifier
) {
    val barHeight = 14.dp
    val barColorAttended = Color(0xFF22C55E) // 출석 (밝은 녹색)
    val barColorAbsent = Color(0xFFFF5252) // 결석 (적색)
    val barColorLate = Color(0xFF9B90FA) // 지각 (연보라)
    val barColorDefault = MaterialTheme.colorScheme.surfaceVariant // 미진행

    val passColor = Color(0xFF16A34A) // PASS 기준선 및 텍스트

    // 결석(지각 3회 포함) 횟수만큼 PASS 기준선 위치가 뒤로 밀림
    val passTargetSlot = passRequiredAttendances + effectiveAbsentCount

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalWidth = maxWidth

        Column(modifier = Modifier.fillMaxWidth()) {
            // 상단 PASS 텍스트 라벨 (기준선 위치 우측/상단에 표시)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            ) {
                if (hasData && totalWeeks > 0 && passTargetSlot <= totalWeeks) {
                    val passFraction = (passTargetSlot.toFloat() / totalWeeks).coerceIn(0f, 1f)
                    val labelOffset = (totalWidth * passFraction - 32.dp).coerceAtLeast(0.dp)
                    Text(
                        text = "PASS",
                        fontFamily = PretendardFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = passColor,
                        modifier = Modifier.offset(x = labelOffset)
                    )
                }
            }

            // 분할된 세그먼트 막대 Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(totalWeeks) { weekIndex ->
                        val attendance = weeklyAttendances.getOrNull(weekIndex)
                        val status = attendance?.status?.trim().orEmpty()

                        val segmentColor = when {
                            !hasData -> barColorDefault
                            status.contains("출석") && !status.contains("결석") && !status.contains("미출석") -> barColorAttended
                            status.contains("결석") || status.contains("미출석") -> barColorAbsent
                            status.contains("지각") -> barColorLate
                            else -> barColorDefault
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(segmentColor)
                        )
                    }
                }

                // PASS 기준 세로선 (결석 시 뒤로 밀림, 주차 범위 내일 때 표시)
                if (hasData && totalWeeks > 0 && passTargetSlot <= totalWeeks) {
                    val passFraction = (passTargetSlot.toFloat() / totalWeeks).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .offset(x = totalWidth * passFraction - 1.dp)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(passColor)
                    )
                }
            }
        }
    }
}

// 좌석 정보와 좌석 배치도 그래픽을 포함하는 섹션입니다.
@Composable
private fun ChapelSeatInfoSection(
    chapelData: DashboardChapelData,
    seatFloor: String,
    hasData: Boolean,
    modifier: Modifier = Modifier
) {
    val seatParts = chapelData.seat.split("-").map { it.trim() }
    val formattedSeat = if (hasData && chapelData.seat.isNotBlank()) {
        listOf(
            seatFloor.ifBlank { "1F" },
            seatParts.joinToString(" - ")
        ).joinToString(" / ")
    } else {
        "좌석 정보 없음"
    }

    val entranceGuide = getEntranceGuideText(chapelData.seat)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "좌석 정보",
            fontFamily = PretendardFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 좌석 번호 크게 표시
        Text(
            text = formattedSeat,
            fontFamily = PretendardFontFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )

        // 출입문 진입 안내 문구
        if (hasData && chapelData.seat.isNotBlank()) {
            Text(
                text = buildAnnotatedString {
                    append(seatFloor.ifBlank { "1층" })
                    append(" ")
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF00897B),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(entranceGuide)
                    }
                    append("으로 들어가세요.")
                },
                fontFamily = PretendardFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Text(
                text = "배정된 좌석 정보가 없습니다.",
                fontFamily = PretendardFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 좌석 배치도 그래픽 임베드
        ChapelSeatMapCard(chapelData = chapelData)
    }
}

// 좌석 번호를 바탕으로 출입문 진입 경로를 안내합니다.
private fun getEntranceGuideText(seat: String): String {
    val zone = seat.substringBefore("-").trim().uppercase()
    return when (zone) {
        "A", "B", "C" -> "정면 좌측 문"
        "D", "E" -> "정면 우측 문"
        "F", "G", "H" -> "좌측 계단 및 출입문"
        "I", "J" -> "우측 계단 및 출입문"
        else -> "지정 출입문"
    }
}

// 주차별 출석 내역을 원형 배지 그리드로 표시하는 섹션입니다.
@Composable
private fun ChapelAttendanceHistorySection(
    weeklyAttendances: List<DashboardChapelWeeklyAttendance>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    val totalCount = weeklyAttendances.size
    val attendedCount = weeklyAttendances.count {
        val status = it.status.trim()
        status.contains("출석") && !status.contains("결석") && !status.contains("미출석")
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 상단 헤더: "출결 정보" + "8 / 12" + 접기/펼치기 화살표
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "출결 정보",
                fontFamily = PretendardFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (weeklyAttendances.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = attendedCount.toString(),
                            fontFamily = PretendardFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                        Text(
                            text = " / $totalCount",
                            fontFamily = PretendardFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "접기" else "펼치기",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 본문: 로딩, 빈 상태, 또는 원형 배지 그리드
        if (isExpanded) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                weeklyAttendances.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "해당 학기의 출석 기록이 없습니다.",
                            fontFamily = PretendardFontFamily,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 색상 안내 범례 (출석, 지각, 결석, 예정)
                        ChapelAttendanceStatusLegend(modifier = Modifier.fillMaxWidth())

                        ChapelAttendanceCircleGrid(
                            weeklyAttendances = weeklyAttendances,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// 출결 상태 색상 안내 범례(Legend) 컴포넌트입니다.
@Composable
private fun ChapelAttendanceStatusLegend(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChapelLegendItem(color = Color(0xFF22C55E), label = "출석")
            ChapelLegendItem(
                color = if (isDarkTheme) Color(0xFF383260) else Color(0xFFE5E2F9),
                borderColor = Color(0xFF7065E6),
                label = "지각"
            )
            ChapelLegendItem(
                color = if (isDarkTheme) Color(0xFF4A2222) else Color(0xFFFFE8E8),
                borderColor = Color(0xFFE53935),
                label = "결석"
            )
            ChapelLegendItem(
                color = if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFF2F3F5),
                borderColor = Color(0xFF9CA3AF),
                label = "예정"
            )
        }
    }
}

// 개별 범례 항목(작은 원 + 텍스트)입니다.
@Composable
private fun ChapelLegendItem(
    color: Color,
    label: String,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (borderColor != null) {
                        Modifier.border(0.8.dp, borderColor, CircleShape)
                    } else Modifier
                )
        )
        Text(
            text = label,
            fontFamily = PretendardFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 주차별 출석 날짜를 6개씩 원형 배지로 정렬하는 그리드입니다.
@Composable
private fun ChapelAttendanceCircleGrid(
    weeklyAttendances: List<DashboardChapelWeeklyAttendance>,
    modifier: Modifier = Modifier
) {
    val itemsPerRow = 6
    val rows = weeklyAttendances.chunked(itemsPerRow)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { attendance ->
                    ChapelAttendanceCircleBadge(attendance = attendance)
                }

                // 남은 빈 슬롯 공간 유지 (한 행이 6개 미만일 때)
                val emptySlots = itemsPerRow - rowItems.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.size(46.dp))
                }
            }
        }
    }
}

// 개별 주차 출석 날짜 원형 배지입니다.
@Composable
private fun ChapelAttendanceCircleBadge(
    attendance: DashboardChapelWeeklyAttendance,
    modifier: Modifier = Modifier
) {
    val status = attendance.status.trim()
    val isAttended = status.contains("출석") && !status.contains("결석") && !status.contains("미출석")
    val isLate = status.contains("지각")
    val isAbsent = (status.contains("결석") || status.contains("미출석")) && !isLate

    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    val (backgroundColor, textColor) = when {
        isAttended -> {
            // 출석: 밝은 녹색 배경 + 흰색 텍스트
            Color(0xFF22C55E) to Color.White
        }

        isLate -> {
            // 지각: 연한 파스텔 연보라 배경 + 보라색 텍스트
            if (isDarkTheme) {
                Color(0xFF383260) to Color(0xFFB5ACF7)
            } else {
                Color(0xFFE5E2F9) to Color(0xFF7065E6)
            }
        }

        isAbsent -> {
            // 결석: 소프트 적색 계열 배경 + 적색 텍스트
            if (isDarkTheme) {
                Color(0xFF4A2222) to Color(0xFFFF8A80)
            } else {
                Color(0xFFFFE8E8) to Color(0xFFE53935)
            }
        }

        else -> {
            // 미진행 (예정): 연한 회색 배경 + 회색 텍스트
            if (isDarkTheme) {
                Color(0xFF2C2C2E) to Color(0xFF8E8E93)
            } else {
                Color(0xFFF2F3F5) to Color(0xFF9CA3AF)
            }
        }
    }

    val displayDate = formatChapelMonthDay(attendance.date)

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayDate,
            fontFamily = PretendardFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

// 날짜 문자열(예: "2026.03.11", "2026-03-11", "3.11")을 "3/11" 형태로 변환합니다.
private fun formatChapelMonthDay(rawDate: String): String {
    val match = Regex("""(\d{1,2})[./\-](\d{1,2})""").find(rawDate.replace(Regex("""^\d{4}[./\-]"""), ""))
    if (match != null) {
        val month = match.groupValues[1].toIntOrNull() ?: match.groupValues[1]
        val day = match.groupValues[2].toIntOrNull() ?: match.groupValues[2]
        return "$month/$day"
    }

    val fullMatch = Regex("""\d{4}[./\-](\d{1,2})[./\-](\d{1,2})""").find(rawDate)
    if (fullMatch != null) {
        val month = fullMatch.groupValues[1].toIntOrNull() ?: fullMatch.groupValues[1]
        val day = fullMatch.groupValues[2].toIntOrNull() ?: fullMatch.groupValues[2]
        return "$month/$day"
    }

    return rawDate.ifBlank { "-" }
}

// 주차별 개별 출석 카드 항목을 표시합니다.
@Composable
private fun ChapelWeeklyAttendanceItem(
    attendance: DashboardChapelWeeklyAttendance,
    modifier: Modifier = Modifier
) {
    val normalizedStatus = attendance.status.trim()
    val statusText = normalizedStatus.ifBlank { "예정" }
    val statusContainerColor = when {
        normalizedStatus.contains("결석") || normalizedStatus.contains("미출석") -> {
            Color(0xFFFFEBEE)
        }
        normalizedStatus.startsWith("출석") -> Color(0xFFE0F2F1)
        normalizedStatus.startsWith("지각") -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val statusContentColor = when {
        normalizedStatus.contains("결석") || normalizedStatus.contains("미출석") -> {
            Color(0xFFE53935)
        }
        normalizedStatus.startsWith("출석") -> Color(0xFF00897B)
        normalizedStatus.startsWith("지각") -> Color(0xFFFB8C00)
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
                    modifier = Modifier.size(width = 54.dp, height = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "${attendance.week}",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = PretendardFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 19.sp,
                    )
                    Text(
                        text = "주차",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontFamily = PretendardFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
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
                    fontFamily = PretendardFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PretendardFontFamily,
                        fontSize = 12.sp,
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
                    fontFamily = PretendardFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// 좌석 번호로부터 층 정보를 계산합니다.
private fun getSeatFloor(seat: String): String {
    val zone = seat
        .substringBefore("-")
        .trim()
        .uppercase()

    return when (zone) {
        "A", "B", "C", "D", "E" -> "1F"
        "F", "G", "H", "I", "J" -> "2F"
        else -> ""
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
                        seat = "C-13-4",
                        seatDescription = "월 15:00-15:50 (08110-서기태)",
                        required = 8,
                        attended = 7,
                        late = 0,
                        absent = 1,
                        remaining = 0,
                        weeklyAttendances = listOf(
                            DashboardChapelWeeklyAttendance(1, "2026.03.09", "정규채플", "서기태", "개강예배", "출석"),
                            DashboardChapelWeeklyAttendance(2, "2026.03.16", "정규채플", "서기태", "비전 채플", "출석"),
                            DashboardChapelWeeklyAttendance(3, "2026.03.23", "정규채플", "서기태", "감사 채플", "출석"),
                            DashboardChapelWeeklyAttendance(4, "2026.03.30", "정규채플", "서기태", "나눔 채플", "출석"),
                            DashboardChapelWeeklyAttendance(5, "2026.04.06", "정규채플", "서기태", "음악 채플", "출석"),
                            DashboardChapelWeeklyAttendance(6, "2026.04.13", "정규채플", "서기태", "특강 채플", "결석"),
                            DashboardChapelWeeklyAttendance(7, "2026.04.20", "정규채플", "서기태", "청년 채플", "출석"),
                            DashboardChapelWeeklyAttendance(8, "2026.04.27", "정규채플", "서기태", "종강예배", "출석")
                        )
                    ),
                    availableTerms = listOf(
                        DashboardChapelTerm("2026", "1학기"),
                        DashboardChapelTerm("2025", "2학기"),
                        DashboardChapelTerm("2025", "1학기")
                    )
                ),
                onSemesterSelect = { _, _ -> },
                onRefresh = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "Chapel Screen Mixed Attendance - Light", showBackground = true)
@Preview(
    name = "Chapel Screen Mixed Attendance - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChapelScreenMixedAttendancePreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChapelSuccessScreen(
                uiState = ChapelUiState(
                    isLoading = false,
                    selectedYear = "2026",
                    selectedSemester = "1학기",
                    chapelData = DashboardChapelData(
                        year = "2026",
                        semester = "1학기",
                        seat = "C-13-4",
                        seatDescription = "월 15:00-15:50 (08110-서기태)",
                        required = 12,
                        attended = 8,
                        late = 1,
                        absent = 1,
                        remaining = 2,
                        weeklyAttendances = listOf(
                            DashboardChapelWeeklyAttendance(1, "2026.03.11", "정규채플", "서기태", "개강예배", "출석"),
                            DashboardChapelWeeklyAttendance(2, "2026.03.18", "정규채플", "서기태", "비전 채플", "출석"),
                            DashboardChapelWeeklyAttendance(3, "2026.03.25", "정규채플", "서기태", "감사 채플", "지각"),
                            DashboardChapelWeeklyAttendance(4, "2026.04.01", "정규채플", "서기태", "나눔 채플", "출석"),
                            DashboardChapelWeeklyAttendance(5, "2026.04.08", "정규채플", "서기태", "음악 채플", "출석"),
                            DashboardChapelWeeklyAttendance(6, "2026.04.15", "정규채플", "서기태", "특강 채플", "출석"),
                            DashboardChapelWeeklyAttendance(7, "2026.04.22", "정규채플", "서기태", "청년 채플", "출석"),
                            DashboardChapelWeeklyAttendance(8, "2026.04.29", "정규채플", "서기태", "문화 채플", "출석"),
                            DashboardChapelWeeklyAttendance(9, "2026.05.06", "정규채플", "서기태", "선교 채플", "결석"),
                            DashboardChapelWeeklyAttendance(10, "2026.05.13", "정규채플", "서기태", "찬양 채플", "출석"),
                            DashboardChapelWeeklyAttendance(11, "2026.05.20", "정규채플", "서기태", "특강 채플", ""),
                            DashboardChapelWeeklyAttendance(12, "2026.05.27", "정규채플", "서기태", "종강예배", "")
                        )
                    ),
                    availableTerms = listOf(
                        DashboardChapelTerm("2026", "1학기"),
                        DashboardChapelTerm("2025", "2학기"),
                        DashboardChapelTerm("2025", "1학기")
                    )
                ),
                onSemesterSelect = { _, _ -> },
                onRefresh = {},
                onBackClick = {}
            )
        }
    }
}

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
                    selectedYear = "2026",
                    selectedSemester = "1학기",
                    chapelData = DashboardChapelData(
                        year = "2026",
                        semester = "1학기",
                        seat = "",
                        seatDescription = "",
                        required = 0,
                        attended = 0,
                        late = 0,
                        absent = 0,
                        weeklyAttendances = emptyList()
                    ),
                    availableTerms = listOf(
                        DashboardChapelTerm("2026", "1학기"),
                        DashboardChapelTerm("2025", "2학기")
                    )
                ),
                onSemesterSelect = { _, _ -> },
                onRefresh = {},
                onBackClick = {}
            )
        }
    }
}

// 상단 에러 안내 뱃지가 표시되는 채플 화면 프리뷰입니다.
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
                    selectedYear = "2026",
                    selectedSemester = "1학기",
                    error = "유세인트 통신 상태가 원활하지 않아 캐시된 데이터를 표시합니다.",
                    chapelData = DashboardChapelData(
                        year = "2026",
                        semester = "1학기",
                        seat = "C-13-4",
                        seatDescription = "월 15:00-15:50 (08110-서기태)",
                        required = 8,
                        attended = 7,
                        late = 0,
                        absent = 1,
                        remaining = 0,
                        weeklyAttendances = listOf(
                            DashboardChapelWeeklyAttendance(1, "2026.03.09", "정규채플", "서기태", "개강예배", "출석"),
                            DashboardChapelWeeklyAttendance(2, "2026.03.16", "정규채플", "서기태", "비전 채플", "출석")
                        )
                    ),
                    availableTerms = listOf(
                        DashboardChapelTerm("2026", "1학기"),
                        DashboardChapelTerm("2025", "2학기")
                    )
                ),
                onSemesterSelect = { _, _ -> },
                onRefresh = {},
                onBackClick = {}
            )
        }
    }
}

// 출결 현황 원형 배지 그리드 컴포넌트 프리뷰입니다.
@Preview(name = "Chapel Circle Grid - Light", showBackground = true)
@Preview(
    name = "Chapel Circle Grid - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChapelAttendanceCircleGridPreview() {
    val sampleAttendances = listOf(
        DashboardChapelWeeklyAttendance(1, "2026.03.11", "정규채플", "서기태", "개강예배", "출석"),
        DashboardChapelWeeklyAttendance(2, "2026.03.18", "정규채플", "서기태", "비전 채플", "출석"),
        DashboardChapelWeeklyAttendance(3, "2026.03.25", "정규채플", "서기태", "감사 채플", "지각"),
        DashboardChapelWeeklyAttendance(4, "2026.04.01", "정규채플", "서기태", "나눔 채플", "출석"),
        DashboardChapelWeeklyAttendance(5, "2026.04.08", "정규채플", "서기태", "음악 채플", "출석"),
        DashboardChapelWeeklyAttendance(6, "2026.04.15", "정규채플", "서기태", "특강 채플", "출석"),
        DashboardChapelWeeklyAttendance(7, "2026.04.22", "정규채플", "서기태", "청년 채플", "출석"),
        DashboardChapelWeeklyAttendance(8, "2026.04.29", "정규채플", "서기태", "문화 채플", "출석"),
        DashboardChapelWeeklyAttendance(9, "2026.05.06", "정규채플", "서기태", "선교 채플", "결석"),
        DashboardChapelWeeklyAttendance(10, "2026.05.13", "정규채플", "서기태", "찬양 채플", "출석"),
        DashboardChapelWeeklyAttendance(11, "2026.05.20", "정규채플", "서기태", "특강 채플", ""),
        DashboardChapelWeeklyAttendance(12, "2026.05.27", "정규채플", "서기태", "종강예배", "")
    )

    SoongsilLifeAndroidTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            ChapelAttendanceCircleGrid(
                weeklyAttendances = sampleAttendances,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 상태별(출석, 지각, 결석, 미진행) 원형 배지 단일 프리뷰입니다.
@Preview(name = "Chapel Circle Badges Status - Light", showBackground = true)
@Preview(
    name = "Chapel Circle Badges Status - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ChapelAttendanceCircleBadgesPreview() {
    SoongsilLifeAndroidTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. 출석 (진한 보라)
                ChapelAttendanceCircleBadge(
                    attendance = DashboardChapelWeeklyAttendance(1, "3/11", "", "", "", "출석")
                )
                // 2. 지각 (연보라)
                ChapelAttendanceCircleBadge(
                    attendance = DashboardChapelWeeklyAttendance(2, "3/25", "", "", "", "지각")
                )
                // 3. 결석 (소프트 적색)
                ChapelAttendanceCircleBadge(
                    attendance = DashboardChapelWeeklyAttendance(3, "5/06", "", "", "", "결석")
                )
                // 4. 미진행 (연회색)
                ChapelAttendanceCircleBadge(
                    attendance = DashboardChapelWeeklyAttendance(4, "5/20", "", "", "", "")
                )
            }
        }
    }
}
