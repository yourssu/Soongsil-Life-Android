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
import com.yourssu.data.dashboard.DashboardChapelData
import com.yourssu.soongsil.R

@Composable
fun ChapelSeatLocation(
    chapelData: DashboardChapelData,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit = {},
) {
    val seatParts = chapelData.seat
        .split("-")
        .map { it.trim() }

    val zone = seatParts
        .getOrNull(0)
        ?.uppercase()
        .orEmpty()

    val rowNumber = seatParts
        .getOrNull(1)
        ?.toIntOrNull()
        ?: 1

    val columnNumber = seatParts
        .getOrNull(2)
        ?.toIntOrNull()
        ?: 1

    val floor = getSeatLocationFloor(zone)

    val building = chapelData.seatDescription
        .substringBefore(" · ")
        .ifBlank { "한경직기념관" }

    MySeatLocationScreen(
        seatCode = chapelData.seat.ifBlank { "좌석정보 없음" },
        seatFloor = floor,
        seatBuilding = building,
        seatZone = zone,
        seatRow = (rowNumber - 1).coerceAtLeast(0),
        seatCol = (columnNumber - 1).coerceAtLeast(0),
        helperText = if (chapelData.seat.isBlank()) {
            "배정된 좌석 정보가 없습니다."
        } else {
            "${zone}구역 ${rowNumber}번째 줄 ${columnNumber}번째 자리예요"
        },
        onBackClick = onBackClick,
        onInfoClick = onInfoClick,
    )
}

private fun getSeatLocationFloor(zone: String): String {
    return when (zone.trim().uppercase()) {
        "A", "B", "C", "D", "E" -> "1층"
        "F", "G", "H", "I", "J" -> "2층"
        else -> ""
    }
}

private data class SeatInfo(
    val code: String,
    val floor: String,
    val building: String,
    val zone: String,
    val row: Int,
    val col: Int,
    val helperText: String,
)

@Composable
private fun SeatLocationHeader(
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_caret_left),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBackClick),
            tint = Color(0xFF0F172A),
        )

        Text(
            text = "내 좌석 위치",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
        )

        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = "정보",
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onInfoClick),
            tint = Color(0xFF0F172A),
        )
    }
}

@Composable
private fun SeatInfoCard(
    seatInfo: SeatInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "내 자리",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280),
        )

        Text(
            text = seatInfo.code,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0062FF),
        )

        Text(
            text = listOf(seatInfo.floor, seatInfo.building)
                .filter { it.isNotBlank() }
                .joinToString(" · "),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280),
        )
    }
}

private fun getZoneSeatPattern(zone: String): List<String> {
    return when (zone) {
        "A" -> listOf(
            "00011111",
            "00111111",
            "01111111",
            "11111111",
            "11111111",
            "11111111",
            "01111111",
            "",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111100",
        )

        "B" -> listOf(
            "0111111",
            "0111111",
            "0111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1110000",
        )

        "C" -> listOf(
            "00111111100",
            "01111111110",
            "01111111110",
            "01111111110",
            "01111111110",
            "11111111111",
            "11111111111",
            "",
            "11111111111",
            "11111111111",
            "11111111111",
            "11111111111",
            "11111111111",
            "11111111111",
            "11111111111",
            "11111111111",
        )

        "D" -> listOf(
            "1111110",
            "1111110",
            "1111110",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
        )

        "E" -> listOf(
            "11111000",
            "11111100",
            "11111110",
            "11111111",
            "11111111",
            "11111111",
            "11111110",
            "",
            "01111111",
            "01111111",
            "01111111",
            "01111111",
            "01111111",
            "01111111",
            "01111111",
            "01111111",
            "01111111",
            "00011111",
        )

        "F" -> listOf(
            "11111111",
            "11111111",
            "11111111",
            "11111111",
            "11111111",
            "11111111",
            "",
            "11100000",
            "11100000",
            "11100000",
            "11111111",
            "11111111",
            "11111111",
            "00111111",
            "00111110",
        )

        "G" -> listOf(
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111110",
            "1111110",
        )

        "H" -> listOf(
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
            "111111111",
        )

        "I" -> listOf(
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
            "1111111",
        )

        "J" -> listOf(
            "11111111",
            "11111111",
            "11111111",
            "11111111",
            "11111111",
            "11111111",
            "",
            "00000111",
            "00000111",
            "00000111",
            "11111111",
            "11111111",
            "11111111",
            "11111100",
            "01111100",
        )

        else -> emptyList()
    }
}

