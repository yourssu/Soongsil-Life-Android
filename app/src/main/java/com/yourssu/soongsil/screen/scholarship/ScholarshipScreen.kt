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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.scholarship.TuitionHistory
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

private enum class ScholarshipTab(val label: String) {
    TUITION("등록금 내역"),
    SCHOLARSHIP("장학금 내역")
}

@Composable
fun ScholarshipScreen(
    tuitionHistories: List<TuitionHistory> = emptyList(),
    isTuitionLoading: Boolean = false,
    tuitionErrorMessage: String? = null,
    onTuitionRetryClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(ScholarshipTab.TUITION) }

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

        if (selectedTab == ScholarshipTab.TUITION) {
            when {
                isTuitionLoading -> ScholarshipLoading(modifier = Modifier.weight(1f))
                tuitionErrorMessage != null -> ScholarshipError(
                    message = tuitionErrorMessage,
                    onRetryClick = onTuitionRetryClick,
                    modifier = Modifier.weight(1f)
                )
                else -> TuitionHistoryList(
                    tuitionHistories = tuitionHistories,
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
    selectedTab: ScholarshipTab,
    onTabClick: (ScholarshipTab) -> Unit,
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
        ScholarshipTab.entries.forEach { tab ->
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
private fun TuitionHistoryList(
    tuitionHistories: List<TuitionHistory>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 12.dp,
            end = 20.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tuitionHistories) { history ->
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
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "${history.year} ${history.semester}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${history.paymentAmount}원",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "등록일자 ${history.registrationDate} · 사전감면 ${history.reduction}원",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = history.registrationType,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ScholarshipLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ScholarshipError(
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

@Preview(name = "Light", showBackground = true, widthDp = 402, heightDp = 874)
@Preview(
    name = "Dark",
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

@Preview(name = "Loading", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun ScholarshipLoadingPreview() {
    SoongsilLifeAndroidTheme {
        ScholarshipScreen(isTuitionLoading = true)
    }
}
