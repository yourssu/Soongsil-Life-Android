package com.yourssu.soongsil.screen.scholarship

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.scholarship.ScholarshipHistory
import com.yourssu.data.scholarship.TuitionHistory
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

enum class TuitionScholarshipTab(val label: String) {
    TUITION("등록금 내역"),
    SCHOLARSHIP("장학금 내역")
}

@Composable
fun ScholarshipScreen(
    modifier: Modifier = Modifier,
    initialTab: TuitionScholarshipTab = TuitionScholarshipTab.TUITION,
    tuitionHistories: List<TuitionHistory> = emptyList(),
    isTuitionLoading: Boolean = false,
    tuitionErrorMessage: String? = null,
    onTuitionRetryClick: () -> Unit = {},
    scholarshipHistories: List<ScholarshipHistory> = emptyList(),
    isScholarshipLoading: Boolean = false,
    scholarshipErrorMessage: String? = null,
    onScholarshipRetryClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var selectedTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    val bottomBarPadding = LocalMainBottomBarPadding.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScholarshipHeader(onBackClick = onBackClick)
        ScholarshipTabs(
            selectedTab = selectedTab,
            onTabClick = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        when (selectedTab) {
            TuitionScholarshipTab.TUITION -> when {
                isTuitionLoading -> HistoryLoading(modifier = Modifier.weight(1f))
                tuitionErrorMessage != null -> HistoryError(
                    message = tuitionErrorMessage,
                    onRetryClick = onTuitionRetryClick,
                    modifier = Modifier.weight(1f)
                )
                tuitionHistories.isEmpty() -> HistoryEmpty(
                    message = "등록금 내역이 없습니다.",
                    modifier = Modifier.weight(1f)
                )
                else -> TuitionHistoryList(
                    tuitionHistories = tuitionHistories,
                    bottomPadding = bottomBarPadding,
                    modifier = Modifier.weight(1f)
                )
            }
            TuitionScholarshipTab.SCHOLARSHIP -> when {
                isScholarshipLoading -> HistoryLoading(modifier = Modifier.weight(1f))
                scholarshipErrorMessage != null -> HistoryError(
                    message = scholarshipErrorMessage,
                    onRetryClick = onScholarshipRetryClick,
                    modifier = Modifier.weight(1f)
                )
                scholarshipHistories.isEmpty() -> HistoryEmpty(
                    message = "장학금 내역이 없습니다.",
                    modifier = Modifier.weight(1f)
                )
                else -> ScholarshipHistoryList(
                    scholarshipHistories = scholarshipHistories,
                    bottomPadding = bottomBarPadding,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScholarshipHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_caret_left),
            contentDescription = "뒤로가기",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(24.dp)
                .clickable(onClick = onBackClick)
        )
    }
}

@Composable
private fun ScholarshipTabs(
    selectedTab: TuitionScholarshipTab,
    onTabClick: (TuitionScholarshipTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup()
    ) {
        TuitionScholarshipTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onTabClick(tab) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                // 선택된 탭을 파란색 밑줄로 표시합니다.
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScholarshipHistoryList(
    scholarshipHistories: List<ScholarshipHistory>,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 4.dp,
            end = 20.dp,
            // 목록의 마지막 항목이 바텀바에 가려지지 않도록 여백을 확보합니다.
            bottom = 20.dp + bottomPadding
        )
    ) {
        itemsIndexed(scholarshipHistories) { index, history ->
            ScholarshipHistoryItem(history = history)
            // 카드 대신 구분선으로 각 내역을 나눕니다.
            if (index < scholarshipHistories.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun ScholarshipHistoryItem(
    history: ScholarshipHistory,
    modifier: Modifier = Modifier
) {
    val isCompleted = history.processStatus == "지급완료"
    val description = listOf(
        "${history.year} ${history.semester}",
        history.processDate,
        history.dropReason
    ).filter { it.isNotBlank() }
        .distinct()
        .joinToString(" · ")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 108.dp)
            .padding(vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = history.scholarshipName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 62.dp)
            )
            Text(
                text = "${history.actualAmount}원",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = history.processStatus,
            color = if (isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isCompleted) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                )
                .padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TuitionHistoryList(
    tuitionHistories: List<TuitionHistory>,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val sortedHistories = tuitionHistories.sortedByDescending { it.registrationDate }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 4.dp,
            end = 20.dp,
            // 목록의 마지막 항목이 바텀바에 가려지지 않도록 여백을 확보합니다.
            bottom = 20.dp + bottomPadding
        )
    ) {
        itemsIndexed(sortedHistories) { index, history ->
            TuitionHistoryItem(history = history)
            // 카드 대신 구분선으로 각 내역을 나눕니다.
            if (index < sortedHistories.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun TuitionHistoryItem(
    history: TuitionHistory,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 108.dp)
            .padding(vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "${history.year} ${history.semester}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${history.paymentAmount}원",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "등록일자 ${history.registrationDate} · 사전감면 ${history.reduction}원",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = history.registrationType,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun HistoryLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun HistoryError(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        TextButton(onClick = onRetryClick) {
            Text(text = "다시 시도")
        }
    }
}

@Composable
private fun HistoryEmpty(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

// Preview에 띄울 목업 데이터
private val previewTuitionHistories = listOf(
    TuitionHistory(
        year = "2022학년도",
        semester = "1학기",
        grade = "1학년",
        registrationType = "학기등록",
        registrationDate = "2022.02.09",
        amount = "1,000,000",
        reduction = "0",
        paymentAmount = "1,000,000"
    ),
    TuitionHistory(
        year = "2022학년도",
        semester = "1학기",
        grade = "1학년",
        registrationType = "학기등록",
        registrationDate = "2022.02.09",
        amount = "1,000,000",
        reduction = "0",
        paymentAmount = "1,000,000"
    ),
    TuitionHistory(
        year = "2022학년도",
        semester = "1학기",
        grade = "2학년",
        registrationType = "학기등록",
        registrationDate = "2022.02.09",
        amount = "1,000,000",
        reduction = "0",
        paymentAmount = "1,000,000"
    ),
    TuitionHistory(
        year = "2022학년도",
        semester = "1학기",
        grade = "2학년",
        registrationType = "학기등록",
        registrationDate = "2022.02.09",
        amount = "1,000,000",
        reduction = "0",
        paymentAmount = "1,000,000"
    ),
    TuitionHistory(
        year = "2022학년도",
        semester = "1학기",
        grade = "2학년",
        registrationType = "학기등록",
        registrationDate = "2022.02.09",
        amount = "1,000,000",
        reduction = "0",
        paymentAmount = "1,000,000"
    ),
    TuitionHistory(
        year = "2022학년도",
        semester = "1학기",
        grade = "3학년",
        registrationType = "학기등록",
        registrationDate = "2022.02.09",
        amount = "1,000,000",
        reduction = "0",
        paymentAmount = "1,000,000"
    )
)

private val previewScholarshipHistories = listOf(
    ScholarshipHistory(
        year = "2026",
        semester = "1학기",
        scholarshipName = "한국장학재단(국가장학금Ⅰ유형)",
        paymentMethod = "사전감면",
        processStatus = "선발탈락",
        note = "",
        dropReason = "소득분위 초과",
        processDate = "2026.02.04",
        selectedAmount = "0",
        actualAmount = "0",
        redeemedAmount = "0",
        replacedAmount = "0",
        replacedScholarshipName = "",
        workDepartment = ""
    ),
    ScholarshipHistory(
        year = "2026",
        semester = "2학기",
        scholarshipName = "한국장학재단(국가장학금Ⅰ유형)",
        paymentMethod = "사전감면",
        processStatus = "지급완료",
        note = "",
        dropReason = "",
        processDate = "2026.09.03",
        selectedAmount = "0",
        actualAmount = "2,000,000",
        redeemedAmount = "0",
        replacedAmount = "0",
        replacedScholarshipName = "",
        workDepartment = ""
    ),
    ScholarshipHistory(
        year = "2026",
        semester = "1학기",
        scholarshipName = "한국장학재단(국가장학금Ⅰ유형)",
        paymentMethod = "사전감면",
        processStatus = "선발탈락",
        note = "",
        dropReason = "소득분위 초과",
        processDate = "2026.02.04",
        selectedAmount = "0",
        actualAmount = "0",
        redeemedAmount = "0",
        replacedAmount = "0",
        replacedScholarshipName = "",
        workDepartment = ""
    ),
    ScholarshipHistory(
        year = "2026",
        semester = "1학기",
        scholarshipName = "한국장학재단(국가장학금Ⅰ유형)",
        paymentMethod = "사전감면",
        processStatus = "선발탈락",
        note = "",
        dropReason = "소득분위 초과",
        processDate = "2026.02.04",
        selectedAmount = "0",
        actualAmount = "0",
        redeemedAmount = "0",
        replacedAmount = "0",
        replacedScholarshipName = "",
        workDepartment = ""
    )
)

@Preview(name = "등록금 - Light", showBackground = true, widthDp = 402, heightDp = 874)
@Preview(
    name = "등록금 - Dark",
    showBackground = true,
    widthDp = 402,
    heightDp = 874,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ScholarshipScreenPreview() {
    SoongsilLifeAndroidTheme {
        ScholarshipScreen(tuitionHistories = previewTuitionHistories)
    }
}

@Preview(name = "장학금 - Light", showBackground = true, widthDp = 402, heightDp = 874)
@Preview(
    name = "장학금 - Dark",
    showBackground = true,
    widthDp = 402,
    heightDp = 874,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ScholarshipHistoryScreenPreview() {
    SoongsilLifeAndroidTheme {
        ScholarshipScreen(
            initialTab = TuitionScholarshipTab.SCHOLARSHIP,
            scholarshipHistories = previewScholarshipHistories
        )
    }
}

@Preview(name = "로딩 - Light", showBackground = true, widthDp = 402, heightDp = 874)
@Preview(
    name = "로딩 - Dark",
    showBackground = true,
    widthDp = 402,
    heightDp = 874,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ScholarshipLoadingPreview() {
    SoongsilLifeAndroidTheme {
        ScholarshipScreen(isTuitionLoading = true)
    }
}

@Preview(name = "빈 화면 - Light", showBackground = true, widthDp = 402, heightDp = 874)
@Preview(
    name = "빈 화면 - Dark",
    showBackground = true,
    widthDp = 402,
    heightDp = 874,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ScholarshipEmptyPreview() {
    SoongsilLifeAndroidTheme {
        ScholarshipScreen()
    }
}

@Preview(name = "오류 - Light", showBackground = true, widthDp = 402, heightDp = 874)
@Preview(
    name = "오류 - Dark",
    showBackground = true,
    widthDp = 402,
    heightDp = 874,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ScholarshipErrorPreview() {
    SoongsilLifeAndroidTheme {
        ScholarshipScreen(tuitionErrorMessage = "등록금 내역을 불러오지 못했습니다.")
    }
}
