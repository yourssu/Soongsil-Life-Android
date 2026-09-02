package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.screen.grade.model.SemesterTab
import com.yourssu.soongsil.ui.theme.PretendardFontFamily

// 성적 학기 탭 목록을 표시합니다.
@Composable
fun SemesterTabs(
    tabs: List<SemesterTab>,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val activeBorderColor = if (isDark) Color(0xFF5B9DFF) else Color(0xFF0062FF)
    val activeTextColor = if (isDark) Color(0xFF5B9DFF) else Color(0xFF0062FF)
    val inactiveBgColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F4F6)
    val inactiveTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8B95A1)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tabs) { index, tab ->
            val tabShape = RoundedCornerShape(20.dp)
            val boxModifier = if (tab.isActive) {
                Modifier
                    .clip(tabShape)
                    .background(Color.Transparent)
                    .border(1.5.dp, activeBorderColor, tabShape)
            } else {
                Modifier
                    .clip(tabShape)
                    .background(inactiveBgColor, tabShape)
            }

            Box(
                modifier = boxModifier
                    .clickable { onTabClick(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    fontSize = 13.5.sp,
                    lineHeight = 16.sp,
                    fontFamily = PretendardFontFamily,
                    fontWeight = if (tab.isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (tab.isActive) activeTextColor else inactiveTextColor
                )
            }
        }
    }
}
