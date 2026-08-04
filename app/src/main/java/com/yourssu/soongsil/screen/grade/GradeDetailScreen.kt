package com.yourssu.soongsil.screen.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    modifier: Modifier = Modifier,
    viewModel: GradeViewModel = hiltViewModel()
) {
    val maxGpa = "4.5"
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gradeData = uiState.gradeData
    var includeSeasonSemester by rememberSaveable { mutableStateOf(true) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        GradeDetailHeader(onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 24.dp,
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
                    gpa = gradeData.gpa,
                    maxGpa = maxGpa,
                    credits = gradeData.credits,
                    courseCount = gradeData.courseCount,
                    rank = gradeData.rank
                )
            }

            item {
                GpaTrendChart(
                    points = if(includeSeasonSemester) uiState.gpaPoints
                    else uiState.gpaPoints.filter {
                        !(it.semester.contains("여름") || it.semester.contains("겨울"))
                    },
                    includeSeasonSemester = includeSeasonSemester,
                    onIncludeSeasonSemesterChange = { includeSeasonSemester = it }
                )
            }

            items(gradeData.courses) { course ->
                CourseDetailCard(course = course)
            }
        }
    }
}

@Composable
fun GradeDetailHeader(onBackClick: () -> Unit) {

}
