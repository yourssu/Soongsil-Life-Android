package com.yourssu.soongsil.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LoginBackground = Color(0xFF121212)
private val LoginSurface = Color(0xFF2C2C2E)
private val LoginBorder = Color(0xFF3A3A3C)
private val LoginText = Color(0xFFF5F5F5)
private val LoginHint = Color(0xFF8A8A8E)
private val LoginSubText = Color(0xFFA1A1AA)
private val LoginBlue = Color(0xFF0062FF)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (studentId: String, password: String) -> Unit = { _, _ -> },
    onForgotPasswordClick: () -> Unit = {}
) {
    var studentId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LoginScreenContent(
        studentId = studentId,
        password = password,
        onStudentIdChange = { studentId = it },
        onPasswordChange = { password = it },
        onLoginClick = { onLoginClick(studentId, password) },
        onForgotPasswordClick = onForgotPasswordClick,
        modifier = modifier
    )
}

@Composable
private fun LoginScreenContent(
    studentId: String,
    password: String,
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
            .background(LoginBackground)
            .safeDrawingPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, top = 56.dp, end = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFD9D9D9))
            )

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "유세인트에\n로그인해주세요",
                color = LoginText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp,
                letterSpacing = (-0.6).sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "학사 정보를 한눈에 확인할 수 있어요",
                color = LoginSubText,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
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
                color = LoginBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 6.dp, end = 4.dp)
                    .semantics { role = Role.Button }
                    .clickable(onClick = onForgotPasswordClick)
            )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoginBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "로그인",
                    color = Color.White,
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
            .background(LoginSurface)
            .border(width = 1.dp, color = LoginBorder, shape = shape)
            .padding(horizontal = 20.dp),
        textStyle = TextStyle(
            color = LoginText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            textAlign = TextAlign.End
        ),
        cursorBrush = SolidColor(LoginBlue),
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
                    color = LoginHint,
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
                color = LoginHint,
                style = Stroke(width = 1.4.dp.toPx())
            )
            drawCircle(
                color = LoginHint,
                radius = 2.dp.toPx(),
                center = center
            )

            if (!isPasswordVisible) {
                drawLine(
                    color = LoginHint,
                    start = Offset(2.dp.toPx(), size.height - 2.dp.toPx()),
                    end = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
                    strokeWidth = 1.4.dp.toPx()
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun LoginScreenPreview() {
    LoginScreenContent(
        studentId = "20231234",
        password = "password",
        onStudentIdChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onForgotPasswordClick = {}
    )
}
