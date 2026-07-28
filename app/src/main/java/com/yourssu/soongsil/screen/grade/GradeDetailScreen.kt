package com.yourssu.soongsil.screen.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.soongsil.screen.grade.components.CourseDetailCard
import com.yourssu.soongsil.screen.grade.components.GpaDetailCard
import com.yourssu.soongsil.screen.grade.components.GpaTrendChart
import com.yourssu.soongsil.screen.grade.components.SemesterTabs
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding

@Composable
@Preview
fun previewGradeDetailScreen(){
    GradeDetailScreen(modifier = Modifier)
}

// ─── Screen ───
@Composable
fun GradeDetailScreen(
    onBackClick: () -> Unit = {},
    maxGpa: String = "4.5",
    modifier: Modifier = Modifier,
    viewModel: GradeViewModel = hiltViewModel()
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getTerms()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        GradeDetailHeader(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 24.dp,
                    bottom = 20.dp + bottomBarPadding
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SemesterTabs(
                tabs = uiState.semesters,
                onTabClick = { index ->
                    viewModel.selectSemester(index)
                }
            )

            GpaDetailCard(
                gpa = uiState.gpa,
                maxGpa = maxGpa,
                credits = uiState.credits,
                courseCount = uiState.courseCount,
                rank = uiState.rank
            )

            GpaTrendChart(points = uiState.gpaPoints)

            uiState.courses.forEach { course ->
                CourseDetailCard(course = course)
            }
        }
    }
}

@Composable
fun GradeDetailHeader(onBackClick: () -> Unit) {

}
