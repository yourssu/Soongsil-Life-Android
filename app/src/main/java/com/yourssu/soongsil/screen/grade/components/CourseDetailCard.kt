package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.screen.grade.model.CourseItem
import com.yourssu.soongsil.screen.grade.model.badgeBgColor
import com.yourssu.soongsil.screen.grade.model.dodColor
import com.yourssu.soongsil.screen.grade.model.gradeColor
import com.yourssu.soongsil.ui.theme.SoongsilPalette

// 과목별 상세 성적 카드를 표시합니다.
@Composable
fun CourseDetailCard(
    course: CourseItem,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDarkTheme) Color(0xFF1C1C1E) else SoongsilPalette.White, RoundedCornerShape(16.dp))
            .border(1.dp, if (isDarkTheme) Color(0xFF2C2C2E) else SoongsilPalette.Slate100 , RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.Top)
                .padding(top = 6.dp)
                .size(8.dp)
                .background(course.dodColor(isSystemInDarkTheme()), CircleShape)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp),
        ) {
            Text(
                text = course.name,
                modifier = Modifier
                    .basicMarquee(),
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkTheme) Color(0xFFF5F5F5) else SoongsilPalette.Navy700,
                maxLines = 1
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.credit,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = if (isDarkTheme) Color(0xFF8A8A8E) else SoongsilPalette.Slate200,
                    maxLines = 1
                )
                Text(
                    text = course.professor,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = if (isDarkTheme) Color(0xFF8A8A8E) else SoongsilPalette.Slate400,
                    maxLines = 1
                )
            }
        }
        Box(
            modifier = Modifier
                .background(course.badgeBgColor(isDarkTheme), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = course.grade,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = course.gradeColor(isDarkTheme)
            )
        }
    }
}
