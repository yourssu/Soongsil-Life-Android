package com.yourssu.soongsil.screen.dashboard

import android.content.res.Configuration
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.dashboard.DashboardRefreshStep
import com.yourssu.data.dashboard.DashboardSemesterGrade
import com.yourssu.soongsil.screen.dashboard.components.DashboardChapelSection
import com.yourssu.soongsil.screen.dashboard.components.DashboardGradeDetailButton
import com.yourssu.soongsil.screen.dashboard.components.DashboardGradeSection
import com.yourssu.soongsil.screen.dashboard.components.DashboardQuickLinks
import com.yourssu.soongsil.screen.dashboard.components.DashboardRefreshPopup
import com.yourssu.soongsil.screen.dashboard.components.DashboardTopBar
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

@Composable
@Preview(name = "대시보드 - Light")
@Preview(name = "대시보드 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
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
            isGradeBlurred = false
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
            isGradeBlurred = true
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
    isGradeBlurred: Boolean = true,
    refreshStatus: DashboardRefreshStatus = DashboardRefreshStatus.HIDDEN,
    refreshStep: DashboardRefreshStep = DashboardRefreshStep.CONNECTING,
    refreshErrorMessage: String? = null,
    isPullRefreshing: Boolean = false,
    onPullToRefresh: () -> Unit = {},
    onRefreshRetryClick: () -> Unit = {},
    onGradeBlurClick: () -> Unit = {},
    onGradeDetailClick: () -> Unit = {},
    onChapelClick: () -> Unit = {},
    onGraduateClick: () -> Unit = {},
    onScholarshipClick: () -> Unit = {}
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val bottomBarPadding = LocalMainBottomBarPadding.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardTopBar()

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
                            showGraphData = !isGradeBlurred,
                            modifier = Modifier
                                .fillMaxWidth()
                                .blur(
                                    radius = if (isGradeBlurred) 14.dp else 0.dp,
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
                        onDetailClick = onChapelClick,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                    )

                    DashboardQuickLinks(
                        onGraduateClick = onGraduateClick,
                        onScholarshipClick = onScholarshipClick,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp + bottomBarPadding))
                }
            }
        }

        DashboardRefreshPopup(
            status = refreshStatus,
            step = refreshStep,
            errorMessage = refreshErrorMessage,
            onRetryClick = onRefreshRetryClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 52.dp)
        )
    }
}
