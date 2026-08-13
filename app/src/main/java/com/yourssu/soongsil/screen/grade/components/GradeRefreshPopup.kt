package com.yourssu.soongsil.screen.grade.components

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
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

enum class GradeRefreshStatus {
    HIDDEN,
    LOADING,
    SUCCESS,
    ERROR
}

// 성적 정보 갱신 상태를 상단 팝업으로 표시합니다.
@Composable
fun GradeRefreshPopup(
    status: GradeRefreshStatus,
    message: String,
    currentStep: Int?,
    totalStep: Int?,
    errorMessage: String?,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = status != GradeRefreshStatus.HIDDEN,
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
                        GradeRefreshStatusIcon(status = status)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = status.title(),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (status == GradeRefreshStatus.LOADING && currentStep != null && totalStep != null) {
                            Text(
                                text = "$currentStep/$totalStep",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = Triple(status, message, errorMessage),
                        label = "gradeRefreshMessage"
                    ) { (currentStatus, currentMessage, currentErrorMessage) ->
                        Text(
                            text = currentStatus.description(
                                message = currentMessage,
                                errorMessage = currentErrorMessage
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    when (status) {
                        GradeRefreshStatus.LOADING -> {
                            val progress = if (currentStep != null && totalStep != null && totalStep > 0) {
                                currentStep.toFloat() / totalStep
                            } else {
                                0f
                            }
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        GradeRefreshStatus.ERROR -> {
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

                        GradeRefreshStatus.HIDDEN,
                        GradeRefreshStatus.SUCCESS -> Unit
                    }
                }
            }
        }
    }
}

// 성적 갱신 상태에 맞는 아이콘을 표시합니다.
@Composable
private fun GradeRefreshStatusIcon(status: GradeRefreshStatus) {
    val color = when (status) {
        GradeRefreshStatus.SUCCESS -> MaterialTheme.colorScheme.tertiary
        GradeRefreshStatus.ERROR -> MaterialTheme.colorScheme.error
        GradeRefreshStatus.HIDDEN,
        GradeRefreshStatus.LOADING -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            GradeRefreshStatus.LOADING -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = color,
                strokeWidth = 2.dp
            )

            GradeRefreshStatus.SUCCESS -> GradeRefreshIcon(
                imageVector = Icons.Outlined.CheckCircle,
                color = color
            )

            GradeRefreshStatus.ERROR -> GradeRefreshIcon(
                imageVector = Icons.Outlined.ErrorOutline,
                color = color
            )

            GradeRefreshStatus.HIDDEN -> Unit
        }
    }
}

// 성적 갱신 팝업의 벡터 아이콘을 표시합니다.
@Composable
private fun GradeRefreshIcon(
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

// 성적 갱신 상태에 맞는 제목을 반환합니다.
private fun GradeRefreshStatus.title(): String = when (this) {
    GradeRefreshStatus.HIDDEN -> ""
    GradeRefreshStatus.LOADING -> "성적 정보를 불러오는 중"
    GradeRefreshStatus.SUCCESS -> "성적 정보를 불러왔어요"
    GradeRefreshStatus.ERROR -> "성적 정보를 불러오지 못했어요"
}

// 성적 갱신 상태에 맞는 설명 문구를 반환합니다.
private fun GradeRefreshStatus.description(
    message: String,
    errorMessage: String?
): String = when (this) {
    GradeRefreshStatus.HIDDEN -> ""
    GradeRefreshStatus.LOADING -> message
    GradeRefreshStatus.SUCCESS -> message.ifBlank { "모든 성적 정보를 화면에 반영했어요" }
    GradeRefreshStatus.ERROR -> errorMessage ?: "잠시 후 다시 시도해 주세요"
}

// 밝은 모드 성적 갱신 팝업 미리보기를 표시합니다.
@Preview(name = "성적 불러오는 중", showBackground = true)
@Composable
private fun GradeRefreshPopupLightPreview() {
    SoongsilLifeAndroidTheme(darkTheme = false) {
        GradeRefreshPopupPreviewContent()
    }
}

// 어두운 모드 성적 갱신 팝업 미리보기를 표시합니다.
@Preview(name = "성적 불러오는 중 - 다크", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun GradeRefreshPopupDarkPreview() {
    SoongsilLifeAndroidTheme(darkTheme = true) {
        GradeRefreshPopupPreviewContent()
    }
}

// 성적 갱신 팝업 미리보기 내용을 표시합니다.
@Composable
private fun GradeRefreshPopupPreviewContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        GradeRefreshPopup(
            status = GradeRefreshStatus.LOADING,
            message = "2026학년도 1학기 성적 정보를 불러오는 중",
            currentStep = 1,
            totalStep = 8,
            errorMessage = null,
            onRetryClick = {}
        )
    }
}
