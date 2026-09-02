package com.yourssu.soongsil.screen.grade.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import com.yourssu.soongsil.ui.theme.SoongsilPalette

// 성적 상세 화면의 상단 헤더를 표시합니다. 로딩 중일 때는 진행 상태 텍스트와 프로그레스바를 함께 렌더링합니다.
@Composable
fun GradeDetailHeader(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isLoading: Boolean = false,
    loadingText: String = "",
    currentStep: Int? = null,
    totalStep: Int? = null,
    modifier: Modifier = Modifier
) {
    val titleColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onBackground
    } else {
        SoongsilPalette.Navy900
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기 버튼
        Icon(
            painter = painterResource(R.drawable.ic_caret_left),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .size(24.dp)
                .clickable { onBackClick() },
            tint = titleColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 화면 타이틀
        Text(
            text = "성적 상세",
            fontSize = 17.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = PretendardFontFamily,
            color = titleColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 메인 대시보드 탑바 스타일의 로딩 상태 표시
        if (isLoading) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = loadingText.ifBlank { "성적 정보를 불러오는 중" },
                    color = SoongsilPalette.Slate400,
                    fontFamily = PretendardFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                if (currentStep != null && totalStep != null && totalStep > 0) {
                    LinearProgressIndicator(
                        progress = { currentStep.toFloat() / totalStep },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Color(0xFF3182F6),
                        trackColor = Color(0xFFC9E2FF)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Color(0xFF3182F6),
                        trackColor = Color(0xFFC9E2FF)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 우측 새로고침 버튼
        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable(enabled = !isLoading) { onRefreshClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "새로고침",
                modifier = Modifier.size(24.dp),
                tint = if (isLoading) SoongsilPalette.Slate300 else titleColor
            )
        }
    }
}

// ─── Previews ───

@Preview(name = "성적 상세 헤더 (기본) - Light", showBackground = true)
@Preview(name = "성적 상세 헤더 (기본) - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradeDetailHeaderPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GradeDetailHeader(
                onBackClick = {},
                onRefreshClick = {}
            )
        }
    }
}

@Preview(name = "성적 상세 헤더 (로딩 중) - Light", showBackground = true)
@Preview(name = "성적 상세 헤더 (로딩 중) - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradeDetailHeaderLoadingPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GradeDetailHeader(
                onBackClick = {},
                onRefreshClick = {},
                isLoading = true,
                loadingText = "2024년 1학기 성적 확인 중 (2/5)",
                currentStep = 2,
                totalStep = 5
            )
        }
    }
}
