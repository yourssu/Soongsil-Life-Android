package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.screen.grade.model.SemesterTab
import com.yourssu.soongsil.ui.theme.SoongsilPalette

// 성적 학기 탭 목록을 표시합니다.
@Composable
fun SemesterTabs(
    tabs: List<SemesterTab>,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTabColor = if (isSystemInDarkTheme()) {
        SoongsilPalette.Blue600
    } else { SoongsilPalette.Gray950 }

    val inactiveTabColor = if (isSystemInDarkTheme()) {
        Color(0xFF2C2C2E)
    } else { SoongsilPalette.Gray100 }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tabs) { index, tab ->
            Box(
                modifier = Modifier
                    .background(
                        if (tab.isActive) activeTabColor else inactiveTabColor,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        onTabClick(index)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    fontSize = 13.sp,
                    fontWeight = if (tab.isActive) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (tab.isActive) Color.White else Color(0xFF8B95A1)
                )
            }
        }
    }
}
