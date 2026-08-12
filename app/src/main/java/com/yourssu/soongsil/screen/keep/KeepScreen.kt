package com.yourssu.soongsil.screen.keep

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.keep.KeepCourse
import com.yourssu.data.keep.KeepData
import com.yourssu.soongsil.ui.components.CourseDetailBottomSheet
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepScreen(
    uiState: KeepUiState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    onPlanClick: (KeepCourse) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    var selectedCourse by remember { mutableStateOf<KeepCourse?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KeepHeader(onBackClick = onBackClick)

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.data != null -> KeepDataContent(
                    data = uiState.data,
                    errorMessage = uiState.errorMessage,
                    onRetryClick = onRetryClick,
                    onCourseClick = { selectedCourse = it },
                    bottomPadding = bottomBarPadding
                )

                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                else -> KeepStateMessage(
                    title = "장바구니 정보를 불러오지 못했어요",
                    description = uiState.errorMessage ?: "잠시 후 다시 시도해 주세요.",
                    buttonText = "다시 시도",
                    onButtonClick = onRetryClick,
                    bottomPadding = bottomBarPadding
                )
            }
        }
    }

    selectedCourse?.let { course ->
        CourseDetailBottomSheet(
            subjectName = course.subjectName,
            classification = course.classification,
            professor = course.professor,
            countLabel = "담은 인원",
            count = course.savedStudentCount,
            details = course.toDetailItems(),
            onDismissRequest = { selectedCourse = null },
            onPlanClick = {
                selectedCourse = null
                onPlanClick(course)
            }
        )
    }
}

