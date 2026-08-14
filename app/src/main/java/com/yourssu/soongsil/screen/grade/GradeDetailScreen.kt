package com.yourssu.soongsil.screen.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.soongsil.screen.grade.components.CourseDetailCard
import com.yourssu.soongsil.screen.grade.components.GpaDetailCard
import com.yourssu.soongsil.screen.grade.components.GpaTrendChart
import com.yourssu.soongsil.screen.grade.components.GradeDetailHeader
import com.yourssu.soongsil.screen.grade.components.GradeRefreshPopup
import com.yourssu.soongsil.screen.grade.components.SemesterTabs
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding

// 성적 상세 화면 미리보기를 표시합니다.
@Composable
@Preview
fun previewGradeDetailScreen(){
    GradeDetailScreen(modifier = Modifier)
}

// ─── Screen ───
// 성적 상세 화면을 표시합니다.
@Composable
fun GradeDetailScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GradeViewModel = hiltViewModel()
) {
    val maxGpa = "4.5"
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val semesterGradeData = uiState.semesterGradeData
    var includeSeasonSemester by rememberSaveable { mutableStateOf(true) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            GradeDetailHeader(
                onBackClick = onBackClick,
                onRefreshClick = viewModel::loadGradeOverview
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                // 대시보드처럼 목록 끝에 바텀바 높이만큼 스크롤 여백을 둡니다.
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 20.dp + bottomBarPadding
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SemesterTabs(
                        tabs = uiState.semesters,
                        onTabClick = { index ->
                            viewModel.selectSemester(index)
                        }
                    )
                }

                item {
                    GpaDetailCard(
                        gpa = semesterGradeData.gpa,
                        maxGpa = maxGpa,
                        credits = semesterGradeData.credits,
                        courseCount = semesterGradeData.courseCount,
                        rank = semesterGradeData.rank
                    )
                }

                // 조회된 학기가 2개 이상인 경우 성적 추이 차트를 표시한다.
                if (uiState.gpaPoints.size >= 2) {
                    item {
                        GpaTrendChart(
                            points = if (includeSeasonSemester) uiState.gpaPoints
                            else uiState.gpaPoints.filter {
                                !(it.semester.contains("여름") || it.semester.contains("겨울"))
                            },
                            includeSeasonSemester = includeSeasonSemester,
                            onIncludeSeasonSemesterChange = { includeSeasonSemester = it }
                        )
                    }
                }

                items(semesterGradeData.courses) { course ->
                    CourseDetailCard(course = course)
                }
            }
        }
        GradeRefreshPopup(
            status = uiState.refreshStatus,
            message = uiState.refreshMessage,
            currentStep = uiState.refreshCurrentStep,
            totalStep = uiState.refreshTotalStep,
            errorMessage = uiState.errorMessage,
            onRetryClick = {
                viewModel.loadGradeOverview()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 52.dp)
        )
    }
}
