package com.yourssu.soongsil.screen.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yourssu.data.dashboard.DashboardRefreshStep
import com.yourssu.data.dashboard.DashboardSemesterGrade
import com.yourssu.soongsil.screen.dashboard.components.ChapelAttendanceCard
import com.yourssu.soongsil.screen.dashboard.components.ChapelSeatCard
import com.yourssu.soongsil.screen.dashboard.components.DashboardQuickLinks
import com.yourssu.soongsil.screen.dashboard.components.DashboardRefreshPopup
import com.yourssu.soongsil.screen.dashboard.components.GpaChartCard
import com.yourssu.soongsil.screen.dashboard.components.GpaHeroCard
import com.yourssu.soongsil.screen.dashboard.components.MainHeader
import com.yourssu.soongsil.screen.dashboard.components.ProfileCard
import com.yourssu.soongsil.screen.dashboard.model.GpaBarData
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

@Composable
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun DashboardScreenPreview() {
    SoongsilLifeAndroidTheme {
        DashboardScreen(
            greetingName = "강우현",
            profileName = "강우현",
            department = "컴퓨터학부",
            studentId = "20231234",
            gpa = "3.87",
            semesterGrades = listOf(
                DashboardSemesterGrade("23-1", "3.50"),
                DashboardSemesterGrade("23-2", "3.87"),
                DashboardSemesterGrade("24-1", "4.21")
            ),
            chapelSeat = "B-12",
            chapelSeatDescription = "한경직기념관 · 월 10:30",
            chapelRemaining = 3,
            chapelRequired = 8,
            chapelAttended = 5,
            chapelLate = 0,
            chapelAbsent = 1,
            chapelProgress = 0.625f,
            refreshStatus = DashboardRefreshStatus.LOADING,
            refreshStep = DashboardRefreshStep.TWO_COMPLETED
        )
    }
}

// ─── Screen ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    greetingName: String = "",
    notificationCount: Int = 0,
    profileName: String = "",
    department: String = "",
    year: String = "",
    status: String = "",
    studentId: String = "",
    gpa: String = "",
    maxGpa: String = "4.5",
    semesterGrades: List<DashboardSemesterGrade> = emptyList(),
    chapelSeat: String = "",
    chapelSeatDescription: String = "",
    chapelRemaining: Int = 0,
    chapelRequired: Int = 0,
    chapelAttended: Int = 0,
    chapelLate: Int = 0,
    chapelAbsent: Int = 0,
    chapelProgress: Float = 0f,
    refreshStatus: DashboardRefreshStatus = DashboardRefreshStatus.HIDDEN,
    refreshStep: DashboardRefreshStep = DashboardRefreshStep.CONNECTING,
    refreshErrorMessage: String? = null,
    isPullRefreshing: Boolean = false,
    onPullToRefresh: () -> Unit = {},
    onRefreshRetryClick: () -> Unit = {},
    onGradeDetailClick: () -> Unit = {},
    onChartDetailClick: () -> Unit = {},
    onChapelClick: () -> Unit = {},
    onGraduateClick: () -> Unit = {},
    onScholarshipClick: () -> Unit = {}
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val barData = semesterGrades.mapIndexed { index, grade ->
        val maxGpaValue = maxGpa.toFloatOrNull()?.takeIf { it > 0f } ?: 4.5f
        val height = ((grade.gpa.toFloatOrNull() ?: 0f) / maxGpaValue) * 80f
        GpaBarData(
            label = grade.label,
            height = height.coerceAtLeast(8f).dp,
            isCurrent = index == semesterGrades.lastIndex,
            gpaText = grade.gpa.takeIf { index == semesterGrades.lastIndex }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            MainHeader(
                greetingName = greetingName,
                notificationCount = notificationCount
            )

            Box(
                modifier = Modifier.weight(1f)
            ) {
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
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 8.dp,
                                bottom = 16.dp + bottomBarPadding
                            ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileCard(
                            name = profileName,
                            department = department,
                            year = year,
                            status = status,
                            studentId = studentId
                        )
                        GpaHeroCard(
                            gpa = gpa.ifBlank { "-" },
                            maxGpa = maxGpa,
                            onDetailClick = onGradeDetailClick
                        )
                        GpaChartCard(
                            bars = barData,
                            onDetailClick = onChartDetailClick
                        )
                        ChapelSeatCard(
                            seat = chapelSeat.ifBlank { "-" },
                            seatDescription = chapelSeatDescription.ifBlank { "채플 정보를 불러오는 중이에요" },
                            onClick = onChapelClick
                        )
                        ChapelAttendanceCard(
                            remaining = chapelRemaining,
                            required = chapelRequired,
                            attended = chapelAttended,
                            late = chapelLate,
                            absent = chapelAbsent,
                            progress = chapelProgress,
                            onClick = onChapelClick
                        )
                        DashboardQuickLinks(
                            onGraduateClick = onGraduateClick,
                            onScholarshipClick = onScholarshipClick
                        )
                    }
                }

                DashboardRefreshPopup(
                    status = refreshStatus,
                    step = refreshStep,
                    errorMessage = refreshErrorMessage,
                    onRetryClick = onRefreshRetryClick,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}
