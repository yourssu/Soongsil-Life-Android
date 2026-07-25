package com.yourssu.soongsil.screen.chapel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.data.dashboard.DashboardChapelData

@Composable
fun ChapelScreen(
    modifier: Modifier = Modifier,
    viewModel: ChapelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChapelScreenContent(
        uiState = uiState,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
private fun ChapelScreenContent(
    uiState: ChapelUiState,
    onRetryClick: () -> Unit,
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
                chapelData = uiState.chapelData,
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
    chapelData: DashboardChapelData,
    modifier: Modifier = Modifier,
) {
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
        seatNumber = chapelData.seat.ifBlank { "좌석 정보 없음" },
        seatFloor = seatFloor,
        seatZone = seatZone,
        attended = chapelData.attended,
        total = chapelData.required,
        late = chapelData.late,
        absent = chapelData.absent,
        remaining = chapelData.remaining,
        progress = chapelData.progress,
        modifier = modifier,
    )
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
    onViewSeatClick: () -> Unit,
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
            Text(
                text = "좌석 위치 보기 →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.clickable { onViewSeatClick() }
            )
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

// ─── CTA Button ───

@Composable
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
}

// ─── Screen ───

@Composable
@Preview
fun ChapelSeatScreen(
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
    onViewSeatClick: () -> Unit = {},
    onAttendClick: () -> Unit = {},
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
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SeatHeroCard(
                seatNumber = seatNumber,
                floor = seatFloor,
                zone = seatZone,
                onViewSeatClick = onViewSeatClick
            )
            AttendanceGauge(
                attended = attended,
                total = total,
                late = late,
                absent = absent,
                remaining = remaining,
                progress = progress,
            )
        }

        AttendanceCta(onClick = onAttendClick)
    }
}
