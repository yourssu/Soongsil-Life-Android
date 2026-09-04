package com.yourssu.soongsil.screen.dashboard.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.dashboard.DashboardSemesterGrade
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
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

// 대시보드 화면의 채플 섹션을 표시합니다.
// @param seat 배정된 좌석 번호
// @param totalClasses 채플 총 수업 횟수
// @param attended 현재 출석 완료 횟수
// @param onDetailClick 채플 상세 이동 클릭 이벤트
// @param year 채플 대상 연도 (예: "2026")
// @param semester 채플 대상 학기 (예: "1학기")
@Composable
fun DashboardChapelSection(
    seat: String,
    totalClasses: Int,
    attended: Int,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier,
    year: String = "",
    semester: String = ""
) {
    val hasChapelData = totalClasses > 0
    val passRequired = (totalClasses - 1).coerceAtLeast(0)
    val remaining = (passRequired - attended).coerceAtLeast(0)
    val progress = if (totalClasses == 0) 0f else attended.toFloat() / totalClasses
    val passPoint = if (totalClasses == 0) 0f else passRequired.toFloat() / totalClasses

    // "2026년 1학기"와 같이 N년 N학기 형식으로 학기 텍스트를 구성합니다.
    val formattedTerm = if (year.isNotBlank() && semester.isNotBlank()) {
        val normalizedSemester = if (semester.endsWith("학기")) semester else "${semester}학기"
        "${year}년 $normalizedSemester"
    } else {
        "이번 학기"
    }

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
            if (!hasChapelData) {
                // 채플 상세 데이터가 없는 경우 안내 문구를 표시합니다.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "$formattedTerm 채플 정보가 없어요",
                        style = chapelBodyStyle(),
                        color = SoongsilPalette.Slate400,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
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
                                text = "$formattedTerm 채플은 Pass!",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$attended",
                                color = Color(0xFF3182F6),
                                fontFamily = PretendardFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " / $totalClasses",
                                color = SoongsilPalette.Slate400,
                                fontFamily = PretendardFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 세그먼트 분할 프로그레스 바 및 상단 역삼각형(▼) 기준 마커
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        val triangleWidth = 8.dp
                        val triangleHeight = 6.dp
                        val markerX = if (totalClasses > 0) {
                            (maxWidth * passPoint.coerceIn(0f, 1f) - triangleWidth / 2)
                                .coerceIn(0.dp, (maxWidth - triangleWidth).coerceAtLeast(0.dp))
                        } else {
                            0.dp
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // 상단 Pass 기준 역삼각형 마커
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(triangleHeight)
                            ) {
                                if (totalClasses > 0) {
                                    Canvas(
                                        modifier = Modifier
                                            .offset(x = markerX)
                                            .size(width = triangleWidth, height = triangleHeight)
                                    ) {
                                        val path = Path().apply {
                                            moveTo(0f, 0f)
                                            lineTo(size.width, 0f)
                                            lineTo(size.width / 2f, size.height)
                                            close()
                                        }
                                        drawPath(path, color = Color(0xFF3182F6))
                                    }
                                }
                            }

                            // 분할된 세그먼트 프로그레스 막대
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                            ) {
                                val segmentCount = totalClasses.coerceAtLeast(1)
                                repeat(segmentCount) { index ->
                                    val isAttended = totalClasses > 0 && index < attended
                                    val segmentColor = if (isAttended) {
                                        androidx.compose.ui.graphics.lerp(
                                            Color(0xFF90CAF9),
                                            Color(0xFF3182F6),
                                            if (totalClasses <= 1) 1f else (index.toFloat() / (totalClasses - 1)).coerceIn(0f, 1f)
                                        )
                                    } else {
                                        if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE5E8EB)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(segmentColor)
                                    )
                                }
                            }
                        }
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
                    val zone = seat.substringBefore("-").trim().uppercase()
                    val floor = when (zone) {
                        "A", "B", "C", "D", "E" -> "1층"
                        "F", "G", "H", "I", "J" -> "2층"
                        else -> ""
                    }
                    val seatDisplayText = when {
                        !hasChapelData || seat.isBlank() || seat == "-" -> "정보 없음"
                        floor.isNotBlank() && !seat.startsWith(floor) -> "$floor $seat"
                        else -> seat
                    }

                    Text("좌석 정보", style = chapelBodyStyle(), color = SoongsilPalette.Slate400)
                    Text(
                        text = seatDisplayText,
                        style = chapelBodyStyle(),
                        color = if (hasChapelData && seat.isNotBlank() && seat != "-") Color(0xFF3182F6) else SoongsilPalette.Slate400,
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

// 대시보드 바로가기(졸업사정표, 등록금·장학금) 항목 컴포넌트입니다.
// @param label 버튼 하단에 노출될 텍스트 라벨입니다.
// @param onClick 클릭 시 호출될 콜백입니다.
// @param modifier 컴포저블에 적용할 Modifier입니다.
// @param resId 표시할 벡터 드로어블 리소스 ID입니다.
@Composable
private fun DashboardQuickLink(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, resId: Int) {
    val quickLinkShape = RoundedCornerShape(4.dp)
    // 다크모드에서는 밝은 흰색으로 표시하여 시인성을 높입니다.
    val iconTint = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .clip(quickLinkShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, quickLinkShape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(resId),
            contentDescription = label,
            modifier = Modifier.size(34.dp),
            tint = iconTint
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

// ─── Previews ───

// 대시보드 채플 섹션 (진행 중 - 12주차 케이스) 프리뷰입니다.
@Preview(name = "Dashboard Chapel Section 12 Weeks - Light", showBackground = true)
@Preview(
    name = "Dashboard Chapel Section 12 Weeks - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DashboardChapelSection12WeeksPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            DashboardChapelSection(
                seat = "A-1-2",
                totalClasses = 12,
                attended = 6,
                year = "2026",
                semester = "1학기",
                onDetailClick = {}
            )
        }
    }
}

// 대시보드 채플 섹션 (진행 중) 프리뷰입니다.
@Preview(name = "Dashboard Chapel Section In Progress - Light", showBackground = true)
@Preview(
    name = "Dashboard Chapel Section In Progress - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DashboardChapelSectionInProgressPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            DashboardChapelSection(
                seat = "C-13-4",
                totalClasses = 8,
                attended = 5,
                year = "2026",
                semester = "1학기",
                onDetailClick = {}
            )
        }
    }
}

// 대시보드 채플 섹션 (Pass 완료) 프리뷰입니다.
@Preview(name = "Dashboard Chapel Section Pass - Light", showBackground = true)
@Preview(
    name = "Dashboard Chapel Section Pass - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DashboardChapelSectionPassPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            DashboardChapelSection(
                seat = "C-13-4",
                totalClasses = 8,
                attended = 7,
                year = "2026",
                semester = "1학기",
                onDetailClick = {}
            )
        }
    }
}

// 대시보드 채플 섹션 (데이터 없음) 프리뷰입니다.
@Preview(name = "Dashboard Chapel Section Empty - Light", showBackground = true)
@Preview(
    name = "Dashboard Chapel Section Empty - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DashboardChapelSectionEmptyPreview() {
    SoongsilLifeAndroidTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(16.dp)) {
            DashboardChapelSection(
                seat = "",
                totalClasses = 0,
                attended = 0,
                year = "2026",
                semester = "1학기",
                onDetailClick = {}
            )
        }
    }
}
