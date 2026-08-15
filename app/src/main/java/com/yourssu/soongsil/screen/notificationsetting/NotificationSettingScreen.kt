package com.yourssu.soongsil.screen.notificationsetting

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme

private val BackgroundColor = Color(0xFFFFFFFF)
private val PrimaryTextColor = Color(0xFF0F172A)
private val SecondaryTextColor = Color(0xFF9CA3AF)
private val DividerColor = Color(0xFFF1F5F9)
private val ToggleOnColor = Color(0xFF0062FF)
private val ToggleOffColor = Color(0xFFE5E7EB)

@Composable
fun NotificationSettingScreen(
    pushNotificationEnabled: Boolean,
    onPushNotificationToggle: (Boolean) -> Unit,
    soundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    vibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    courseRegistrationEnabled: Boolean,
    onCourseRegistrationToggle: (Boolean) -> Unit,
    assignmentDeadlineEnabled: Boolean,
    onAssignmentDeadlineToggle: (Boolean) -> Unit,
    gradeReleaseEnabled: Boolean,
    onGradeReleaseToggle: (Boolean) -> Unit,
    chapelEnabled: Boolean,
    onChapelToggle: (Boolean) -> Unit,
    marketingEnabled: Boolean,
    onMarketingToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        NotificationSettingHeader(onBackClick = onBackClick)
        HorizontalDivider(thickness = 1.dp, color = DividerColor)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            NotificationSettingSection(
                title = "알림 받기",
                items = listOf(
                    NotificationToggleItem(
                        title = "푸시 알림",
                        description = "알림으로 소식 받기",
                        checked = pushNotificationEnabled,
                        onCheckedChange = onPushNotificationToggle
                    ),
                    NotificationToggleItem(
                        title = "알림음",
                        description = "새 알림 도착 시 소리",
                        checked = soundEnabled,
                        onCheckedChange = onSoundToggle
                    ),
                    NotificationToggleItem(
                        title = "진동",
                        description = "새 알림 도착 시 진동",
                        checked = vibrationEnabled,
                        onCheckedChange = onVibrationToggle
                    ),
                )
            )

            NotificationSettingSection(
                title = "알림 종류",
                items = listOf(
                    NotificationToggleItem(
                        title = "수강신청",
                        description = "수강신청 일정 알림",
                        checked = courseRegistrationEnabled,
                        onCheckedChange = onCourseRegistrationToggle
                    ),
                    NotificationToggleItem(
                        title = "과제 마감",
                        description = "마감 24시간 전 알림",
                        checked = assignmentDeadlineEnabled,
                        onCheckedChange = onAssignmentDeadlineToggle
                    ),
                    NotificationToggleItem(
                        title = "성적 발표",
                        description = "성적 공개 즉시 알림",
                        checked = gradeReleaseEnabled,
                        onCheckedChange = onGradeReleaseToggle
                    ),
                    NotificationToggleItem(
                        title = "채플 안내",
                        description = "채플 일정 알림",
                        checked = chapelEnabled,
                        onCheckedChange = onChapelToggle
                    ),
                )
            )

            NotificationSettingSection(
                title = "마케팅",
                items = listOf(
                    NotificationToggleItem(
                        title = "마케팅 알림",
                        description = "이벤트 · 혜택 알림",
                        checked = marketingEnabled,
                        onCheckedChange = onMarketingToggle
                    ),
                )
            )
        }
    }
}

@Composable
private fun NotificationSettingHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onBackClick() },
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Back",
            tint = PrimaryTextColor
        )
        Text(
            text = "알림 설정",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTextColor
        )
    }
}

private data class NotificationToggleItem(
    val title: String,
    val description: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
)

@Composable
private fun NotificationSettingSection(
    title: String,
    items: List<NotificationToggleItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = SecondaryTextColor
        )
        Spacer(Modifier.height(12.dp))
        items.forEachIndexed { index, item ->
            NotificationToggleRow(
                title = item.title,
                description = item.description,
                checked = item.checked,
                onCheckedChange = item.onCheckedChange
            )
            if (index < items.lastIndex) {
                HorizontalDivider(thickness = 1.dp, color = DividerColor)
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryTextColor
            )
            Text(
                text = description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = SecondaryTextColor
            )
        }
        NotificationToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun NotificationToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleShape = RoundedCornerShape(13.dp)
    val trackColor by animateColorAsState(
        targetValue = if (checked) ToggleOnColor else ToggleOffColor,
        label = "toggleTrackColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 0.dp,
        label = "toggleThumbOffset"
    )
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(toggleShape)
            .background(trackColor, toggleShape)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .background(Color.White, CircleShape)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationSettingScreenPreview() {
    var pushNotificationEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(false) }
    var courseRegistrationEnabled by remember { mutableStateOf(true) }
    var assignmentDeadlineEnabled by remember { mutableStateOf(true) }
    var gradeReleaseEnabled by remember { mutableStateOf(true) }
    var chapelEnabled by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(false) }

    SoongsilLifeAndroidTheme {
        NotificationSettingScreen(
            pushNotificationEnabled = pushNotificationEnabled,
            onPushNotificationToggle = { pushNotificationEnabled = it },
            soundEnabled = soundEnabled,
            onSoundToggle = { soundEnabled = it },
            vibrationEnabled = vibrationEnabled,
            onVibrationToggle = { vibrationEnabled = it },
            courseRegistrationEnabled = courseRegistrationEnabled,
            onCourseRegistrationToggle = { courseRegistrationEnabled = it },
            assignmentDeadlineEnabled = assignmentDeadlineEnabled,
            onAssignmentDeadlineToggle = { assignmentDeadlineEnabled = it },
            gradeReleaseEnabled = gradeReleaseEnabled,
            onGradeReleaseToggle = { gradeReleaseEnabled = it },
            chapelEnabled = chapelEnabled,
            onChapelToggle = { chapelEnabled = it },
            marketingEnabled = marketingEnabled,
            onMarketingToggle = { marketingEnabled = it },
        )
    }
}
