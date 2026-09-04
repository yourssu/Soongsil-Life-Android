package com.yourssu.soongsil.screen.grade

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.soongsil.screen.grade.components.CourseDetailCard
import com.yourssu.soongsil.screen.grade.components.GpaDetailCard
import com.yourssu.soongsil.screen.grade.components.GpaTrendChart
import com.yourssu.soongsil.screen.grade.components.GradeDetailHeader
import com.yourssu.soongsil.screen.grade.components.GradeRefreshStatus
import com.yourssu.soongsil.screen.grade.components.SemesterTabs
import com.yourssu.soongsil.screen.grade.model.CourseItem
import com.yourssu.soongsil.screen.grade.model.GpaPoint
import com.yourssu.soongsil.screen.grade.model.SemesterTab
import com.yourssu.soongsil.screen.grade.model.getGradeStyle
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import io.github.chlwhdtn03.data.Lms.Semester

// ─── Screen ───
// 성적 상세 화면 진입점입니다.
@Composable
fun GradeDetailScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GradeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GradeDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRefresh = viewModel::loadGradeOverview,
        onTabClick = viewModel::selectSemester,
        modifier = modifier
    )
}

// 성적 상세 화면 본문 화면입니다. 당겨서 새로고침 및 탭/평점/차트/과목목록을 렌더링합니다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeDetailContent(
    uiState: GradeUiState,
    onBackClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onTabClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val semesterGradeData = uiState.semesterGradeData
    val pullToRefreshState = rememberPullToRefreshState()
    val isPullRefreshing = uiState.refreshStatus == GradeRefreshStatus.LOADING
    val isDark = isSystemInDarkTheme()
    val dividerColor = if (isDark) Color(0xFF1E2024) else Color(0xFFF2F4F6)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 상단 헤더 (뒤로가기, 타이틀, 탑바 로딩 인디케이터, 새로고침)
            GradeDetailHeader(
                onBackClick = onBackClick,
                onRefreshClick = onRefresh,
                isLoading = isPullRefreshing,
                loadingText = uiState.refreshMessage,
                currentStep = uiState.refreshCurrentStep,
                totalStep = uiState.refreshTotalStep
            )

            // 상단 에러 안내 뱃지
            val errorMessage = uiState.errorMessage
            if (errorMessage != null && !isPullRefreshing) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "경고",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontFamily = PretendardFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 학기 데이터가 전혀 없는 최초 로딩 시 화면 중앙에 로딩 인디케이터를 표시합니다.
            if (uiState.isLoading && uiState.semesters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isPullRefreshing,
                    onRefresh = onRefresh,
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 20.dp + bottomBarPadding
                    )
                ) {
                    // 상단 학기 탭 목록
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            SemesterTabs(
                                tabs = uiState.semesters,
                                onTabClick = onTabClick
                            )
                        }
                    }

                    // 평점 평균 및 요약 (취득 학점, 학기별 석차, 전체 석차)
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            GpaDetailCard(
                                gpa = semesterGradeData.gpa,
                                maxGpa = "4.50",
                                credits = semesterGradeData.credits,
                                semesterRank = semesterGradeData.rank,
                                totalRank = semesterGradeData.totalRank
                            )
                        }
                    }

                    // 성적 추이 차트
                    if (uiState.gpaPoints.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                GpaTrendChart(points = uiState.gpaPoints)
                            }
                        }
                    }

                    // 차트와 과목 리스트 사이 구분선
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(
                            thickness = 8.dp,
                            color = dividerColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 과목별 상세 성적 리스트
                    items(semesterGradeData.courses) { course ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            CourseDetailCard(course = course)
                        }
                    }
                }
            }
            }
        }
    }
}

// ─── Previews ───

