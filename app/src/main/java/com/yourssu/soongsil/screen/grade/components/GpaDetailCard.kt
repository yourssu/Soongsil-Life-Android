package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.ui.theme.PretendardFontFamily

// 선택된 학기의 평점 및 요약 정보(취득 학점, 학기별 석차, 전체 석차)를 표시합니다.
@Composable
fun GpaDetailCard(
    gpa: String,
    maxGpa: String = "4.50",
    credits: String,
    semesterRank: String = "-",
    totalRank: String = "-",
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val primaryColor = if (isDark) Color(0xFF4D96FF) else Color(0xFF0062FF)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 평점 평균 헤더
        Column {
            Text(
                text = "평점 평균",
                fontSize = 14.sp,
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = gpa.ifBlank { "-" },
                    fontSize = 36.sp,
                    fontFamily = PretendardFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "/ $maxGpa",
                    fontSize = 16.sp,
                    fontFamily = PretendardFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        // 세부 항목 리스트 (취득 학점, 학기별 석차, 전체 석차)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GpaSummaryRow(
                label = "취득 학점",
                value = credits
            )
            GpaSummaryRow(
                label = "학기별 석차",
                value = semesterRank
            )
            GpaSummaryRow(
                label = "전체 석차",
                value = totalRank
            )
        }
    }
}

// 요약 항목 행 (라벨 - 수치)
@Composable
private fun GpaSummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // "21 / 102" 또는 "104 / 133" 처럼 슬래시(/)가 있는 경우 앞부분을 볼드로 분리
        val parts = value.split("/").map { it.trim() }
        if (parts.size >= 2) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = parts[0],
                    fontSize = 14.sp,
                    fontFamily = PretendardFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = " / ${parts[1]}",
                    fontSize = 13.sp,
                    fontFamily = PretendardFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = value.ifBlank { "-" },
                fontSize = 14.sp,
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
