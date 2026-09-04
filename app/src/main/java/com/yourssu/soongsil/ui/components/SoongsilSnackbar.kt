package com.yourssu.soongsil.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

// 앱 전반에서 일관된 스타일로 스낵바를 표시하는 커스텀 SnackbarHost 컴포넌트입니다.
// @param hostState 스낵바의 표시 및 제어를 담당하는 상태 객체입니다.
// @param modifier 컴포저블에 적용할 Modifier입니다.
@Composable
fun SoongsilSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        SoongsilSnackbar(snackbarData = snackbarData)
    }
}

// 숭실생활 디자인 시스템에 맞춘 커스텀 스낵바 UI 컴포넌트입니다.
// @param snackbarData 표시할 스낵바 데이터입니다.
// @param icon 스낵바 좌측에 노출할 아이콘입니다. 기본값은 WifiOff 아이콘입니다.
// @param modifier 컴포저블에 적용할 Modifier입니다.
@Composable
fun SoongsilSnackbar(
    snackbarData: SnackbarData,
    icon: ImageVector = Icons.Outlined.WifiOff,
    modifier: Modifier = Modifier
) {
    SoongsilSnackbarContent(
        message = snackbarData.visuals.message,
        icon = icon,
        modifier = modifier
    )
}

// 스낵바의 실제 내부 콘텐츠(아이콘 및 텍스트)를 렌더링하는 컴포저블입니다.
// @param message 스낵바에 노출할 메시지 텍스트입니다.
// @param icon 스낵바 좌측에 노출할 아이콘입니다.
// @param modifier 컴포저블에 적용할 Modifier입니다.
@Composable
fun SoongsilSnackbarContent(
    message: String,
    icon: ImageVector = Icons.Outlined.WifiOff,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

// 라이트 모드에서의 스낵바 프리뷰입니다.
@Preview(name = "Light Mode", showBackground = true, widthDp = 402)
@Composable
private fun SoongsilSnackbarLightPreview() {
    SoongsilLifeAndroidTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            SoongsilSnackbarContent(
                message = "인터넷에 연결되어 있지 않아 데이터를 불러올 수 없습니다."
            )
        }
    }
}

// 다크 모드에서의 스낵바 프리뷰입니다.
@Preview(
    name = "Dark Mode",
    showBackground = true,
    widthDp = 402,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SoongsilSnackbarDarkPreview() {
    SoongsilLifeAndroidTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            SoongsilSnackbarContent(
                message = "인터넷에 연결되어 있지 않아 데이터를 불러올 수 없습니다."
            )
        }
    }
}
