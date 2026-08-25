package com.yourssu.soongsil.screen.graduation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
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


private val previewFailData = GraduationData(
    overallResult = "불가능",
    items = listOf(
        GraduationRequirementItem(
            classification = "졸업필수 요건",
            requirement = "학부-졸업학점 133",
            standardValue = "133",
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
            classification = "교양필수",
            requirement = "학부-교양필수 16",
            standardValue = "16",
            calculatedValue = "16.0",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "교양선택",
            requirement = "학부-교양선택 12",
            standardValue = "12",
            calculatedValue = "15.0",
            difference = "+3.0",
            result = "충족",
            usedSubjects = listOf("컴퓨팅적사고", "글로벌시민의식")
        ),
        GraduationRequirementItem(
            classification = "교양선택",
            requirement = "통합조건 2020~ 교선 공동체/리더십 1과목",
            standardValue = "1",
            calculatedValue = "2.0",
            difference = "+1.0",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "교양선택",
            requirement = "통합조건 2020~ 교선 의사소통/글로벌 1과",
            standardValue = "1",
            calculatedValue = "1.0",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "교양선택",
            requirement = "통합조건 2020~ 교선 창의융합 2개 영역",
            result = "충족"
        ),
        GraduationRequirementItem(
            classification = "전공기초",
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

private val previewPassData = previewFailData.copy(
    overallResult = "가능",
    items = previewFailData.items.map { it.copy(result = "충족", difference = "") }
)

@Composable
@Preview(name = "불가능 - Light")
@Preview(name = "불가능 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationScreenFailPreview() {
    SoongsilLifeAndroidTheme {
        GraduationContent(data = previewFailData)
    }
}

@Composable
@Preview(name = "가능 - Light")
@Preview(name = "가능 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationScreenPassPreview() {
    SoongsilLifeAndroidTheme {
        GraduationContent(data = previewPassData)
    }
}

@Composable
@Preview(name = "프로그레스바 (기준 미달) - Light")
@Preview(name = "프로그레스바 (기준 미달) - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationProgressBarUnderPreview() {
    SoongsilLifeAndroidTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GraduationRequirementProgressBar(
                standardValue = "133",
                calculatedValue = "131.0",
                isPass = false
            )
        }
    }
}

@Composable
@Preview(name = "프로그레스바 (기준 초과) - Light")
@Preview(name = "프로그레스바 (기준 초과) - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationProgressBarOverPreview() {
    SoongsilLifeAndroidTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GraduationRequirementProgressBar(
                standardValue = "12",
                calculatedValue = "15.0",
                isPass = true
            )
        }
    }
}

@Composable
@Preview(name = "로딩 - Light")
@Preview(name = "로딩 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationScreenLoadingPreview() {
    SoongsilLifeAndroidTheme {
        GraduationLoadingState(onBackClick = {})
    }
}

@Composable
@Preview(name = "에러 - Light")
@Preview(name = "에러 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationScreenErrorPreview() {
    SoongsilLifeAndroidTheme {
        GraduationErrorState(
            message = "졸업사정표를 불러오지 못했습니다.",
            onBackClick = {},
            onRetryClick = {}
        )
    }
}

// 상단 에러 안내 뱃지가 표시되는 졸업사정 화면 프리뷰입니다.
@Composable
@Preview(name = "에러 뱃지 노출 - Light")
@Preview(name = "에러 뱃지 노출 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun GraduationScreenWithErrorBadgePreview() {
    SoongsilLifeAndroidTheme {
        GraduationContent(
            data = previewFailData,
            errorMessage = "유세인트 통신 상태가 원활하지 않아 캐시된 데이터를 표시합니다."
        )
    }
}

@Composable
@Preview(name = "과목 상세 펼침 - Light")
private fun GraduationDetailsPreview() {
    SoongsilLifeAndroidTheme {
        GraduationContent(data = previewFailData, initialShowDetails = true)
    }
}

// ─── Screen ───

// ViewModel과 연결되는 진입점입니다. 실제 데이터 로딩/에러 상태를 처리한 뒤 GraduationContent에 그리기를 맡깁니다.
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
                    onBackClick = onBackClick,
                    onDetailClick = onDetailClick
                )
            }
        }
    }
}

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

