package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.screen.grade.model.GpaPoint
import com.yourssu.soongsil.screen.grade.model.shortSemesterName
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import com.yourssu.soongsil.ui.theme.SoongsilPalette

// 성적 평점 추이 차트를 표시합니다.
@Composable
fun GpaTrendChart(
    points: List<GpaPoint>,
    includeSeasonSemester: Boolean,
    onIncludeSeasonSemesterChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val textMeasure = rememberTextMeasurer()
    val scoreTextStyle = TextStyle(
        color = SoongsilPalette.Blue600,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
    val axisTextStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) , RoundedCornerShape(20.dp))
            .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "성적 추이",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.3).sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeSeasonSemester,
                    onCheckedChange = onIncludeSeasonSemesterChange
                )
                Text(
                    text = if (includeSeasonSemester) "계절학기 포함" else "계절학기 제외",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8B95A1)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val w = size.width
            val h = size.height
            val padLeft = 36.dp.toPx()
            val chartWidth = w - padLeft

            listOf(0.06f, 0.5f, 0.94f).forEach { ratio ->
                drawLine(gridColor, Offset(padLeft, h * ratio), Offset(w, h * ratio), 1.dp.toPx())
            }
            listOf(
                "4.5" to 0.06f,
                "3.0" to 0.5f,
                "1.5" to 0.94f
            ).forEach { (label, ratio) ->
                val textLayout = textMeasure.measure(
                    text = label,
                    style = axisTextStyle
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = 0f,
                        y = h * ratio - textLayout.size.height / 2f
                    )
                )
            }

            if (points.size >= 2) {
                val maxGpa = 4.5f
                // 계절학기 등 gpa가 0.0으로 표시되는 특수한 경우를 위해 하한선을 융통성 있게 조정
                val minGpa = minOf(1.5f, points.minOf { it.gpa })

                val range = maxGpa - minGpa

                // 평점을 차트의 세로 좌표로 변환합니다.
                fun gpaToY(gpa: Float): Float = h * (1f - (gpa - minGpa) / range) * 0.88f + h * 0.06f
                // 학기 순서를 차트의 가로 좌표로 변환합니다.
                fun indexToX(i: Int): Float = padLeft + chartWidth * i / (points.size - 1)

                val areaPath = Path().apply {
                    moveTo(indexToX(0), gpaToY(points[0].gpa))
                    for (i in 1 until points.size) {
                        lineTo(indexToX(i), gpaToY(points[i].gpa))
                    }
                    lineTo(indexToX(points.lastIndex), h)
                    lineTo(indexToX(0), h)
                    close()
                }
                drawPath(
                    areaPath,
                    Brush.verticalGradient(
                        listOf(Color(0x300062FF), Color(0x050062FF))
                    )
                )

                val linePath = Path().apply {
                    moveTo(indexToX(0), gpaToY(points[0].gpa))
                    for (i in 1 until points.size) {
                        lineTo(indexToX(i), gpaToY(points[i].gpa))
                    }
                }
                drawPath(linePath, Color(0xFF0062FF), style = Stroke(2.dp.toPx()))

                points.forEachIndexed { i, pt ->
                    val cx = indexToX(i)
                    val cy =  gpaToY(pt.gpa)
                    val radius = if (pt.isCurrent) 6.dp.toPx() else 5.dp.toPx()
                    val strokeW = if (pt.isCurrent) 3.dp.toPx() else 2.dp.toPx()
                    val textLayout = textMeasure.measure(
                        text = pt.gpa.toString(),
                        style = scoreTextStyle
                    )
                    drawCircle(Color.White, radius + strokeW, Offset(cx, cy))
                    drawCircle(Color(0xFF0062FF), radius, Offset(cx, cy))
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            x = cx - textLayout.size.width / 2f,
                            y = cy - radius - strokeW - textLayout.size.height - 6.dp.toPx()
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.shortSemesterName,
                    fontSize = 10.sp,
                    fontWeight = if (pt.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (pt.isCurrent) Color(0xFF0062FF) else Color(0xFF8B95A1),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

// 밝은 모드 성적 추이 차트 미리보기를 표시합니다.
@Preview(name = "GPA Trend Chart Light", showBackground = true)
@Composable
private fun GpaTrendChartLightPreview() {
    SoongsilLifeAndroidTheme(darkTheme = false) {
        val includeSeasonSemester = remember { mutableStateOf(true) }

        GpaTrendChart(
            points = previewGpaPoints,
            includeSeasonSemester = includeSeasonSemester.value,
            onIncludeSeasonSemesterChange = { includeSeasonSemester.value = it }
        )
    }
}

// 어두운 모드 성적 추이 차트 미리보기를 표시합니다.
@Preview(name = "GPA Trend Chart Dark", showBackground = true)
@Composable
private fun GpaTrendChartDarkPreview() {
    SoongsilLifeAndroidTheme(darkTheme = true) {
        val includeSeasonSemester = remember { mutableStateOf(false) }

        GpaTrendChart(
            points = previewGpaPoints.filter { point ->
                !point.semester.contains("여름") && !point.semester.contains("겨울")
            },
            includeSeasonSemester = includeSeasonSemester.value,
            onIncludeSeasonSemesterChange = { includeSeasonSemester.value = it }
        )
    }
}

private val previewGpaPoints = listOf(
    GpaPoint("24-1", 3.2f),
    GpaPoint("24-여름", 4.0f),
    GpaPoint("24-2", 3.5f),
    GpaPoint("25-겨울", 3.8f),
    GpaPoint("25-1", 3.87f, isCurrent = true)
)
