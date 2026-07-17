package com.yourssu.soongsil.screen.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yourssu.soongsil.life.screen.grade.model.CourseItem
import com.yourssu.soongsil.life.screen.grade.model.GpaPoint
import com.yourssu.soongsil.life.screen.grade.model.SemesterTab
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
    semesters: List<SemesterTab> = listOf(
        SemesterTab("2025년 1학기", isActive = true),
        SemesterTab("2024년 2학기"),
        SemesterTab("2024년 1학기"),
        SemesterTab("2023년 2학기")
    ),
    gpaPoints: List<GpaPoint> = listOf(
        GpaPoint("24-1", 3.2f),
        GpaPoint("24-2", 3.5f),
        GpaPoint("25-1", 3.7f),
        GpaPoint("25-2", 3.87f, isCurrent = true)
    ),
    courses: List<CourseItem> = listOf(
        CourseItem("비전채플", "박영수", "0.5학점", "P", Color(0xFF0062FF), Color(0xFF0062FF), Color(0xFFE6F0FF)),
        CourseItem("CTE for IT, Engineering", "최민지", "3.0학점", "A+", Color(0xFF059669), Color(0xFF059669), Color(0xFFECFDF5)),
        CourseItem("인간관계론", "이준호", "2.0학점", "A-", Color(0xFF0062FF), Color(0xFF0062FF), Color(0xFFE6F0FF)),
        CourseItem("디지털미디어원리", "김서연", "3.0학점", "A-", Color(0xFF0062FF), Color(0xFF0062FF), Color(0xFFE6F0FF)),
        CourseItem("데이터베이스", "전지훈", "3.0학점", "B+", Color(0xFFD97706), Color(0xFFD97706), Color(0xFFFFF7ED))
    ),
    gpa: String = "3.87",
    maxGpa: String = "4.5",
    credits: String = "11.5",
    courseCount: String = "5",
    rank: String = "12위",
    modifier: Modifier = Modifier
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current

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
                tabs = semesters,
                onTabClick = { /* handle tab change */ }
            )

            GpaDetailCard(
                gpa = gpa,
                maxGpa = maxGpa,
                credits = credits,
                courseCount = courseCount,
                rank = rank
            )

            GpaTrendChart(points = gpaPoints)

            courses.forEach { course ->
                CourseDetailCard(course = course)
            }
        }
    }
}

@Composable
fun GradeDetailHeader(onBackClick: () -> Unit) {

}
