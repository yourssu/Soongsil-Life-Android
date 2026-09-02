package com.yourssu.soongsil.screen.graduation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.data.graduation.GraduationData
import com.yourssu.data.graduation.GraduationRequirementItem
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import com.yourssu.soongsil.ui.theme.SoongsilPalette

// ─── Screen Entry Point ───

// ViewModel과 연결되는 졸업사정표 진입점입니다. 로딩/에러/데이터 상태를 분기 처리합니다.
@Composable
fun GraduationScreen(
    modifier: Modifier = Modifier,
    viewModel: GraduationViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onDetailClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.graduationData == null -> {
                GraduationLoadingState(onBackClick = onBackClick)
            }

            uiState.error != null && uiState.graduationData == null -> {
                GraduationErrorState(
                    message = uiState.error ?: "졸업사정표를 불러오지 못했습니다.",
                    onBackClick = onBackClick,
                    onRetryClick = viewModel::retry
                )
            }

            else -> {
                GraduationContent(
                    modifier = Modifier.fillMaxSize(),
                    data = uiState.graduationData ?: GraduationData(),
                    errorMessage = uiState.error,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = viewModel::refresh,
                    onBackClick = onBackClick,
                    onDetailClick = onDetailClick
                )
            }
        }
    }
}

// ─── Content ───

// 졸업사정표 본문 화면입니다. 상단 결과 요약 및 아코디언 요건 목록을 렌더링하며 당겨서 새로고침을 지원합니다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GraduationContent(
    modifier: Modifier = Modifier,
    data: GraduationData = GraduationData(),
    errorMessage: String? = null,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onDetailClick: () -> Unit = {},
    initialExpandedClassifications: Set<String> = emptySet(),
    initialShowDetails: Boolean = false
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val pullToRefreshState = rememberPullToRefreshState()
    val groupedItems = remember(data.items) {
        data.items.groupBy { it.classification }
    }
    var showSubjectDetails by rememberSaveable { mutableStateOf(initialShowDetails) }

    // 각 분류별 펼침/접힘 상태를 관리합니다.
    val expandedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            initialExpandedClassifications.forEach { this[it] = true }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 탑바
            GraduationHeader(onBackClick = onBackClick)

            // 상단 에러 안내 뱃지
            if (errorMessage != null) {
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

            // PullToRefreshBox로 스크롤 영역을 감싸 당겨서 새로고침 지원
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                // 스크롤 가능한 아코디언 목록 영역
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 20.dp + bottomBarPadding)
                ) {
                    // 상단 졸업사정결과 타이틀 및 우측 과목 상세 토글 버튼
                    GraduationResultHeader(
                        overallResult = data.overallResult,
                        showSubjectDetails = showSubjectDetails,
                        onDetailClick = {
                            showSubjectDetails = !showSubjectDetails
                            onDetailClick()
                        }
                    )

                    // 분류별 아코디언 섹션 목록
                    groupedItems.forEach { (classification, items) ->
                        val isExpanded = expandedStates[classification] ?: false
                        GraduationAccordionSection(
                            title = classification,
                            items = items,
                            isExpanded = isExpanded,
                            showSubjectDetails = showSubjectDetails,
                            onToggle = {
                                expandedStates[classification] = !isExpanded
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Header & Results ───

// 상단 뒤로가기 버튼과 화면 타이틀을 표시합니다.
@Composable
private fun GraduationHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onBackground
    } else {
        SoongsilPalette.Navy900
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_caret_left),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(24.dp)
                .clickable { onBackClick() },
            tint = titleColor
        )
        Text(
            text = "졸업사정표",
            fontSize = 17.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = PretendardFontFamily,
            color = titleColor,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// 상단 졸업사정결과 라벨과 결과(불가능/가능) 및 우측 과목상세 보기 버튼을 표시하는 헤더입니다.
@Composable
private fun GraduationResultHeader(
    overallResult: String,
    showSubjectDetails: Boolean,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPass = overallResult.isPassResult()
    val isDark = isSystemInDarkTheme()
    val resultColor = if (isPass) {
        if (isDark) Color(0xFF4D96FF) else Color(0xFF0062FF)
    } else {
        if (isDark) Color(0xFFFF6B6B) else Color(0xFFFF3B30)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "졸업사정결과",
                fontSize = 14.sp,
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = overallResult.ifBlank { "불가능" },
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = PretendardFontFamily,
                color = resultColor
            )
        }

        // 우측 과목 상세 보기/숨기기 버튼
        val buttonBg = if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            SoongsilPalette.Gray175
        }
        val buttonText = if (showSubjectDetails) "과목상세 숨기기" else "과목상세 보기"

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(buttonBg)
                .clickable(onClick = onDetailClick)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buttonText,
                fontSize = 12.5.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PretendardFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─── Accordion Section ───

// 각 분류(졸업필수 요건, 교양 필수 등)를 여닫을 수 있는 아코디언 컴포넌트입니다.
@Composable
private fun GraduationAccordionSection(
    title: String,
    items: List<GraduationRequirementItem>,
    isExpanded: Boolean,
    showSubjectDetails: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val dividerColor = if (isDark) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    } else {
        SoongsilPalette.Gray175.copy(alpha = 0.8f)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 아코디언 헤더 (분류명 + 화살표 아이콘)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PretendardFontFamily,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "접기" else "펼치기",
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 아코디언 본문 (펼침 시 항목 목록 렌더링)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                items.forEachIndexed { index, item ->
                    GraduationRequirementItemRow(
                        item = item,
                        showSubjectDetails = showSubjectDetails
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            thickness = 0.8.dp,
                            color = dividerColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 섹션 하단 구분선
        HorizontalDivider(
            thickness = 0.8.dp,
            color = dividerColor
        )
    }
}

// 각 요건 항목의 상세 내용(요건명, 기준/계산값, 상태 뱃지, 이수 과목 목록)을 표시하는 행입니다.
@Composable
private fun GraduationRequirementItemRow(
    item: GraduationRequirementItem,
    showSubjectDetails: Boolean,
    modifier: Modifier = Modifier
) {
    val isPass = item.result.isPassResult()
    val hasCalculation = item.standardValue.isNotBlank() || item.calculatedValue.isNotBlank()
    val usedSubjects = item.usedSubjects.filter { it.isNotBlank() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                // 요건 명칭
                Text(
                    text = item.requirement,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PretendardFontFamily,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // 기준/계산값 정보 (있는 경우)
                if (hasCalculation) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "기준 ${item.standardValue} · 계산 ${item.calculatedValue}",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            fontFamily = PretendardFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.difference.isNotBlank()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.difference,
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PretendardFontFamily,
                                color = if (item.difference.startsWith("-")) {
                                    if (isSystemInDarkTheme()) Color(0xFFFF6B6B) else Color(0xFFFF3B30)
                                } else {
                                    if (isSystemInDarkTheme()) Color(0xFF4D96FF) else Color(0xFF0062FF)
                                }
                            )
                        }
                    }
                }
            }

            // 우측 상태 뱃지 (충족 / 부족)
            GraduationStatusBadge(isPass = isPass, text = item.result)
        }

        // 과목 상세 활성화 시 이수 과목 배지 목록 노출
        if (showSubjectDetails && usedSubjects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                usedSubjects.forEach { subject ->
                    UsedSubjectBadge(subject = subject)
                }
            }
        }
    }
}

