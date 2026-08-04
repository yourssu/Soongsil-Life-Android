package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GpaDetailCard(
    gpa: String,
    maxGpa: String,
    credits: String,
    courseCount: String,
    rank: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A), RoundedCornerShape(20.dp))
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "총 평점 평균",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFA1A1A1),
            letterSpacing = (-0.3).sp
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = gpa,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-2.5).sp
            )
            Text(
                text = "/ $maxGpa",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA1A1A1)
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "취득 학점 $credits", fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFFA1A1A1))
            Text(text = "·", fontSize = 13.sp, color = Color(0xFF525252))
            Text(text = "과목 $courseCount", fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFFA1A1A1))
            Text(text = "·", fontSize = 13.sp, color = Color(0xFF525252))
            Text(text = "전체 석차 $rank", fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFFA1A1A1))
        }
    }
}
