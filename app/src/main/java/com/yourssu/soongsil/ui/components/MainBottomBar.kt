package com.yourssu.soongsil.ui.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

enum class MainTab(
    val label: String,
    @DrawableRes val iconRes: Int?
) {
    HOME("홈", R.drawable.ic_tabbar_house),
    TIMETABLE("시간표", null),
//    TODO 알림 기능 구현시 복원
//    NOTIFICATIONS("알림", R.drawable.ic_tabbar_bell),
    MY_PAGE("마이", R.drawable.ic_tabbar_user),
}

@Composable
fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(9999.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(9999.dp))
                .padding(8.dp)
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                            shape = RoundedCornerShape(9999.dp)
                        )
                        .clickable(role = Role.Tab) { onTabSelected(tab) }
                        .padding(top = 8.dp),

                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = tab.painter(),
                        contentDescription = tab.label,
                        modifier = Modifier.size(if (tab == MainTab.TIMETABLE) 20.dp else 18.dp),
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = tab.label,
                        fontSize = if (isSelected) 12.sp else 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainTab.painter(): Painter = iconRes?.let { painterResource(it) }
    ?: rememberVectorPainter(Icons.Outlined.CalendarMonth)

@Preview(name = "Light", showBackground = true, widthDp = 402)
@Preview(
    name = "Dark",
    showBackground = true,
    widthDp = 402,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MainBottomBarPreview() {
    SoongsilLifeAndroidTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            MainBottomBar(
                selectedTab = MainTab.HOME,
                onTabSelected = {}
            )
        }
    }
}
