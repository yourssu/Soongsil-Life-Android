package com.yourssu.soongsil.screen.plan

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import com.yourssu.soongsil.ui.theme.SoongsilPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PlanPdfScreen(
    title: String,
    pdfBytes: ByteArray,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    var pdfState by remember(pdfBytes) { mutableStateOf<PdfFileState>(PdfFileState.Loading) }
    val context = LocalContext.current

    LaunchedEffect(pdfBytes) {
        pdfState = withContext(Dispatchers.IO) {
            runCatching {
                val file = File.createTempFile("course_plan_", ".pdf", context.cacheDir)
                try {
                    file.writeBytes(pdfBytes)
                    val pageCount = ParcelFileDescriptor.open(
                        file,
                        ParcelFileDescriptor.MODE_READ_ONLY
                    ).use { descriptor ->
                        PdfRenderer(descriptor).use { it.pageCount }
                    }
                    check(pageCount > 0) { "표시할 PDF 페이지가 없습니다." }
                    PdfFileState.Success(file, pageCount)
                } catch (throwable: Throwable) {
                    file.delete()
                    throw throwable
                }
            }.getOrElse { throwable ->
                PdfFileState.Error(throwable.message ?: "PDF를 열지 못했습니다.")
            }
        }
    }

    val currentPdfState = pdfState
    DisposableEffect(currentPdfState) {
        onDispose {
            (currentPdfState as? PdfFileState.Success)?.file?.delete()
        }
    }

    PlanPdfContent(
        title = title,
        pdfState = pdfState,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

// 강의계획서 PDF 본문 화면입니다. 핀치 줌, 더블탭 확대 및 플로팅 확대/축소 컨트롤을 제공합니다.
@Composable
private fun PlanPdfContent(
    title: String,
    pdfState: PdfFileState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PlanPdfHeader(title = title, onBackClick = onBackClick)

        when (pdfState) {
            PdfFileState.Loading -> PlanPdfStateMessage(
                message = "강의계획서를 여는 중이에요.",
                showProgress = true,
                modifier = Modifier.weight(1f)
            )

            is PdfFileState.Error -> PlanPdfStateMessage(
                message = pdfState.message,
                modifier = Modifier.weight(1f)
            )

            is PdfFileState.Success -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // PDF 페이지 목록 (핀치 줌 및 드래그 제스처 지원)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.05f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 4f)
                                scale = newScale
                                offset = if (newScale > 1f) {
                                    offset + pan
                                } else {
                                    Offset.Zero
                                }
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 20.dp + bottomBarPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items((0 until pdfState.pageCount).toList()) { pageIndex ->
                        PdfPage(file = pdfState.file, pageIndex = pageIndex)
                    }
                }

                // 우측 하단 확대/축소 플로팅 컨트롤 바
                PdfZoomControls(
                    scale = scale,
                    onZoomIn = { scale = (scale + 0.5f).coerceAtMost(4f) },
                    onZoomOut = {
                        val newScale = (scale - 0.5f).coerceAtLeast(1f)
                        scale = newScale
                        if (newScale == 1f) offset = Offset.Zero
                    },
                    onResetZoom = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = bottomBarPadding)
                )
            }
        }
    }
}

