package com.yourssu.soongsil.screen.chapel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.R

@Composable
@Preview
fun ChapelSeatLocation(){
    MySeatLocationScreen()
}

// ─── Data ───

private data class SeatInfo(
    val code: String,         // "B-12"
    val floor: String,        // "1층 앞자리"
    val building: String,     // "한경직기념관"
    val zone: String,         // "B"
    val row: Int,             // 2 (0-based)
    val col: Int,             // 3 (0-based)
    val helperText: String
)

// ─── Header ───

@Composable
private fun SeatLocationHeader(
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_caret_left),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .size(24.dp)
                .clickable { onBackClick() },
            tint = Color(0xFF0F172A)
        )
        Text(
            text = "내 좌석 위치",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = "정보",
            modifier = Modifier
                .size(22.dp)
                .clickable { onInfoClick() },
            tint = Color(0xFF0F172A)
        )
    }
}

// ─── Info Card ───

@Composable
private fun SeatInfoCard(
    seatInfo: SeatInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "내 자리",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280)
        )
        Text(
            text = seatInfo.code,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0062FF)
        )
        Text(
            text = "${seatInfo.floor} · ${seatInfo.building}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )
    }
}

// ─── Seat Grid ───

@Composable
private fun SeatGrid(
    rows: Int,
    cols: Int,
    mineRow: Int,
    mineCol: Int,
    rowAisle: Int,
    modifier: Modifier = Modifier
) {
    val seatSize = 13.dp
    val gap = 4.dp
    val aisleGap = 12.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        for (r in 0 until rows) {
            if (r == rowAisle) {
                Spacer(modifier = Modifier.height(aisleGap - gap))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                for (c in 0 until cols) {
                    val isMine = r == mineRow && c == mineCol
                    Box(
                        modifier = Modifier
                            .size(seatSize)
                            .background(
                                when {
                                    isMine -> Color(0xFF0062FF)
                                    else -> Color(0xFFE2E8F0)
                                },
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}

// ─── Map Wrap ───

@Composable
private fun SeatMapWrap(
    seatInfo: SeatInfo,
    rows: Int,
    cols: Int,
    mineRow: Int,
    mineCol: Int,
    rowAisle: Int,
    zoneLabels: List<Pair<String, Boolean>>,
    pulpitLabel: String,
    calloutText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFBFC), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 설교단
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(28.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = pulpitLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
        }

        // 구역 라벨
        Row(
            modifier = Modifier.width(178.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            zoneLabels.forEach { (label, isActive) ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isActive) Color(0xFF0062FF) else Color(0xFF94A3B8)
                )
            }
        }

        // 좌석 그리드
        SeatGrid(
            rows = rows,
            cols = cols,
            mineRow = mineRow,
            mineCol = mineCol,
            rowAisle = rowAisle
        )

        // 콜아웃
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_up),//ic_arrow_up
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFF0062FF)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = calloutText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0062FF)
            )
        }
    }
}

// ─── Helper Text ───

@Composable
private fun HelperText(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6B7280),
            lineHeight = (13 * 1.5).sp
        )
    }
}

// ─── Screen ───

@Composable
private fun MySeatLocationScreen(
    seatCode: String = "B-12",
    seatFloor: String = "1층 앞자리",
    seatBuilding: String = "한경직기념관",
    seatZone: String = "B",
    seatRow: Int = 2,
    seatCol: Int = 3,
    helperText: String = "입구에서 좌측으로 입장해 앞으로 3번째 줄 네 번째 자리예요",
    rows: Int = 12,
    cols: Int = 10,
    rowAisle: Int = 6,
    zoneLabels: List<Pair<String, Boolean>> = listOf(
        "A구역" to false,
        "B구역" to true,
        "C구역" to false
    ),
    pulpitLabel: String = "설교단",
    calloutText: String = "이 자리에요",
    onBackClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val seatInfo = SeatInfo(
        code = seatCode,
        floor = seatFloor,
        building = seatBuilding,
        zone = seatZone,
        row = seatRow,
        col = seatCol,
        helperText = helperText
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SeatLocationHeader(
            onBackClick = onBackClick,
            onInfoClick = onInfoClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SeatInfoCard(seatInfo = seatInfo)
            SeatMapWrap(
                seatInfo = seatInfo,
                rows = rows,
                cols = cols,
                mineRow = seatInfo.row,
                mineCol = seatInfo.col,
                rowAisle = rowAisle,
                zoneLabels = zoneLabels,
                pulpitLabel = pulpitLabel,
                calloutText = calloutText
            )
            HelperText(text = seatInfo.helperText)
        }
    }
}

@Composable
@Preview
fun ChapelSeatLocationSamplePreview() {
    MySeatLocationScreen(
        seatCode = "C-07",
        seatFloor = "2층 중앙",
        seatBuilding = "한경직기념관",
        seatZone = "C",
        seatRow = 4,
        seatCol = 6,
        helperText = "입구에서 우측으로 입장해 5번째 줄 7번째 자리예요",
        rows = 12,
        cols = 10,
        rowAisle = 6,
        zoneLabels = listOf(
            "A구역" to false,
            "B구역" to false,
            "C구역" to true
        ),
        pulpitLabel = "설교단",
        calloutText = "여기예요"
    )
}
