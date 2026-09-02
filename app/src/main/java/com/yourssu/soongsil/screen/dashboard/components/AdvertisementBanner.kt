package com.yourssu.soongsil.screen.dashboard.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

// 대시보드 하단에 표시되는 인앱 홍보 배너 컴포넌트입니다.
@Composable
fun AdvertisementBanner(
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "광고 페이지 열기",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .aspectRatio(3f)
            .clickable(onClick = onClick)
    )
}

// ─── Previews ───

@Preview(name = "광고 배너 - Light", showBackground = true)
@Preview(name = "광고 배너 - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AdvertisementBannerPreview() {
    SoongsilLifeAndroidTheme {
        Surface {
            AdvertisementBanner(
                imageUrl = "https://example.com/banner.png",
                onClick = {}
            )
        }
    }
}
