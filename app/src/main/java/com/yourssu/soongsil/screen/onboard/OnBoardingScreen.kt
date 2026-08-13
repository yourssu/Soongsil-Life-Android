package com.yourssu.soongsil.screen.onboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import com.yourssu.soongsil.ui.theme.SoongsilPalette

private val RequiredBadgeColor = Color(0xFFDC2626)
private val RequiredBadgeBackgroundColor = Color(0xFF3A1D1F)
private val OptionalBadgeBackgroundColor = Color(0xFF2C2C2E)
private val LightRequiredBadgeBackgroundColor = Color(0xFFFEE2E2)
private val DarkScreenBackgroundColor = Color(0xFF121212)
private val DarkSurfaceColor = Color(0xFF2C2C2E)
private val DarkPrimaryTextColor = Color(0xFFF5F5F5)
private val DarkSecondaryTextColor = Color(0xFF8A8A8E)
private val DarkMutedTextColor = Color(0xFFA1A1AA)

@Composable
fun OnBoardingScreen(
    viewModel: OnBoardingViewModel = hiltViewModel(),
    onServiceTermsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onMarketingTermsClick: () -> Unit = {},
    onTermsAgreementCompleted: () -> Unit = {}
) {
    LaunchedEffect(viewModel.isTermsAgreementCompleted) {
        if (viewModel.isTermsAgreementCompleted) {
            onTermsAgreementCompleted()
            viewModel.onTermsAgreementNavigationHandled()
        }
    }

    OnBoardingContent(
        serviceTermsAgreed = viewModel.serviceTermsAgreed,
        privacyPolicyAgreed = viewModel.privacyPolicyAgreed,
        marketingTermsAgreed = viewModel.marketingTermsAgreed,
        allTermsAgreed = viewModel.allTermsAgreed,
        canStart = viewModel.canStart,
        onAllTermsClick = viewModel::onAllTermsClick,
        onServiceTermsCheckedChange = viewModel::onServiceTermsClick,
        onPrivacyPolicyCheckedChange = viewModel::onPrivacyPolicyClick,
        onMarketingTermsCheckedChange = viewModel::onMarketingTermsClick,
        onServiceTermsDetailClick = onServiceTermsClick,
        onPrivacyPolicyDetailClick = onPrivacyPolicyClick,
        onMarketingTermsDetailClick = onMarketingTermsClick,
        onStartClick = viewModel::onStartClick
    )
}

