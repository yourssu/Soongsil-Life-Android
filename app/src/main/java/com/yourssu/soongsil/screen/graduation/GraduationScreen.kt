package com.yourssu.soongsil.screen.graduation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourssu.data.graduation.GraduationData
import com.yourssu.data.graduation.GraduationRequirementItem
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
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
            result = "충족"
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
            result = "부족"
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRetryClick,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(text = "다시 시도")
            }
        }
    }
}

// mock/실데이터 상관없이 화면을 그리는 순수 컴포저블입니다. Preview에서도 이걸 그대로 사용합니다.
@Composable
private fun GraduationContent(
    modifier: Modifier = Modifier,
    data: GraduationData = GraduationData(),
    onBackClick: () -> Unit = {},
    onDetailClick: () -> Unit = {}
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    val groupedItems = data.items.groupBy { it.classification }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GraduationHeader(onBackClick = onBackClick)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 16.dp + bottomBarPadding
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GraduationSummaryCard(
                    overallResult = data.overallResult,
                    onDetailClick = onDetailClick
                )
                groupedItems.forEach { (classification, items) ->
                    GraduationSection(
                        title = classification,
                        items = items
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_caret_left),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
                .clickable { onBackClick() },
            tint = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "졸업사정표",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.4).sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun GraduationSummaryCard(
    overallResult: String,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPass = overallResult.isPassResult()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "졸업사정결과 · ",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = overallResult,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPass) SoongsilPalette.Green500 else MaterialTheme.colorScheme.error
            )
        }
        Button(
            onClick = onDetailClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(text = "과목상세 보기", fontSize = 13.sp)
        }
    }
}

@Composable
private fun GraduationSection(
    title: String,
    items: List<GraduationRequirementItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        // 항목 사이 여백은 Column의 spacedBy로 처리됩니다.
        items.forEach { item ->
            GraduationRequirementRow(item = item)
        }
    }
}

@Composable
private fun GraduationRequirementRow(
    item: GraduationRequirementItem,
    modifier: Modifier = Modifier
) {
    val isPass = item.result.isPassResult()
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.requirement,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            ResultBadge(isPass = isPass, text = item.result)
        }
        // 기준/계산값이 있는 항목만 서브텍스트를 보여줍니다.
        if (item.standardValue.isNotBlank() || item.calculatedValue.isNotBlank()) {
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "기준 ${item.standardValue} · 계산 ${item.calculatedValue}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (item.difference.isNotBlank()) {
                    Text(
                        text = " · ${item.difference}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.difference.startsWith("-")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            SoongsilPalette.Green500
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultBadge(
    isPass: Boolean,
    text: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isPass) SoongsilPalette.Green50 else MaterialTheme.colorScheme.errorContainer
    val textColor = if (isPass) SoongsilPalette.Green500 else MaterialTheme.colorScheme.error
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// "충족", "합격", "가능"처럼 통과를 의미하는 결과 문자열인지 확인합니다.
private fun String.isPassResult(): Boolean =
    this == "충족" || this == "합격" || this == "가능"
