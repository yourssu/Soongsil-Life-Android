package com.yourssu.soongsil.screen.scholarship

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
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
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        ScholarshipTabs(
            selectedTab = selectedTab,
            onTabClick = { selectedTab = it },
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)
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
            .height(56.dp),
        contentAlignment = Alignment.Center
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
        Text(
            text = "등록금·장학금 조회",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp
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
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TuitionScholarshipTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable { onTabClick(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
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
            top = 11.dp,
            end = 20.dp,
            // 목록의 마지막 항목이 바텀바에 가려지지 않도록 여백을 확보합니다.
            bottom = 20.dp + bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        items(scholarshipHistories) { history ->
            ScholarshipHistoryCard(history = history)
        }
    }
}

@Composable
private fun ScholarshipHistoryCard(
    history: ScholarshipHistory,
    modifier: Modifier = Modifier
) {
    val isCompleted = history.processStatus == "지급완료"
    val description = listOf(
        "${history.year} ${history.semester}",
        history.processDate,
        history.dropReason,
        history.note
    ).filter { it.isNotBlank() }
        .distinct()
        .joinToString(" · ")
    val additionalDescription = listOfNotNull(
        history.paymentMethod.takeIf { it.isNotBlank() }?.let { "지급방식 $it" },
        history.redeemedAmount.toAmountDetail("환수금액"),
        history.replacedAmount.toAmountDetail("대체금액"),
        history.replacedScholarshipName.takeIf { it.isNotBlank() }?.let { "대체장학금 $it" },
        history.workDepartment.takeIf { it.isNotBlank() }?.let { "근로부서 $it" }
    ).joinToString(" · ")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 90.5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
            if (additionalDescription.isNotBlank()) {
                Text(
                    text = additionalDescription,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = history.processStatus,
            color = if (isCompleted) {
                MaterialTheme.colorScheme.tertiary
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
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.background
                    }
                )
                .padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

private fun String.toAmountDetail(label: String): String? {
    val value = trim()
    if (value.isBlank()) return null

    val amount = value.removeSuffix("원").replace(",", "").trim().toLongOrNull()
    if (amount == 0L) return null

    return "$label ${if (value.endsWith("원")) value else "${value}원"}"
}

@Composable
private fun TuitionHistoryList(
    tuitionHistories: List<TuitionHistory>,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 11.dp,
            end = 20.dp,
            // 목록의 마지막 항목이 바텀바에 가려지지 않도록 여백을 확보합니다.
            bottom = 20.dp + bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        items(tuitionHistories.sortedByDescending { it.registrationDate }) { history ->
            TuitionHistoryCard(history = history)
        }
    }
}

@Composable
private fun TuitionHistoryCard(
    history: TuitionHistory,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 93.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
                .background(MaterialTheme.colorScheme.surfaceContainer)
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
        registrationDate = "2022.02.10",
        amount = "4,731,000",
        reduction = "180,000",
        paymentAmount = "4,551,000"
    ),
    TuitionHistory(
        year = "2022학년도",
        semester = "2학기",
        grade = "1학년",
        registrationType = "학기등록",
        registrationDate = "2022.08.24",
        amount = "4,551,000",
        reduction = "0",
        paymentAmount = "4,551,000"
    ),
    TuitionHistory(
        year = "2023학년도",
        semester = "1학기",
        grade = "2학년",
        registrationType = "학기등록",
        registrationDate = "2023.02.23",
        amount = "4,411,000",
        reduction = "0",
        paymentAmount = "4,411,000"
    ),
    TuitionHistory(
        year = "2024학년도",
        semester = "여름학기",
        grade = "2학년",
        registrationType = "학기등록",
        registrationDate = "2024.05.24",
        amount = "255,000",
        reduction = "0",
        paymentAmount = "255,000"
    ),
    TuitionHistory(
        year = "2025학년도",
        semester = "2학기",
        grade = "2학년",
        registrationType = "학기등록",
        registrationDate = "2025.08.26",
        amount = "4,629,000",
        reduction = "0",
        paymentAmount = "4,629,000"
    ),
    TuitionHistory(
        year = "2026학년도",
        semester = "1학기",
        grade = "3학년",
        registrationType = "학기등록",
        registrationDate = "2026.02.26",
        amount = "4,765,000",
        reduction = "0",
        paymentAmount = "4,765,000"
    )
)

private val previewScholarshipHistories = listOf(
    ScholarshipHistory(
        year = "2026",
        semester = "1학기",
        scholarshipName = "특별장학금(주거비지원)_학업장려비",
        paymentMethod = "사후지급",
        processStatus = "선발탈락",
        note = "",
        dropReason = "순위 외",
        processDate = "2026.06.16",
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
        processDate = "2026.02.03",
        selectedAmount = "0",
        actualAmount = "0",
        redeemedAmount = "0",
        replacedAmount = "0",
        replacedScholarshipName = "",
        workDepartment = ""
    ),
    ScholarshipHistory(
        year = "2022",
        semester = "2학기",
        scholarshipName = "학과(부)우수장학금",
        paymentMethod = "사후지급",
        processStatus = "지급완료",
        note = "[융특]학과우수장학금",
        dropReason = "",
        processDate = "2023.02.14",
        selectedAmount = "0",
        actualAmount = "100,000",
        redeemedAmount = "10,000",
        replacedAmount = "50,000",
        replacedScholarshipName = "교내대체장학금",
        workDepartment = "학생서비스팀"
    ),
    ScholarshipHistory(
        year = "2022",
        semester = "1학기",
        scholarshipName = "한국장학재단(국가장학금Ⅱ유형) 신·편입생지원",
        paymentMethod = "사전감면",
        processStatus = "지급완료",
        note = "",
        dropReason = "",
        processDate = "2022.03.01",
        selectedAmount = "0",
        actualAmount = "180,000",
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

@Preview(name = "Loading", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun ScholarshipLoadingPreview() {
    SoongsilLifeAndroidTheme {
        ScholarshipScreen(isTuitionLoading = true)
    }
}