@Composable
private fun OnBoardingContent(
    serviceTermsAgreed: Boolean,
    privacyPolicyAgreed: Boolean,
    marketingTermsAgreed: Boolean,
    allTermsAgreed: Boolean,
    canStart: Boolean,
    onAllTermsClick: () -> Unit,
    onServiceTermsCheckedChange: () -> Unit,
    onPrivacyPolicyCheckedChange: () -> Unit,
    onMarketingTermsCheckedChange: () -> Unit,
    onServiceTermsDetailClick: () -> Unit,
    onPrivacyPolicyDetailClick: () -> Unit,
    onMarketingTermsDetailClick: () -> Unit,
    onStartClick: () -> Unit
) {
    val isDarkTheme = isOnBoardingDarkTheme()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (isDarkTheme) DarkScreenBackgroundColor else MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(top = 64.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = "유세인트와\n함께 시작해볼까요?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkTheme) DarkPrimaryTextColor else MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "학사 정보를 안전하게 받아보려면 동의가 필요해요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) DarkSecondaryTextColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AllTermsRow(
                isAgreed = allTermsAgreed,
                onClick = onAllTermsClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            TermsRow(
                title = "서비스 이용약관",
                isRequired = true,
                isAgreed = serviceTermsAgreed,
                onCheckedChange = onServiceTermsCheckedChange,
                onDetailClick = onServiceTermsDetailClick
            )
            TermsRow(
                title = "개인정보 처리방침",
                isRequired = true,
                isAgreed = privacyPolicyAgreed,
                onCheckedChange = onPrivacyPolicyCheckedChange,
                onDetailClick = onPrivacyPolicyDetailClick
            )
            TermsRow(
                title = "마케팅 정보 수신 동의",
                isRequired = false,
                isAgreed = marketingTermsAgreed,
                onCheckedChange = onMarketingTermsCheckedChange,
                onDetailClick = onMarketingTermsDetailClick
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartClick,
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = if (isDarkTheme) {
                        DarkSurfaceColor
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    disabledContentColor = if (isDarkTheme) {
                        DarkSecondaryTextColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Text(
                    text = "동의하고 시작하기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AllTermsRow(
    isAgreed: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isOnBoardingDarkTheme()) {
        DarkSurfaceColor
    } else {
        SoongsilPalette.Gray150
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgreementCheckIcon(isAgreed = isAgreed, size = 24.dp)
        Spacer(modifier = Modifier.size(14.dp))
        Text(
            text = "전체 약관에 동의합니다",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isOnBoardingDarkTheme()) DarkMutedTextColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TermsRow(
    title: String,
    isRequired: Boolean,
    isAgreed: Boolean,
    onCheckedChange: () -> Unit,
    onDetailClick: () -> Unit
) {
    val isDarkTheme = isOnBoardingDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgreementCheckIcon(
            isAgreed = isAgreed,
            size = 22.dp,
            modifier = Modifier.clickable(onClick = onCheckedChange)
        )
        Spacer(modifier = Modifier.size(14.dp))
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TermsBadge(isRequired = isRequired)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkTheme) DarkPrimaryTextColor else MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "$title 자세히 보기",
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onDetailClick),
            tint = if (isDarkTheme) DarkSecondaryTextColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TermsBadge(isRequired: Boolean) {
    val isDarkTheme = isOnBoardingDarkTheme()
    val backgroundColor = when {
        isRequired && isDarkTheme -> RequiredBadgeBackgroundColor
        isRequired -> LightRequiredBadgeBackgroundColor
        isDarkTheme -> OptionalBadgeBackgroundColor
        else -> SoongsilPalette.Gray100
    }
    val contentColor = if (isRequired) RequiredBadgeColor else MaterialTheme.colorScheme.onSurfaceVariant

    Text(
        text = if (isRequired) "필수" else "선택",
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun AgreementCheckIcon(
    isAgreed: Boolean,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isOnBoardingDarkTheme()
    val backgroundColor = when {
        isAgreed -> MaterialTheme.colorScheme.primary
        isDarkTheme -> DarkSurfaceColor
        else -> Color.Transparent
    }
    val borderColor = when {
        isAgreed -> MaterialTheme.colorScheme.primary
        isDarkTheme -> DarkSecondaryTextColor
        else -> SoongsilPalette.Gray500
    }

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        if (isAgreed) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "선택됨",
                modifier = Modifier.padding(3.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// 현재 적용된 앱 테마가 다크 모드인지 확인합니다.
@Composable
private fun isOnBoardingDarkTheme(): Boolean =
    MaterialTheme.colorScheme.background == SoongsilPalette.Gray950

@Preview(showBackground = true)
@Composable
private fun OnBoardingScreenLightPreview() {
    SoongsilLifeAndroidTheme(darkTheme = false) {
        OnBoardingContent(
            serviceTermsAgreed = false,
            privacyPolicyAgreed = false,
            marketingTermsAgreed = false,
            allTermsAgreed = false,
            canStart = false,
            onAllTermsClick = {},
            onServiceTermsCheckedChange = {},
            onPrivacyPolicyCheckedChange = {},
            onMarketingTermsCheckedChange = {},
            onServiceTermsDetailClick = {},
            onPrivacyPolicyDetailClick = {},
            onMarketingTermsDetailClick = {},
            onStartClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnBoardingScreenDarkPreview() {
    SoongsilLifeAndroidTheme(darkTheme = true) {
        OnBoardingContent(
            serviceTermsAgreed = false,
            privacyPolicyAgreed = false,
            marketingTermsAgreed = false,
            allTermsAgreed = false,
            canStart = false,
            onAllTermsClick = {},
            onServiceTermsCheckedChange = {},
            onPrivacyPolicyCheckedChange = {},
            onMarketingTermsCheckedChange = {},
            onMarketingTermsDetailClick = {},
            onServiceTermsDetailClick = {},
            onPrivacyPolicyDetailClick = {},
            onStartClick = {}
        )
    }
}