// mock/실데이터 상관없이 화면을 그리는 순수 컴포저블입니다. Preview에서도 이걸 그대로 사용합니다.
@Composable
private fun GraduationContent(
    modifier: Modifier = Modifier,
    data: GraduationData = GraduationData(),
    errorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onDetailClick: () -> Unit = {},
    initialShowDetails: Boolean = false
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val groupedItems = data.items.groupBy { it.classification }
    var showSubjectDetails by rememberSaveable { mutableStateOf(initialShowDetails) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GraduationHeader(onBackClick = onBackClick)

            // 상단 에러 안내 뱃지 (헤더와의 간격을 줄여 자연스럽게 이어지도록 배치)
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Info,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 20.dp + bottomBarPadding
                    ),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                GraduationSummaryCard(
                    overallResult = data.overallResult,
                    showSubjectDetails = showSubjectDetails,
                    onDetailClick = {
                        showSubjectDetails = !showSubjectDetails
                        onDetailClick()
                    }
                )
                groupedItems.forEach { (classification, items) ->
                    GraduationSection(
                        title = classification,
                        items = items,
                        showSubjectDetails = showSubjectDetails
                    )
                }
            }
        }
    }
}

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
            letterSpacing = 0.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun GraduationSummaryCard(
    overallResult: String,
    showSubjectDetails: Boolean,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPass = overallResult.isPassResult()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.5.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "졸업사정결과 · ",
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontFamily = PretendardFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = overallResult,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = PretendardFontFamily,
                color = if (isPass) SoongsilPalette.Green500 else SoongsilPalette.Red400
            )
        }
        val detailButtonShape = RoundedCornerShape(100.dp)
        Box(
            modifier = Modifier
                .width(if (showSubjectDetails) 99.5.dp else 89.dp)
                .height(30.5.dp)
                .clip(detailButtonShape)
                .background(
                    if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.surfaceContainer
                    } else {
                        SoongsilPalette.Gray175
                    },
                    detailButtonShape
                )
                .clickable(onClick = onDetailClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showSubjectDetails) "과목상세 숨기기" else "과목상세 보기",
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PretendardFontFamily,
                color = if (isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    SoongsilPalette.Gray950
                }
            )
        }
    }
}

@Composable
private fun GraduationSection(
    title: String,
    items: List<GraduationRequirementItem>,
    showSubjectDetails: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = PretendardFontFamily,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(7.5.dp))
        items.forEach { item ->
            HorizontalDivider(
                thickness = 1.dp,
                color = if (isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.outlineVariant
                } else {
                    SoongsilPalette.Gray190
                }
            )
            GraduationRequirementRow(
                item = item,
                showSubjectDetails = showSubjectDetails
            )
        }
    }
}

