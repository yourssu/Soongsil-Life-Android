package com.yourssu.soongsil.screen.dashboard.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.dashboard.DashboardRefreshStep
import com.yourssu.soongsil.screen.dashboard.DashboardRefreshStatus
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

@Composable
fun DashboardRefreshPopup(
    status: DashboardRefreshStatus,
    step: DashboardRefreshStep,
    errorMessage: String?,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = status != DashboardRefreshStatus.HIDDEN,
        modifier = modifier.fillMaxWidth(),
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.84f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RefreshStatusIcon(status = status)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = status.title(),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (status == DashboardRefreshStatus.LOADING) {
                            Text(
                                text = "${step.current}/${DashboardRefreshStep.TOTAL}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = status to step,
                        label = "dashboardRefreshMessage"
                    ) { (currentStatus, currentStep) ->
                        Text(
                            text = currentStatus.description(currentStep, errorMessage),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    when (status) {
                        DashboardRefreshStatus.LOADING -> {
                            val progress = step.current.toFloat() / DashboardRefreshStep.TOTAL
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        DashboardRefreshStatus.ERROR -> {
                            TextButton(
                                onClick = onRetryClick,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "다시 시도")
                            }
                        }

                        DashboardRefreshStatus.HIDDEN,
                        DashboardRefreshStatus.SUCCESS -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshStatusIcon(status: DashboardRefreshStatus) {
    val color = when (status) {
        DashboardRefreshStatus.SUCCESS -> MaterialTheme.colorScheme.tertiary
        DashboardRefreshStatus.ERROR -> MaterialTheme.colorScheme.error
        DashboardRefreshStatus.HIDDEN,
        DashboardRefreshStatus.LOADING -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            DashboardRefreshStatus.LOADING -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = color,
                strokeWidth = 2.dp
            )

            DashboardRefreshStatus.SUCCESS -> RefreshIcon(
                imageVector = Icons.Outlined.CheckCircle,
                color = color
            )

            DashboardRefreshStatus.ERROR -> RefreshIcon(
                imageVector = Icons.Outlined.ErrorOutline,
                color = color
            )

            DashboardRefreshStatus.HIDDEN -> Unit
        }
    }
}

@Composable
private fun RefreshIcon(
    imageVector: ImageVector,
    color: Color
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = color
    )
}

private fun DashboardRefreshStatus.title(): String = when (this) {
    DashboardRefreshStatus.HIDDEN,
    DashboardRefreshStatus.LOADING -> "새로고침 중"

    DashboardRefreshStatus.SUCCESS -> "최신 정보로 업데이트했어요"
    DashboardRefreshStatus.ERROR -> "정보를 업데이트하지 못했어요"
}

private fun DashboardRefreshStatus.description(
    step: DashboardRefreshStep,
    errorMessage: String?
): String = when (this) {
    DashboardRefreshStatus.HIDDEN -> ""
    DashboardRefreshStatus.LOADING -> step.description()
    DashboardRefreshStatus.SUCCESS -> "새로 불러온 정보를 화면에 반영했어요"
    DashboardRefreshStatus.ERROR -> errorMessage ?: "잠시 후 다시 시도해 주세요"
}

private fun DashboardRefreshStep.description(): String = when (this) {
    DashboardRefreshStep.CONNECTING -> "LMS에 연결"
    DashboardRefreshStep.STUDENT_INFO -> "이름과 학과 정보"
    DashboardRefreshStep.GRADES -> "학기별 성적 정보"
    DashboardRefreshStep.CHAPEL -> "채플 좌석과 출석 정보"
}

@Preview(name = "불러오는 중")
@Preview(name = "불러오는 중 - 다크", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardRefreshPopupPreview() {
    SoongsilLifeAndroidTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            DashboardRefreshPopup(
                status = DashboardRefreshStatus.LOADING,
                step = DashboardRefreshStep.GRADES,
                errorMessage = null,
                onRetryClick = {}
            )
        }
    }
}
