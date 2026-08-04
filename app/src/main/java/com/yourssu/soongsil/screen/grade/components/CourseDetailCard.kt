package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.screen.grade.model.CourseItem

@Composable
fun CourseDetailCard(
    course: CourseItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.Top)
                .padding(top = 6.dp)
                .size(8.dp)
                .background(course.dotColor, CircleShape)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp),
        ) {
            Text(
                text = course.name,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF191f28),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.credit,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFFB0B8C1),
                    maxLines = 1
                )
                Text(
                    text = course.professor,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF8B95A1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .background(course.badgeBgColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = course.grade,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = course.gradeColor
            )
        }
    }
}
