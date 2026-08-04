package com.yourssu.soongsil.screen.grade.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.screen.grade.model.GpaPoint
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

@Composable
fun GpaTrendChart(
    points: List<GpaPoint>,
    includeSeasonSemester: Boolean,
    onIncludeSeasonSemesterChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp),
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
                color = Color(0xFF191F28),
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
            val padLeft = 24.dp.toPx()
            val chartWidth = w - padLeft

            val gridColor = Color(0xFFF2F4F6)
            listOf(0.06f, 0.5f, 0.94f).forEach { ratio ->
                drawLine(gridColor, Offset(padLeft, h * ratio), Offset(w, h * ratio), 1.dp.toPx())
            }

            if (points.size >= 2) {
                val maxGpa = 4.5f
                // 군계절학기 등 gpa가 0.0인 특수한 경우를 위해 하한선을 융통성 있게 조정
                val minGpa = minOf(1.5f, points.minOf { it.gpa })

                val range = maxGpa - minGpa

                fun gpaToY(gpa: Float): Float = h * (1f - (gpa - minGpa) / range) * 0.88f + h * 0.06f
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
                    drawCircle(Color.White, radius + strokeW, Offset(cx, cy))
                    drawCircle(Color(0xFF0062FF), radius, Offset(cx, cy))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.semester,
                    fontSize = 10.sp,
                    fontWeight = if (pt.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (pt.isCurrent) Color(0xFF0062FF) else Color(0xFF8B95A1),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

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