private val previewSampleCourses = listOf(
    createPreviewCourse("Academic Writing in English1", "Jessica Cahill", "2학점", "A+"),
    createPreviewCourse("Academic Writing in English1", "Jessica Cahill", "2학점", "B+"),
    createPreviewCourse("Academic Writing in English1", "Jessica Cahill", "2학점", "C+"),
    createPreviewCourse("Academic Writing in English1", "Jessica Cahill", "2학점", "D+"),
    createPreviewCourse("Academic Writing in English1", "Jessica Cahill", "2학점", "F"),
    createPreviewCourse("Academic Writing in English1", "Jessica Cahill", "2학점", "NP"),
    createPreviewCourse("Academic Writing in English1", "Jessica Cahill", "2학점", "?")
)

private fun createPreviewCourse(name: String, prof: String, credit: String, grade: String): CourseItem {
    val style = grade.getGradeStyle()
    return CourseItem(
        name = name,
        professor = prof,
        credit = credit,
        grade = grade,
        gradeColor = style.gradeColor,
        gradeDarkColor = style.gradeDarkColor,
        badgeBgColor = style.badgeBgColor,
        badgeBgDarkColor = style.badgeBgDarkColor
    )
}

private val previewSampleUiState = GradeUiState(
    semesters = listOf(
        SemesterTab("2024년 1학기", isActive = true, year = "2024", semester = Semester.FIRST),
        SemesterTab("2024년 2학기", isActive = false, year = "2024", semester = Semester.SECOND),
        SemesterTab("2025년 1학기", isActive = false, year = "2025", semester = Semester.FIRST)
    ),
    semesterGradeData = SemesterGradeUiData(
        courses = previewSampleCourses,
        gpa = "4.16",
        credits = "104 / 133",
        rank = "21 / 102",
        totalRank = "37 / 121"
    ),
    gpaPoints = listOf(
        GpaPoint("2022 1학기", 2.7f),
        GpaPoint("2022 2학기", 3.0f),
        GpaPoint("2023 1학기", 4.16f, isCurrent = true),
        GpaPoint("2023 2학기", 3.7f),
        GpaPoint("2024 1학기", 3.4f),
        GpaPoint("2024 2학기", 3.5f),
        GpaPoint("2025 1학기", 3.8f),
        GpaPoint("2025 2학기", 3.9f)
    )
)

@Preview(name = "성적 화면 (기본) - Light", showBackground = true)
@Preview(name = "성적 화면 (기본) - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradeDetailScreenPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GradeDetailContent(
                uiState = previewSampleUiState
            )
        }
    }
}

@Preview(name = "성적 화면 (탑바 로딩 중) - Light", showBackground = true)
@Preview(name = "성적 화면 (탑바 로딩 중) - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradeDetailScreenLoadingPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GradeDetailContent(
                uiState = previewSampleUiState.copy(
                    refreshStatus = GradeRefreshStatus.LOADING,
                    refreshMessage = "2024년 1학기 성적 확인 중 (2/5)",
                    refreshCurrentStep = 2,
                    refreshTotalStep = 5
                )
            )
        }
    }
}

@Preview(name = "성적 화면 (에러 뱃지 노출) - Light", showBackground = true)
@Preview(name = "성적 화면 (에러 뱃지 노출) - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradeDetailScreenErrorPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GradeDetailContent(
                uiState = previewSampleUiState.copy(
                    errorMessage = "유세인트 통신 상태가 원활하지 않아 캐시된 데이터를 표시합니다."
                )
            )
        }
    }
}

// 최초 진입 시 데이터가 없어 중앙 로딩 인디케이터와 탑바 로딩바가 함께 노출되는 화면 미리보기입니다.
@Preview(name = "성적 화면 (최초 로딩 중) - Light", showBackground = true)
@Preview(name = "성적 화면 (최초 로딩 중) - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradeDetailScreenInitialLoadingPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GradeDetailContent(
                uiState = GradeUiState(
                    semesters = emptyList(),
                    isLoading = true,
                    refreshStatus = GradeRefreshStatus.LOADING,
                    refreshMessage = "2024년 1학기 성적 확인 중",
                    refreshCurrentStep = 1,
                    refreshTotalStep = 4
                )
            )
        }
    }
}

