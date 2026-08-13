package com.yourssu.soongsil.screen.dashboard.components

import androidx.compose.foundation.MarqueeDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChapelSeatCard(
    seat: String,
    seatDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = seat
        .substringBefore("-")
        .trim()
        .uppercase()

    val floor = when (zone) {
        "A", "B", "C", "D", "E" -> "1층"
        "F", "G", "H", "I", "J" -> "2층"
        else -> ""
    }

    val seatLocation = listOf(
        floor,
        zone.takeIf { it.isNotBlank() }?.let { "${it}구역" }.orEmpty(),
    )
        .filter { it.isNotBlank() }
        .joinToString(" · ")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "내 자리",
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            fontSize = 13.sp
        )
        Text(
            text = seat,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = seatLocation,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        velocity = MarqueeDefaults.Velocity * 1.25f
                    ),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                fontSize = 12.sp,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "좌석 위치 보기 →",
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ChapelAttendanceCard(
    remaining: Int,
    required: Int,
    attended: Int,
    late: Int,
    absent: Int,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "이번 학기 남은 출석",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$remaining / ${required}회",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = "${attended}회 출석 · ${late}회 지각 · ${absent}회 결석",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}