// 각 졸업 요건 항목의 상태와 기준값/계산값을 표시하는 행 컴포넌트입니다.
// @param item 졸업 요건 항목 데이터
// @param showSubjectDetails 과목 상세 표시 여부
// @param modifier 컴포저블 수정자
@Composable
private fun GraduationRequirementRow(
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
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.requirement,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PretendardFontFamily,
                color = if (isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    SoongsilPalette.Gray950
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            ResultBadge(isPass = isPass, text = item.result)
        }

        // 기준/계산값이 있는 항목은 Linear Progress Bar와 통과 기준점을 표시합니다.
        if (hasCalculation) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "취득 ${item.calculatedValue}",
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PretendardFontFamily,
                        color = if (isSystemInDarkTheme()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            SoongsilPalette.Gray950
                        }
                    )
                    Text(
                        text = " / 필요 ${item.standardValue}",
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontFamily = PretendardFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.difference.isNotBlank()) {
                    Text(
                        text = item.difference,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PretendardFontFamily,
                        color = if (item.difference.startsWith("-")) {
                            SoongsilPalette.Red400
                        } else {
                            SoongsilPalette.Green400
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            GraduationRequirementProgressBar(
                standardValue = item.standardValue,
                calculatedValue = item.calculatedValue,
                isPass = isPass
            )
        }

        if (showSubjectDetails && usedSubjects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
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

// 졸업 요건의 기준값과 계산값을 Linear Progress Bar 및 통과 기준점 마커로 시각화합니다.
// @param standardValue 요건의 기준값 문자열
// @param calculatedValue 요건의 현재 계산값 문자열
// @param isPass 요건 충족 여부
// @param modifier 컴포저블 수정자
@Composable
private fun GraduationRequirementProgressBar(
    standardValue: String,
    calculatedValue: String,
    isPass: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    // 기준값과 계산값을 실수형으로 변환합니다.
    val standard = standardValue.toDoubleOrNull() ?: 0.0
    val calculated = calculatedValue.toDoubleOrNull() ?: 0.0

    // 기준값을 채우기 전에는 기준값이 max가 되고, 계산값이 기준값을 넘기면 계산값이 max가 됩니다.
    val maxValue = maxOf(standard, calculated)

    // 프로그레스바의 진행률(0.0 ~ 1.0)을 계산합니다.
    val progress = if (maxValue > 0.0) {
        (calculated / maxValue).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    // 기준점(통과 기준선)의 위치 비율(0.0 ~ 1.0)을 계산합니다.
    val passPoint = if (maxValue > 0.0) {
        (standard / maxValue).toFloat().coerceIn(0f, 1f)
    } else {
        1f
    }

    val trackColor = if (isDark) {
        SoongsilPalette.Gray875
    } else {
        SoongsilPalette.Blue100
    }
    val progressColor = if (isPass) {
        MaterialTheme.colorScheme.primary
    } else {
        SoongsilPalette.Blue500
    }
    val markerColor = if (isDark) {
        SoongsilPalette.Red400
    } else {
        SoongsilPalette.Red600
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(29.dp)
    ) {
        // 프로그레스 바 배경 (Track)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(trackColor)
        )
        // 프로그레스 바 채움 (Progress Fill)
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(progressColor)
        )

        // 기준점 마커 및 라벨 표시 (채플 화면과 동일한 기준점 시각화)
        if (maxValue > 0.0) {
            val markerWidth = 3.dp
            val markerHeight = 11.dp
            // 마커의 X 좌표 (0 ~ maxWidth - markerWidth)
            val markerX = ((maxWidth - markerWidth) * passPoint).coerceIn(0.dp, (maxWidth - markerWidth).coerceAtLeast(0.dp))
            val markerCenterX = markerX + markerWidth / 2

            // 라벨 폭 및 정렬 (passPoint 위치에 맞춰 선과 글자가 완벽하게 정렬되도록 보정)
            val labelWidth = 36.dp
            val labelX = (markerCenterX - labelWidth * passPoint)
                .coerceIn(0.dp, (maxWidth - labelWidth).coerceAtLeast(0.dp))
            val textAlign = when {
                passPoint >= 0.85f -> TextAlign.End
                passPoint <= 0.15f -> TextAlign.Start
                else -> TextAlign.Center
            }

            // 필요 기준 세로 마커 라인 (눈에 잘 띄도록 밝고 두껍게 표시)
            Box(
                modifier = Modifier
                    .offset(x = markerX)
                    .width(markerWidth)
                    .height(markerHeight)
                    .background(markerColor, RoundedCornerShape(1.5.dp))
            )
            // 필요 텍스트 라벨 (마커 선과 항상 일치하도록 정렬)
            Text(
                text = "필요",
                modifier = Modifier
                    .offset(x = labelX, y = 12.dp)
                    .width(labelWidth),
                color = markerColor,
                fontFamily = PretendardFontFamily,
                fontSize = 11.5.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = textAlign
            )
        }
    }
}

// 해당 졸업 요건 계산에 사용된 과목명을 작은 배지로 표시합니다.
@Composable
private fun UsedSubjectBadge(
    subject: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(21.dp)
            .background(
                color = SoongsilPalette.Gray175,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = subject,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = PretendardFontFamily,
            color = SoongsilPalette.Slate500,
            maxLines = 1
        )
    }
}

@Composable
private fun ResultBadge(
    isPass: Boolean,
    text: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isPass) SoongsilPalette.Green50 else SoongsilPalette.Red50
    val textColor = if (isPass) SoongsilPalette.Green500 else SoongsilPalette.Red600
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PretendardFontFamily,
            color = textColor
        )
    }
}

// "충족", "합격", "가능"처럼 통과를 의미하는 결과 문자열인지 확인합니다.
private fun String.isPassResult(): Boolean =
    this == "충족" || this == "합격" || this == "가능"
