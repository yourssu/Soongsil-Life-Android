package com.yourssu.soongsil.screen.grade.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import com.yourssu.soongsil.ui.theme.SoongsilPalette

// 성적 상세 화면의 상단 헤더를 표시합니다.
@Composable
fun GradeDetailHeader(
    modifier: Modifier = Modifier
        .fillMaxWidth(),
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .padding(end = 12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = "뒤로가기",
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "성적 상세",
            fontSize = 18.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.4).sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable { onRefreshClick() }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "새로고침",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(name = "성적 상세 헤더", showBackground = true, widthDp = 402)
@Composable
private fun GradeDetailHeaderPreview() {
    SoongsilLifeAndroidTheme {
        Box(modifier = Modifier.padding(8.dp)) {
            GradeDetailHeader(
                onBackClick = {},
                onRefreshClick = {}
            )
        }
    }
}