// 해당 졸업 요건 계산에 사용된 과목명을 작은 배지로 표시합니다.
@Composable
private fun UsedSubjectBadge(
    subject: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        SoongsilPalette.Gray175
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = subject,
            fontSize = 11.5.sp,
            lineHeight = 13.sp,
            fontFamily = PretendardFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 충족/부족 상태를 나타내는 미니 뱃지 컴포넌트입니다.
@Composable
private fun GraduationStatusBadge(
    isPass: Boolean,
    text: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isPass) {
        if (isDark) Color(0xFF1E2F4D) else Color(0xFFE8F3FF)
    } else {
        if (isDark) Color(0xFF2B2D31) else Color(0xFFF2F4F6)
    }
    val textColor = if (isPass) {
        if (isDark) Color(0xFF5B9DFF) else Color(0xFF3182F6)
    } else {
        if (isDark) Color(0xFF9E9E9E) else Color(0xFF8B95A1)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.ifBlank { if (isPass) "충족" else "부족" },
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PretendardFontFamily,
            color = textColor
        )
    }
}

// ─── Loading & Error States ───

@Composable
private fun GraduationLoadingState(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        GraduationHeader(onBackClick = onBackClick, modifier = Modifier.align(Alignment.TopCenter))
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun GraduationErrorState(
    message: String,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        GraduationHeader(onBackClick = onBackClick, modifier = Modifier.align(Alignment.TopCenter))
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                fontFamily = PretendardFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRetryClick,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(text = "다시 시도", fontFamily = PretendardFontFamily)
            }
        }
    }
}

