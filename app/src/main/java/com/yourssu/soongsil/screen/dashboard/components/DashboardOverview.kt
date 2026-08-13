package com.yourssu.soongsil.screen.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.dashboard.DashboardSemesterGrade
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilPalette

@Composable
fun DashboardTopBar(
    isLoading: Boolean,
    loadingText: String,
    completedCount: Int,
    totalCount: Int,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.yourssu),
                contentDescription = "슬기로운 숭실생활",
                modifier = Modifier
                    .width(48.dp)
                    .height(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            if (isLoading) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = loadingText,
                        color = SoongsilPalette.Slate400,
                        fontFamily = PretendardFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    LinearProgressIndicator(
                        progress = {
                            if (totalCount == 0) 0f
                            else completedCount.toFloat() / totalCount
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Color(0xFF3182F6),
                        trackColor = Color(0xFFC9E2FF)
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(onClick = onNotificationClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_tabbar_bell),
                    contentDescription = "알림",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
fun DashboardGradeSection(
    gpa: String,
    maxGpa: String,
    earnedCredits: String,
    requiredCredits: String,
    semesterRank: String,
    totalRank: String,
    semesterGrades: List<DashboardSemesterGrade>,
    showSensitiveData: Boolean,
    showGraphData: Boolean,
    sensitiveDataAlpha: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showSensitiveData) {
            Row(
                modifier = Modifier.alpha(sensitiveDataAlpha),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = gpa,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = PretendardFontFamily,
                    fontSize = 40.sp,
                    lineHeight = 56.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.16.sp
                )
                Text(
                    text = "/ $maxGpa",
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
                    color = SoongsilPalette.Slate400,
                    fontFamily = PretendardFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // 구형 기기에서 블러가 지원되지 않아도 실제 GPA가 Compose 트리에 생성되지 않습니다.
            Spacer(modifier = Modifier.height(56.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
        DashboardGradeMetric(
            "취득 학점",
            earnedCredits,
            requiredCredits,
            showSensitiveData,
            sensitiveDataAlpha
        )
        DashboardGradeMetric(
            "학기별 석차",
            semesterRank.substringBefore("/"),
            semesterRank.substringAfter("/", ""),
            showSensitiveData,
            sensitiveDataAlpha
        )
        DashboardGradeMetric(
            "전체 석차",
            totalRank.substringBefore("/"),
            totalRank.substringAfter("/", ""),
            showSensitiveData,
            sensitiveDataAlpha
        )

        DashboardGpaLineChart(
            grades = semesterGrades,
            showData = showGraphData,
            dataAlpha = sensitiveDataAlpha,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
    }
}

@Composable
fun DashboardGradeDetailButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "학기별 성적보기  →",
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = PretendardFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DashboardGradeMetric(
    label: String,
    value: String,
    total: String,
    showValue: Boolean,
    valueAlpha: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = SoongsilPalette.Slate400,
            fontFamily = PretendardFontFamily,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Medium
        )
        if (showValue) {
            Row(
                modifier = Modifier.alpha(valueAlpha),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = PretendardFontFamily,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (total.isNotBlank()) {
                    Text(
                        text = " / $total",
                        color = SoongsilPalette.Slate400,
                        fontFamily = PretendardFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Spacer(
                modifier = Modifier
                    .width(64.dp)
                    .height(21.dp)
            )
        }
    }
}

@Composable
private fun DashboardGpaLineChart(
    grades: List<DashboardSemesterGrade>,
    showData: Boolean,
    dataAlpha: Float,
    modifier: Modifier = Modifier
) {
    val points = grades.takeLast(8)
    val lineColor = Color(0xFF3182F6)
    val gridColor = Color(0xFFDCE9FF)

    Row(modifier = modifier.height(160.dp)) {
        Column(
            modifier = Modifier
                .width(28.dp)
                .height(120.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("4.0", "3.0", "2.0", "1.0").forEach { label ->
                Text(
                    text = label,
                    color = SoongsilPalette.Slate400,
                    fontFamily = PretendardFontFamily,
                    fontSize = 12.sp
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                repeat(4) { index ->
                    val y = size.height * index / 3f
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }
                // 블러 상태에서는 축과 눈금만 남기고 성적 선·점·면적 데이터는 그리지 않습니다.
                if (showData && points.isNotEmpty()) {
                    val xGap = if (points.size == 1) 0f else size.width / (points.size - 1)
                    val offsets = points.mapIndexed { index, grade ->
                        val value = (grade.gpa.toFloatOrNull() ?: 0f).coerceIn(1f, 4.5f)
                        val targetY = size.height * (4.5f - value) / 3.5f
                        // 공개 애니메이션 진행에 맞춰 각 점을 그래프 바닥에서 실제 위치까지 올립니다.
                        val animatedY = size.height + (targetY - size.height) * dataAlpha
                        Offset(index * xGap, animatedY)
                    }
                    val linePath = Path().apply {
                        moveTo(offsets.first().x, offsets.first().y)
                        offsets.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    val areaPath = Path().apply {
                        addPath(linePath)
                        lineTo(offsets.last().x, size.height)
                        lineTo(offsets.first().x, size.height)
                        close()
                    }
                    drawPath(areaPath, lineColor.copy(alpha = 0.18f * dataAlpha))
                    drawPath(
                        linePath,
                        lineColor.copy(alpha = dataAlpha),
                        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    offsets.forEach {
                        drawCircle(lineColor.copy(alpha = dataAlpha), 3.dp.toPx(), it)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                points.forEach { grade ->
                    Text(
                        text = grade.label,
                        modifier = Modifier.weight(1f),
                        color = SoongsilPalette.Slate400,
                        fontFamily = PretendardFontFamily,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardChapelSection(
    seat: String,
    totalClasses: Int,
    attended: Int,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passRequired = (totalClasses - 1).coerceAtLeast(0)
    val remaining = (passRequired - attended).coerceAtLeast(0)
    val progress = if (totalClasses == 0) 0f else attended.toFloat() / totalClasses
    val passPoint = if (totalClasses == 0) 0f else passRequired.toFloat() / totalClasses

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "채플 출석",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = PretendardFontFamily,
                fontSize = 18.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "자세히  ›",
                modifier = Modifier.clickable(onClick = onDetailClick),
                color = SoongsilPalette.Slate400,
                fontFamily = PretendardFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFC9E2FF), RoundedCornerShape(4.dp))
                .padding(top = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (remaining == 0) {
                        Text(
                            text = "이번 학기 채플은 Pass!",
                            style = chapelBodyStyle(),
                            color = Color(0xFF3182F6),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Row {
                            Text("Pass까지 ", style = chapelBodyStyle(), color = MaterialTheme.colorScheme.onBackground)
                            Text("${remaining}회", style = chapelBodyStyle(), color = Color(0xFF3182F6), fontWeight = FontWeight.SemiBold)
                            Text(" 남았어요", style = chapelBodyStyle(), color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Text(
                        text = "$attended / $totalClasses",
                        color = Color(0xFF3182F6),
                        fontFamily = PretendardFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFFC9E2FF), RoundedCornerShape(10.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(8.dp)
                            .background(Color(0xFF3182F6), RoundedCornerShape(10.dp))
                    )
                    if (totalClasses > 0) {
                        val markerX = maxWidth * passPoint.coerceIn(0f, 1f)
                        val labelX = (markerX - 42.dp)
                            .coerceIn(0.dp, (maxWidth - 84.dp).coerceAtLeast(0.dp))
                        Box(
                            modifier = Modifier
                                .offset(x = markerX)
                                .width(1.dp)
                                .height(12.dp)
                                .background(Color(0xFF2272EB))
                        )
                        Text(
                            text = "Pass 기준점",
                            modifier = Modifier
                                .offset(x = labelX, y = 12.dp)
                                .width(84.dp),
                            color = Color(0xFF2272EB),
                            fontFamily = PretendardFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("좌석 정보", style = chapelBodyStyle(), color = SoongsilPalette.Slate400)
                    Text(
                        text = seat,
                        style = chapelBodyStyle(),
                        color = Color(0xFF3182F6),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun chapelBodyStyle() = androidx.compose.ui.text.TextStyle(
    fontFamily = PretendardFontFamily,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    fontWeight = FontWeight.Medium
)

@Composable
fun DashboardQuickLinks(
    onGraduateClick: () -> Unit,
    onScholarshipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "바로가기",
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = PretendardFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardQuickLink("졸업사정표", onGraduateClick, Modifier.weight(1f), R.drawable.graduate)
            DashboardQuickLink("등록금 · 장학금", onScholarshipClick, Modifier.weight(1f), R.drawable.coin)
        }
    }
}

@Composable
private fun DashboardQuickLink(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, resId: Int) {
    Column(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = label,
            modifier = Modifier.size(34.dp)
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = PretendardFontFamily,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
