package com.yourssu.soongsil.screen.dashboard

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.dashboard.AdvertisementData
import com.yourssu.data.dashboard.DashboardRefreshStep
import com.yourssu.data.dashboard.DashboardSemesterGrade
import com.yourssu.soongsil.screen.dashboard.components.AdvertisementBanner
import com.yourssu.soongsil.screen.dashboard.components.DashboardChapelSection
import com.yourssu.soongsil.screen.dashboard.components.DashboardGradeDetailButton
import com.yourssu.soongsil.screen.dashboard.components.DashboardGradeSection
import com.yourssu.soongsil.screen.dashboard.components.DashboardQuickLinks
import com.yourssu.soongsil.screen.dashboard.components.DashboardTopBar
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

@Composable
@Preview(name = "대시보드 전체 (광고 포함) - Light", showBackground = true)
@Preview(name = "대시보드 전체 (광고 포함) - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun DashboardScreenPreview() {
    SoongsilLifeAndroidTheme {
        DashboardScreen(
            gpa = "4.06",
            earnedCredits = "104",
            semesterRank = "21/102",
            totalRank = "37/121",
            semesterGrades = listOf(
                DashboardSemesterGrade("1-1", "2.70"),
                DashboardSemesterGrade("1-2", "3.02"),
                DashboardSemesterGrade("2-1", "3.48"),
                DashboardSemesterGrade("2-2", "3.43"),
                DashboardSemesterGrade("3-1", "3.31"),
                DashboardSemesterGrade("3-2", "3.36"),
                DashboardSemesterGrade("4-1", "3.52"),
                DashboardSemesterGrade("4-2", "3.60")
            ),
            chapelSeat = "A-1-2",
            chapelRequired = 8,
            chapelAttended = 3,
            chapelYear = "2026",
            chapelSemester = "1학기",
            isGradeBlurred = false,
            advertisement = AdvertisementData(
                imageUrl = "https://example.com/banner.png",
                link = "https://example.com",
                success = true
            )
        )
    }
}

@Composable
@Preview(name = "성적 블러 - Light")
@Preview(name = "성적 블러 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun DashboardGradeBlurPreview() {
    SoongsilLifeAndroidTheme {
        DashboardScreen(
            gpa = "4.06",
            earnedCredits = "104",
            semesterRank = "21/102",
            totalRank = "37/121",
            semesterGrades = listOf(
                DashboardSemesterGrade("1-1", "2.70"),
                DashboardSemesterGrade("2-1", "3.48"),
                DashboardSemesterGrade("3-1", "3.31"),
                DashboardSemesterGrade("4-1", "3.52")
            ),
            chapelYear = "2026",
            chapelSemester = "1학기",
            isGradeBlurred = true,
            refreshStatus = DashboardRefreshStatus.LOADING,
            refreshStep = DashboardRefreshStep.TWO_COMPLETED
        )
    }
}