@Composable
private fun ZoneSeatGrid(
    zone: String,
    activeZone: String,
    mineRow: Int,
    mineCol: Int,
    modifier: Modifier = Modifier,
) {
    val pattern = getZoneSeatPattern(zone)
    val maximumColumns = pattern.maxOfOrNull { it.length } ?: 0
    val seatSize = 5.dp
    val horizontalGap = 2.dp
    val rowWidth = (maximumColumns * 7).dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = zone,
            fontSize = 8.sp,
            fontWeight = if (zone == activeZone) FontWeight.Bold else FontWeight.Medium,
            color = if (zone == activeZone) Color(0xFF0062FF) else Color(0xFF64748B),
        )

        Spacer(modifier = Modifier.height(8.dp))

        var actualSeatRow = 0

        pattern.forEach { rowPattern ->
            if (rowPattern.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Row(
                    modifier = Modifier.width(rowWidth),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    rowPattern.forEachIndexed { column, value ->
                        val seatModifier = Modifier
                            .padding(horizontal = (horizontalGap / 2))
                            .size(seatSize)

                        if (value == '1') {
                            val isMySeat =
                                zone == activeZone &&
                                        actualSeatRow == mineRow &&
                                        column == mineCol

                            Box(
                                modifier = seatModifier.background(
                                    color = if (isMySeat) {
                                        Color(0xFF0062FF)
                                    } else {
                                        Color(0xFFE2E8F0)
                                    },
                                    shape = RoundedCornerShape(1.dp),
                                ),
                            )
                        } else {
                            Spacer(modifier = seatModifier)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                actualSeatRow++
            }
        }
    }
}

@Composable
private fun ChapelSeatMap(
    activeZone: String,
    mineRow: Int,
    mineCol: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            listOf("A", "B", "C", "D", "E").forEach { zone ->
                ZoneSeatGrid(
                    zone = zone,
                    activeZone = activeZone,
                    mineRow = mineRow,
                    mineCol = mineCol,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            listOf("F", "G", "H", "I", "J").forEach { zone ->
                ZoneSeatGrid(
                    zone = zone,
                    activeZone = activeZone,
                    mineRow = mineRow,
                    mineCol = mineCol,
                )
            }
        }
    }
}

@Composable
private fun SeatMapWrap(
    seatInfo: SeatInfo,
    pulpitLabel: String,
    calloutText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFBFC), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .padding(start = 12.dp, end = 12.dp, top = 18.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(28.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = pulpitLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 2.sp,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ChapelSeatMap(
            activeZone = seatInfo.zone,
            mineRow = seatInfo.row,
            mineCol = seatInfo.col,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "↑",
                color = Color(0xFF0062FF),
                fontSize = 18.sp,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = calloutText,
                color = Color(0xFF0062FF),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun ChapelSeatMapCard(
    chapelData: DashboardChapelData,
    modifier: Modifier = Modifier,
) {
    val seatParts = chapelData.seat
        .split("-")
        .map { it.trim() }

    val zone = seatParts
        .getOrNull(0)
        ?.uppercase()
        .orEmpty()

    val rowNumber = seatParts
        .getOrNull(1)
        ?.toIntOrNull()
        ?: 1

    val columnNumber = seatParts
        .getOrNull(2)
        ?.toIntOrNull()
        ?: 1

    val seatInfo = SeatInfo(
        code = chapelData.seat,
        floor = getSeatLocationFloor(zone),
        building = chapelData.seatDescription
            .substringBefore(" · ")
            .ifBlank { "한경직기념관" },
        zone = zone,
        row = (rowNumber - 1).coerceAtLeast(0),
        col = (columnNumber - 1).coerceAtLeast(0),
        helperText = "",
    )

    val guideText = if (chapelData.seat.isBlank()) {
        "참고용: 배정된 좌석 정보가 없습니다."
    } else {
        "참고용: ${zone}구역 ${rowNumber}번째 줄 ${columnNumber}번째 자리예요."
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        SeatMapWrap(
            seatInfo = seatInfo,
            pulpitLabel = "STAGE",
            calloutText = "이 자리예요",
        )

        HelperText(
            text = guideText,
        )
    }
}

@Composable
private fun HelperText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6B7280),
            lineHeight = (13 * 1.5).sp,
        )
    }
}

@Composable
private fun MySeatLocationScreen(
    seatCode: String = "B-12",
    seatFloor: String = "1층 앞자리",
    seatBuilding: String = "한경직기념관",
    seatZone: String = "B",
    seatRow: Int = 2,
    seatCol: Int = 3,
    helperText: String = "입구에서 좌측으로 입장해 앞으로 3번째 줄 네 번째 자리예요",
    pulpitLabel: String = "설교단",
    calloutText: String = "이 자리에요",
    onBackClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val seatInfo = SeatInfo(
        code = seatCode,
        floor = seatFloor,
        building = seatBuilding,
        zone = seatZone,
        row = seatRow,
        col = seatCol,
        helperText = helperText,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        SeatLocationHeader(
            onBackClick = onBackClick,
            onInfoClick = onInfoClick,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SeatInfoCard(seatInfo = seatInfo)

            SeatMapWrap(
                seatInfo = seatInfo,
                pulpitLabel = pulpitLabel,
                calloutText = calloutText,
            )

            HelperText(text = seatInfo.helperText)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ChapelSeatLocationSamplePreview() {
    MySeatLocationScreen(
        seatCode = "E-14-4",
        seatFloor = "1층",
        seatBuilding = "한경직기념관",
        seatZone = "E",
        seatRow = 13,
        seatCol = 3,
        helperText = "E구역 14번째 줄 4번째 자리예요",
        pulpitLabel = "설교단",
        calloutText = "이 자리에요",
    )
}