// "충족", "합격", "가능"처럼 통과를 의미하는 결과 문자열인지 확인합니다.
private fun String.isPassResult(): Boolean =
    this == "충족" || this == "합격" || this == "가능"

// ─── Previews ───

private val previewSampleData = GraduationData(
    overallResult = "불가능",
    items = listOf(
        GraduationRequirementItem(
            classification = "졸업필수 요건",
            requirement = "학부-졸업학점 133",
            standardValue = "133.0",
            calculatedValue = "131.0",
            difference = "-2.0",
            result = "부족"
        ),
        GraduationRequirementItem(
            classification = "졸업필수 요건",
            requirement = "학부-편입 요이수 지정과목",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "졸업필수 요건",
            requirement = "학부-졸업논문/졸업시험 이수",
            result = "부족"
        ),
        GraduationRequirementItem(
            classification = "졸업필수 요건",
            requirement = "학부-기독교과목 4학점 이상 (22이전)",
            standardValue = "4",
            calculatedValue = "4.0",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "졸업필수 요건",
            requirement = "학부-졸업확정신고 여부",
            result = "부족"
        ),
        GraduationRequirementItem(
            classification = "교양 필수",
            requirement = "학부-교양필수 16",
            standardValue = "16",
            calculatedValue = "16.0",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "교양 선택",
            requirement = "학부-교양선택 12",
            standardValue = "12",
            calculatedValue = "15.0",
            difference = "+3.0",
            result = "충족",
            usedSubjects = listOf("컴퓨팅적사고", "글로벌시민의식")
        ),
        GraduationRequirementItem(
            classification = "전공 기초",
            requirement = "학부-전기-AI소프트(23이전) 18",
            standardValue = "18",
            calculatedValue = "18.0",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "전공",
            requirement = "학부-전필-AI소프트 12",
            standardValue = "12",
            calculatedValue = "12.0",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "전공",
            requirement = "학부-전필+전선-AI소프트(23이전) 66",
            standardValue = "66",
            calculatedValue = "61.0",
            difference = "-5.0",
            result = "부족",
            usedSubjects = listOf("자료구조", "알고리즘", "운영체제", "소프트웨어공학")
        ),
        GraduationRequirementItem(
            classification = "채플",
            requirement = "학부-채플(신입 6회, 편입2혹은4회)",
            result = "충족"
        )
    )
)

// 아코디언 전체가 접혀있는 기본 화면 프리뷰입니다.
@Composable
@Preview(name = "아코디언 접힘 - Light", showBackground = true)
@Preview(name = "아코디언 접힘 - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationAccordionCollapsedPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GraduationContent(
                data = previewSampleData,
                initialExpandedClassifications = emptySet()
            )
        }
    }
}

// 첫 번째 섹션(졸업필수 요건)이 펼쳐진 아코디언 화면 프리뷰입니다.
@Composable
@Preview(name = "첫 번째 섹션 펼침 - Light", showBackground = true)
@Preview(name = "첫 번째 섹션 펼침 - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationAccordionExpandedPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GraduationContent(
                data = previewSampleData,
                initialExpandedClassifications = setOf("졸업필수 요건")
            )
        }
    }
}

// 과목 상세가 펼쳐진 아코디언 화면 프리뷰입니다.
@Composable
@Preview(name = "과목 상세 펼침 - Light", showBackground = true)
@Preview(name = "과목 상세 펼침 - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationSubjectDetailsExpandedPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GraduationContent(
                data = previewSampleData,
                initialExpandedClassifications = setOf("교양 선택", "전공"),
                initialShowDetails = true
            )
        }
    }
}

// 통과(가능) 상태의 졸업사정 화면 프리뷰입니다.
@Composable
@Preview(name = "통과 상태 (가능) - Light", showBackground = true)
@Preview(name = "통과 상태 (가능) - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationPassPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GraduationContent(
                data = previewSampleData.copy(
                    overallResult = "가능",
                    items = previewSampleData.items.map { it.copy(result = "충족", difference = "") }
                ),
                initialExpandedClassifications = setOf("졸업필수 요건")
            )
        }
    }
}

// 에러 뱃지가 표시되는 화면 프리뷰입니다.
@Composable
@Preview(name = "에러 뱃지 노출 - Light", showBackground = true)
@Preview(name = "에러 뱃지 노출 - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationErrorBadgePreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GraduationContent(
                data = previewSampleData,
                errorMessage = "유세인트 통신 상태가 원활하지 않아 캐시된 데이터를 표시합니다."
            )
        }
    }
}