// 채플 정보가 없는 경우의 대시보드 프리뷰입니다.
@Composable
@Preview(name = "대시보드 채플 없음 - Light")
@Preview(name = "대시보드 채플 없음 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun DashboardScreenNoChapelPreview() {
    SoongsilLifeAndroidTheme {
        DashboardScreen(
            gpa = "4.06",
            earnedCredits = "104",
            semesterRank = "21/102",
            totalRank = "37/121",
            chapelYear = "2026",
            chapelSemester = "1학기",
            chapelSeat = "",
            chapelRequired = 0,
            chapelAttended = 0,
            isGradeBlurred = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    gpa: String = "",
    maxGpa: String = "4.50",
    earnedCredits: String = "",
    requiredCredits: String = "133",
    semesterRank: String = "",
    totalRank: String = "",
    semesterGrades: List<DashboardSemesterGrade> = emptyList(),
    chapelSeat: String = "",
    chapelRequired: Int = 0,
    chapelAttended: Int = 0,
    chapelYear: String = "",
    chapelSemester: String = "",
    isGradeBlurred: Boolean = true,
    advertisement: AdvertisementData? = null,
    refreshStatus: DashboardRefreshStatus = DashboardRefreshStatus.HIDDEN,
    refreshStep: DashboardRefreshStep = DashboardRefreshStep.CONNECTING,
    isPullRefreshing: Boolean = false,
    onPullToRefresh: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onGradeBlurClick: () -> Unit = {},
    onGradeDetailClick: () -> Unit = {},
    onChapelClick: () -> Unit = {},
    onGraduateClick: () -> Unit = {},
    onScholarshipClick: () -> Unit = {},
    onAdvertisementClick: (String) -> Unit = {}
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val bottomBarPadding = LocalMainBottomBarPadding.current
    // 성적 공개 시 블러와 실제 데이터를 함께 서서히 표시합니다.
    val gradeBlurRadius by animateDpAsState(
        targetValue = if (isGradeBlurred) 14.dp else 0.dp,
        animationSpec = tween(durationMillis = GRADE_REVEAL_DURATION_MILLIS),
        label = "dashboardGradeBlur"
    )
    val gradeDataAlpha by animateFloatAsState(
        targetValue = if (isGradeBlurred) 0f else 1f,
        animationSpec = tween(durationMillis = GRADE_REVEAL_DURATION_MILLIS),
        label = "dashboardGradeDataAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardTopBar(
                isLoading = refreshStatus == DashboardRefreshStatus.LOADING,
                loadingText = if (refreshStep == DashboardRefreshStep.CONNECTING) {
                    "로그인 중"
                } else {
                    "데이터 불러오는 중"
                },
                completedCount = refreshStep.current,
                totalCount = DashboardRefreshStep.TOTAL,
                onNotificationClick = onNotificationClick
            )

            PullToRefreshBox(
                isRefreshing = isPullRefreshing,
                onRefresh = onPullToRefresh,
                state = pullToRefreshState,
                indicator = {
                    if (!isPullRefreshing) {
                        PullToRefreshDefaults.Indicator(
                            state = pullToRefreshState,
                            isRefreshing = false,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = isGradeBlurred,
                                onClick = onGradeBlurClick
                            )
                    ) {
                        DashboardGradeSection(
                            gpa = gpa.ifBlank { "-" },
                            maxGpa = maxGpa,
                            earnedCredits = earnedCredits.ifBlank { "-" },
                            requiredCredits = requiredCredits,
                            semesterRank = semesterRank.ifBlank { "-" },
                            totalRank = totalRank.ifBlank { "-" },
                            semesterGrades = semesterGrades,
                            showSensitiveData = !isGradeBlurred,
                            showGraphData = !isGradeBlurred,
                            sensitiveDataAlpha = gradeDataAlpha,
                            modifier = Modifier
                                .fillMaxWidth()
                                .blur(
                                    radius = gradeBlurRadius,
                                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                        if (isGradeBlurred) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "한번 눌러서 블러 해제",
                                    modifier = Modifier
                                        .background(
                                            color = Color.White.copy(alpha = 1f),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .padding(horizontal = 18.dp, vertical = 10.dp),
                                    color = Color.Black.copy(alpha = 0.5f),
                                    fontFamily = PretendardFontFamily,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    DashboardGradeDetailButton(
                        onClick = onGradeDetailClick,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    DashboardChapelSection(
                        seat = chapelSeat.ifBlank { "-" },
                        totalClasses = chapelRequired,
                        attended = chapelAttended,
                        year = chapelYear,
                        semester = chapelSemester,
                        onDetailClick = onChapelClick,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                    )

                    DashboardQuickLinks(
                        onGraduateClick = onGraduateClick,
                        onScholarshipClick = onScholarshipClick,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                    )

                    // 인앱 홍보 광고 배너
                    advertisement?.takeIf { it.success && it.imageUrl.isNotBlank() }?.let { ad ->
                        AdvertisementBanner(
                            imageUrl = ad.imageUrl,
                            onClick = { onAdvertisementClick(ad.link) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp + bottomBarPadding))
                }
            }
        }

    }
}

private const val GRADE_REVEAL_DURATION_MILLIS = 450