@Composable
private fun KeepHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "수강신청 장바구니",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(48.dp))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun KeepDataContent(
    data: KeepData,
    errorMessage: String?,
    onRetryClick: () -> Unit,
    onCourseClick: (KeepCourse) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 20.dp + bottomPadding
        )
    ) {
        item {
            KeepOverviewCard(
                data = data,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (errorMessage != null) {
            item {
                KeepErrorCard(
                    errorMessage = errorMessage,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        item {
            Text(
                text = "담은 과목",
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (data.courses.isEmpty()) {
            item {
                EmptyKeepCard()
            }
        } else {
            itemsIndexed(
                items = data.courses,
                key = { index, course -> "${course.subjectCode}-${course.section}-$index" }
            ) { index, course ->
                Column {
                    KeepCourseListItem(
                        course = course,
                        onClick = { onCourseClick(course) }
                    )
                    if (index < data.courses.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeepOverviewCard(
    data: KeepData,
    modifier: Modifier = Modifier
) {
    val periodInfo = data.period.toKeepPeriodInfo()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = periodInfo.semester,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "신청 기간",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = periodInfo.applicationPeriod,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            KeepInfoRow(label = "예약 상태", value = data.reservationStatus.orDash())
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp, bottom = 18.dp),
                color = MaterialTheme.colorScheme.outline
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeepSummaryColumn(
                    label = "총 과목",
                    value = data.totalCourseCount.orDash(),
                    modifier = Modifier.weight(1f)
                )
                KeepSummaryColumn(
                    label = "총 학점",
                    value = data.totalCredits.orDash(),
                    modifier = Modifier.weight(1f)
                )
                KeepSummaryColumn(
                    label = "신청 가능 학점",
                    value = data.availableCredits.orDash(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KeepSummaryColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun KeepCourseListItem(
    course: KeepCourse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "우선순위",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = course.priority.toPriorityText(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = course.subjectName.orDash(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = listOf(course.classification, course.professor)
                    .filter(String::isNotBlank)
                    .joinToString(" · ")
                    .orDash(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = course.savedStudentCount.orDash(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "담은 인원",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

private fun KeepCourse.toDetailItems(): List<Pair<String, String>> = listOf(
    "우선순위" to priority.toPriorityText(),
    "수업시간" to schedule,
    "시간 / 학점" to hoursCredits,
    "강의계획서 정보" to plan,
    "다전공 이수구분" to multiMajorClassification,
    "공학인증" to engineeringCertification,
    "교과영역" to curriculumArea,
    "과목번호" to subjectCode,
    "분반" to section,
    "신청일시" to applicationDate,
    "비고" to note
)

@Composable
private fun KeepInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.38f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.62f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun KeepErrorCard(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "저장된 정보를 표시하고 있어요",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Button(onClick = onRetryClick) {
                Text(text = "다시 시도")
            }
        }
    }
}

@Composable
private fun EmptyKeepCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "장바구니에 담은 과목이 없어요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun KeepStateMessage(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 32.dp,
            end = 32.dp,
            bottom = bottomPadding
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Button(onClick = onButtonClick) {
                    Text(text = buttonText)
                }
            }
        }
    }
}

private fun String.orDash(): String = ifBlank { "-" }

private fun String.toPriorityText(): String {
    val priority = trim().toIntOrNull()?.toString() ?: trim()
    return priority.takeIf(String::isNotBlank)?.let { "${it}번" } ?: "-"
}

private data class KeepPeriodInfo(
    val semester: String,
    val applicationPeriod: String
)

private fun String.toKeepPeriodInfo(): KeepPeriodInfo {
    val semester = Regex("""\d{4}학년도\s+(?:1|2|여름|겨울)학기""")
        .find(this)
        ?.value
        .orEmpty()
        .orDash()
    val applicationPeriod = substringAfter(":", missingDelimiterValue = "")
        .trim()
        .orDash()
    return KeepPeriodInfo(
        semester = semester,
        applicationPeriod = applicationPeriod
    )
}

private val previewKeepData = KeepData(
    period = "2026학년도 2학기 예비수강신청(장바구니): 2026.08.03 ~ 2026.08.10",
    reservationStatus = "예약 상태 : 신청 가능",
    totalCourseCount = "2",
    totalCredits = "6",
    availableCredits = "19",
    courses = listOf(
        KeepCourse(
            priority = "1",
            plan = "주전공",
            classification = "전공선택",
            multiMajorClassification = "복수전공",
            engineeringCertification = "인증",
            curriculumArea = "전공",
            subjectCode = "21500123",
            subjectName = "모바일프로그래밍",
            section = "01",
            professor = "김교수",
            hoursCredits = "3 / 3",
            schedule = "월 10:30-11:45, 수 10:30-11:45",
            applicationDate = "2026.08.05 10:12:30",
            note = "-",
            savedStudentCount = "28"
        )
    )
)

@Preview(name = "내용 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "내용 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun KeepScreenContentPreview() {
    SoongsilLifeAndroidTheme {
        KeepScreen(
            uiState = KeepUiState(data = previewKeepData, isLoading = false),
            onBackClick = {},
            onRefresh = {},
            onRetryClick = {},
            onPlanClick = {}
        )
    }
}

@Preview(name = "과목 상세 바텀시트 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "과목 상세 바텀시트 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun KeepCourseBottomSheetPreview() {
    SoongsilLifeAndroidTheme {
        val course = previewKeepData.courses.first()
        CourseDetailBottomSheet(
            subjectName = course.subjectName,
            classification = course.classification,
            professor = course.professor,
            countLabel = "담은 인원",
            count = course.savedStudentCount,
            details = course.toDetailItems(),
            onDismissRequest = {},
            onPlanClick = {}
        )
    }
}

@Preview(name = "빈 상태 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "빈 상태 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun KeepScreenEmptyPreview() {
    SoongsilLifeAndroidTheme {
        KeepScreen(
            uiState = KeepUiState(
                data = previewKeepData.copy(
                    totalCourseCount = "0",
                    totalCredits = "0",
                    courses = emptyList()
                ),
                isLoading = false
            ),
            onBackClick = {},
            onRefresh = {},
            onRetryClick = {},
            onPlanClick = {}
        )
    }
}

@Preview(name = "오류 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "오류 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun KeepScreenErrorPreview() {
    SoongsilLifeAndroidTheme {
        KeepScreen(
            uiState = KeepUiState(
                isLoading = false,
                errorMessage = "네트워크 연결을 확인해 주세요."
            ),
            onBackClick = {},
            onRefresh = {},
            onRetryClick = {},
            onPlanClick = {}
        )
    }
}

@Preview(name = "로딩 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "로딩 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun KeepScreenLoadingPreview() {
    SoongsilLifeAndroidTheme {
        KeepScreen(
            uiState = KeepUiState(),
            onBackClick = {},
            onRefresh = {},
            onRetryClick = {},
            onPlanClick = {}
        )
    }
}
