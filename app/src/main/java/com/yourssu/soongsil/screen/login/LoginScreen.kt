package com.yourssu.soongsil.screen.login

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.soongsil.R
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import com.yourssu.soongsil.ui.theme.SoongsilPalette

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onLoginClick: (studentId: String, password: String) -> Unit = { _, _ -> },
    onForgotPasswordClick: () -> Unit = {}
) {
    var studentId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        LoginScreenContent(
            studentId = studentId,
            password = password,
            isLoading = isLoading,
            errorMessage = errorMessage,
            // 학번 입력값에서는 숫자만 유지합니다.
            onStudentIdChange = { studentId = it.filter(Char::isDigit) },
            onPasswordChange = { password = it },
            onLoginClick = { onLoginClick(studentId, password) },
            onForgotPasswordClick = onForgotPasswordClick,
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            LoginLoadingOverlay()
        }
    }
}

@Composable
// 로그인 요청이 진행되는 동안 전체 화면 로딩 화면을 표시합니다.
private fun LoginLoadingOverlay() {
    val isDarkTheme = MaterialTheme.colorScheme.background == SoongsilPalette.Gray950
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color(0xFF1B2A4A),
            strokeWidth = 8.dp
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "로그인 중이에요",
            color = if (isDarkTheme) Color(0xFFF5F5F5) else MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "잠시만 기다려주세요",
            color = if (isDarkTheme) Color(0xFFA1A1AA) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun LoginScreenContent(
    studentId: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onStudentIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, top = 56.dp, end = 28.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp)),
                painter = painterResource(R.drawable.appicon),
                contentDescription = "앱 아이콘"
            )

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "유세인트에\n로그인해주세요",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp,
                letterSpacing = (-0.6).sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "학사 정보를 한눈에 확인할 수 있어요",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            LoginTextField(
                label = "학번",
                value = studentId,
                onValueChange = onStudentIdChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            PasswordTextField(
                value = password,
                onValueChange = onPasswordChange,
                onDone = {
                    focusManager.clearFocus()
                    onLoginClick()
                }
            )

            Text(
                text = "비밀번호를 잊으셨나요?",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 6.dp, end = 4.dp)
                    .semantics { role = Role.Button }
                    .clickable(onClick = onForgotPasswordClick)
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onLoginClick()
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (isLoading) "로그인 중..." else "로그인",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
            }
        }
    }
}

@Composable
private fun LoginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = shape)
            .padding(horizontal = 20.dp),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            textAlign = TextAlign.End
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.size(12.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    innerTextField()
                }
                trailingContent?.let {
                    Spacer(modifier = Modifier.size(4.dp))
                    it()
                }
            }
        }
    )
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    LoginTextField(
        label = "비밀번호",
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            PasswordVisibilityButton(
                isPasswordVisible = isPasswordVisible,
                onClick = { isPasswordVisible = !isPasswordVisible }
            )
        },
        modifier = modifier
    )
}

@Composable
private fun PasswordVisibilityButton(
    isPasswordVisible: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(32.dp)
            .semantics {
                contentDescription = if (isPasswordVisible) {
                    "비밀번호 숨기기"
                } else {
                    "비밀번호 표시"
                }
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val eyePath = Path().apply {
                moveTo(1.dp.toPx(), center.y)
                quadraticTo(center.x, 1.dp.toPx(), size.width - 1.dp.toPx(), center.y)
                quadraticTo(center.x, size.height - 1.dp.toPx(), 1.dp.toPx(), center.y)
            }

            drawPath(
                path = eyePath,
                color = iconColor,
                style = Stroke(width = 1.4.dp.toPx())
            )
            drawCircle(
                color = iconColor,
                radius = 2.dp.toPx(),
                center = center
            )

            if (!isPasswordVisible) {
                drawLine(
                    color = iconColor,
                    start = Offset(2.dp.toPx(), size.height - 2.dp.toPx()),
                    end = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
                    strokeWidth = 1.4.dp.toPx()
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true, widthDp = 402, heightDp = 874)
@Preview(
    name = "Dark",
    showBackground = true,
    widthDp = 402,
    heightDp = 874,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoginScreenPreview() {
    SoongsilLifeAndroidTheme {
        LoginScreenContent(
            studentId = "20231234",
            password = "password",
            isLoading = false,
            errorMessage = null,
            onStudentIdChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@Preview(name = "Loading Light", showBackground = true, widthDp = 402, heightDp = 874)
@Preview(
    name = "Loading Dark",
    showBackground = true,
    widthDp = 402,
    heightDp = 874,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoginLoadingScreenPreview() {
    SoongsilLifeAndroidTheme {
        LoginScreen(isLoading = true)
    }
}
