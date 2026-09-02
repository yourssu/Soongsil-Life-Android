package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.screen.grade.model.GpaPoint
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import java.util.Locale

// 성적 평점 추이 차트를 표시합니다.
@Composable
fun GpaTrendChart(
    points: List<GpaPoint>,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val gridColor = if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFE8EEF5)
    val textMeasure = rememberTextMeasurer()
    val axisTextStyle = TextStyle(
        color = if (isDarkTheme) Color(0xFF8A8A8E) else Color(0xFF8B95A1),
        fontSize = 11.sp,
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            val w = size.width
            val h = size.height
            val padLeft = 32.dp.toPx()
            val padTop = 32.dp.toPx()
            val padBottom = 16.dp.toPx()
            val chartWidth = w - padLeft - 12.dp.toPx()
            val chartHeight = h - padTop - padBottom

            // Y축 4.0, 3.0, 2.0, 1.0 가이드선 및 라벨
            val yLabels = listOf("4.0", "3.0", "2.0", "1.0")
            yLabels.forEachIndexed { index, label ->
                val ratio = index.toFloat() / (yLabels.size - 1)
                val lineY = padTop + chartHeight * ratio

                // 수평 가이드선
                drawLine(
                    color = gridColor,
                    start = Offset(padLeft, lineY),
                    end = Offset(w, lineY),
                    strokeWidth = 1.dp.toPx()
                )

                // Y축 라벨 텍스트
                val textLayout = textMeasure.measure(
                    text = label,
                    style = axisTextStyle
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = padLeft - textLayout.size.width - 8.dp.toPx(),
                        y = lineY - textLayout.size.height / 2f
                    )
                )
            }

            if (points.isNotEmpty()) {
                val maxGpa = 4.0f
                val minGpa = 1.0f
                val gpaRange = maxGpa - minGpa

                fun gpaToY(gpa: Float): Float {
                    val clamped = gpa.coerceIn(minGpa, maxGpa + 0.5f)
                    return padTop + chartHeight * (1f - (clamped - minGpa) / gpaRange)
                }

                fun indexToX(i: Int): Float {
                    return if (points.size == 1) {
                        padLeft + chartWidth / 2f
                    } else {
                        padLeft + chartWidth * i / (points.size - 1)
                    }
                }

                // 영역 그라데이션 채우기
                val areaPath = Path().apply {
                    moveTo(indexToX(0), gpaToY(points[0].gpa))
                    for (i in 1 until points.size) {
                        lineTo(indexToX(i), gpaToY(points[i].gpa))
                    }
                    lineTo(indexToX(points.lastIndex), padTop + chartHeight)
                    lineTo(indexToX(0), padTop + chartHeight)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x330062FF),
                            Color(0x050062FF)
                        ),
                        startY = padTop,
                        endY = padTop + chartHeight
                    )
                )

                // 차트 라인 그리기
                val linePath = Path().apply {
                    moveTo(indexToX(0), gpaToY(points[0].gpa))
                    for (i in 1 until points.size) {
                        lineTo(indexToX(i), gpaToY(points[i].gpa))
                    }
                }
                drawPath(
                    path = linePath,
                    color = Color(0xFF0062FF),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // 데이터 포인트 및 활성 툴팁 렌더링
                points.forEachIndexed { i, pt ->
                    val cx = indexToX(i)
                    val cy = gpaToY(pt.gpa)
                    val radius = if (pt.isCurrent) 4.5.dp.toPx() else 3.5.dp.toPx()

                    drawCircle(
                        color = Color(0xFF0062FF),
                        radius = radius,
                        center = Offset(cx, cy)
                    )

                    // 현재/선택된 학기 툴팁 말풍선 (예: 4.16)
                    if (pt.isCurrent) {
                        val tooltipText = String.format(Locale.US, "%.2f", pt.gpa)
                        val tooltipLayout = textMeasure.measure(
                            text = tooltipText,
                            style = TextStyle(
                                color = if (isDarkTheme) Color(0xFF5B9DFF) else Color(0xFF0062FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PretendardFontFamily
                            )
                        )
                        val padH = 8.dp.toPx()
                        val padV = 4.dp.toPx()
                        val tooltipW = tooltipLayout.size.width + padH * 2
                        val tooltipH = tooltipLayout.size.height + padV * 2
                        val tooltipY = cy - radius - 8.dp.toPx() - tooltipH
                        val tooltipX = (cx - tooltipW / 2f).coerceIn(padLeft, w - tooltipW)

                        // 툴팁 배경 둥근 박스
                        drawRoundRect(
                            color = if (isDarkTheme) Color(0xFF2C3240) else Color(0xFFE2E7EE),
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipW, tooltipH),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // 툴팁 하단 화살표
                        val arrowPath = Path().apply {
                            moveTo(cx - 3.5.dp.toPx(), tooltipY + tooltipH)
                            lineTo(cx + 3.5.dp.toPx(), tooltipY + tooltipH)
                            lineTo(cx, tooltipY + tooltipH + 3.5.dp.toPx())
                            close()
                        }
                        drawPath(arrowPath, if (isDarkTheme) Color(0xFF2C3240) else Color(0xFFE2E7EE))

                        // 툴팁 텍스트
                        drawText(
                            textLayoutResult = tooltipLayout,
                            topLeft = Offset(tooltipX + padH, tooltipY + padV)
                        )
                    }
                }
            }
        }

        // X축 학기 라벨 (1-1, 1-2, 2-1...)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.shortLabel,
                    fontSize = 11.sp,
                    fontFamily = PretendardFontFamily,
                    fontWeight = if (pt.isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (pt.isCurrent) Color(0xFF0062FF) else Color(0xFF8B95A1),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 학기 문자열을 '1-1', '1-2' 등 짧은 형식으로 반환합니다.
private val GpaPoint.shortLabel: String
    get() {
        val s = semester
        val match = Regex("""(\d{4})년?\s*([12])학기""").find(s)
        if (match != null) {
            val sem = match.groupValues[2]
            val yr = match.groupValues[1].takeLast(2)
            return "$yr-$sem"
        }
        return if (s.length >= 3) s.takeLast(3) else s
    }

// ─── Previews ───

private val previewGpaPoints = listOf(
    GpaPoint("2022 1학기", 2.8f),
    GpaPoint("2022 2학기", 3.0f),
    GpaPoint("2023 1학기", 4.16f, isCurrent = true),
    GpaPoint("2023 2학기", 3.7f),
    GpaPoint("2024 1학기", 3.4f),
    GpaPoint("2024 2학기", 3.5f),
    GpaPoint("2025 1학기", 3.8f),
    GpaPoint("2025 2학기", 3.9f)
)

@Preview(name = "GPA Trend Chart Light", showBackground = true)
@Composable
private fun GpaTrendChartLightPreview() {
    SoongsilLifeAndroidTheme(darkTheme = false) {
        GpaTrendChart(points = previewGpaPoints)
    }
}

@Preview(name = "GPA Trend Chart Dark", showBackground = true)
@Composable
private fun GpaTrendChartDarkPreview() {
    SoongsilLifeAndroidTheme(darkTheme = true) {
        GpaTrendChart(points = previewGpaPoints)
    }
}