// PDF 확대/축소 및 배율 초기화 플로팅 버튼 컴포넌트입니다.
@Composable
private fun PdfZoomControls(
    scale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        androidx.compose.ui.graphics.Color(0xFF2C2C2E).copy(alpha = 0.92f)
    } else {
        androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.95f)
    }
    val contentColor = if (isDark) androidx.compose.ui.graphics.Color.White else SoongsilPalette.Navy900
    val percentText = "${(scale * 100).toInt()}%"

    Surface(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        shadowElevation = 6.dp,
        border = BorderStroke(
            0.8.dp,
            if (isDark) androidx.compose.ui.graphics.Color(0xFF3E3E42) else SoongsilPalette.Gray175
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 축소 버튼
            IconButton(
                onClick = onZoomOut,
                enabled = scale > 1.0f,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "축소",
                    modifier = Modifier.size(16.dp),
                    tint = if (scale > 1.0f) contentColor else contentColor.copy(alpha = 0.3f)
                )
            }

            // 배율 텍스트 및 초기화 버튼
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = scale != 1.0f, onClick = onResetZoom)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = percentText,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PretendardFontFamily,
                    color = if (scale != 1.0f) androidx.compose.ui.graphics.Color(0xFF0062FF) else contentColor
                )
            }

            // 확대 버튼
            IconButton(
                onClick = onZoomIn,
                enabled = scale < 4.0f,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "확대",
                    modifier = Modifier.size(16.dp),
                    tint = if (scale < 4.0f) contentColor else contentColor.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun PlanPdfHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(48.dp))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun PdfPage(
    file: File,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.roundToPx() }.coerceAtLeast(1)
        var bitmap by remember(file, pageIndex, widthPx) { mutableStateOf<Bitmap?>(null) }
        var errorMessage by remember(file, pageIndex, widthPx) { mutableStateOf<String?>(null) }

        LaunchedEffect(file, pageIndex, widthPx) {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                        .use { descriptor ->
                            PdfRenderer(descriptor).use { renderer ->
                                renderer.openPage(pageIndex).use { page ->
                                    val renderWidth = (widthPx * 1.5f).toInt().coerceAtMost(1800)
                                    val renderHeight = (renderWidth * page.height.toFloat() / page.width)
                                        .toInt()
                                        .coerceAtLeast(1)
                                    createBitmap(
                                        renderWidth,
                                        renderHeight,
                                        Bitmap.Config.ARGB_8888
                                    ).also { pageBitmap ->
                                        pageBitmap.eraseColor(Color.WHITE)
                                        page.render(
                                            pageBitmap,
                                            null,
                                            null,
                                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                        )
                                    }
                                }
                            }
                        }
                }
            }
            result.onSuccess { bitmap = it }
                .onFailure { errorMessage = it.message ?: "페이지를 표시하지 못했습니다." }
        }

        val renderedBitmap = bitmap
        DisposableEffect(renderedBitmap) {
            onDispose { renderedBitmap?.recycle() }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 1.dp
        ) {
            when {
                bitmap != null -> Image(
                    bitmap = requireNotNull(bitmap).asImageBitmap(),
                    contentDescription = "강의계획서 ${pageIndex + 1}페이지",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )

                errorMessage != null -> PlanPdfStateMessage(
                    message = requireNotNull(errorMessage),
                    modifier = Modifier.height(180.dp)
                )

                else -> PlanPdfStateMessage(
                    message = "${pageIndex + 1}페이지를 불러오는 중이에요.",
                    showProgress = true,
                    modifier = Modifier.height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun PlanPdfStateMessage(
    message: String,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showProgress) CircularProgressIndicator()
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PlanLoadingDialog(
    title: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        PlanLoadingContent(
            title = title,
            onCancel = onCancel,
            modifier = modifier
        )
    }
}

@Composable
private fun PlanLoadingContent(
    title: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "강의계획서를 준비하고 있어요",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = title.ifBlank { "선택한 과목" },
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "원본 강의계획서를 PDF로 변환하고 있습니다. 최대 몇 분 정도 걸릴 수 있어요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            TextButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "불러오기 취소")
            }
        }
    }
}

@Composable
fun PlanErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "확인")
            }
        },
        title = { Text(text = "강의계획서를 열 수 없어요") },
        text = { Text(text = message) }
    )
}

private sealed interface PdfFileState {
    data object Loading : PdfFileState
    data class Success(val file: File, val pageCount: Int) : PdfFileState
    data class Error(val message: String) : PdfFileState
}

@Preview(name = "계획서 불러오기 - 라이트", showBackground = true)
@Preview(
    name = "계획서 불러오기 - 다크",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PlanLoadingContentPreview() {
    SoongsilLifeAndroidTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            PlanLoadingContent(
                title = "모바일프로그래밍",
                onCancel = {}
            )
        }
    }
}

@Preview(name = "PDF 로딩 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "PDF 로딩 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PlanPdfLoadingPreview() {
    SoongsilLifeAndroidTheme {
        PlanPdfContent(
            title = "모바일프로그래밍 강의계획서",
            pdfState = PdfFileState.Loading,
            onBackClick = {}
        )
    }
}

@Preview(name = "PDF 오류 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "PDF 오류 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PlanPdfErrorPreview() {
    SoongsilLifeAndroidTheme {
        PlanPdfContent(
            title = "모바일프로그래밍 강의계획서",
            pdfState = PdfFileState.Error("PDF 파일을 표시하지 못했습니다."),
            onBackClick = {}
        )
    }
}

@Preview(name = "PDF 줌 컨트롤 (100%) - 라이트", showBackground = true)
@Preview(name = "PDF 줌 컨트롤 (100%) - 다크", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PdfZoomControlsPreview() {
    SoongsilLifeAndroidTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            PdfZoomControls(
                scale = 1.0f,
                onZoomIn = {},
                onZoomOut = {},
                onResetZoom = {}
            )
        }
    }
}

@Preview(name = "PDF 줌 컨트롤 (200%) - 라이트", showBackground = true)
@Preview(name = "PDF 줌 컨트롤 (200%) - 다크", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PdfZoomControlsZoomedPreview() {
    SoongsilLifeAndroidTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            PdfZoomControls(
                scale = 2.0f,
                onZoomIn = {},
                onZoomOut = {},
                onResetZoom = {}
            )
        }
    }
}
