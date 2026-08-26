package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.screen.grade.model.CourseItem
import com.yourssu.soongsil.screen.grade.model.badgeBgColor
import com.yourssu.soongsil.screen.grade.model.getGradeStyle
import com.yourssu.soongsil.screen.grade.model.gradeColor
import com.yourssu.soongsil.ui.theme.PretendardFontFamily

// 과목별 상세 성적 항목(좌측 등급 뱃지 + 우측 과목명/교수명)을 표시합니다.
@Composable
fun CourseDetailCard(
    course: CourseItem,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val gradeStyle = course.grade.getGradeStyle()
    val badgeShape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좌측 둥근 등급 뱃지
        val badgeModifier = if (gradeStyle.isOutlined) {
            Modifier
                .size(48.dp)
                .clip(badgeShape)
                .border(1.2.dp, if (isDark) Color(0xFF3A3A3C) else Color(0xFFD1D6DB), badgeShape)
        } else {
            Modifier
                .size(48.dp)
                .clip(badgeShape)
                .background(course.badgeBgColor(isDark), badgeShape)
        }

        Box(
            modifier = badgeModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = course.grade.ifBlank { "?" },
                fontSize = 16.sp,
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.ExtraBold,
                color = course.gradeColor(isDark)
            )
        }

        // 우측 과목명 및 교수명/학점
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = course.name,
                modifier = Modifier.basicMarquee(),
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            val subText = listOfNotNull(
                course.professor.takeIf { it.isNotBlank() },
                course.credit.takeIf { it.isNotBlank() }
            ).joinToString(" · ")

            Text(
                text = subText,
                modifier = Modifier.basicMarquee(),
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontFamily = PretendardFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
