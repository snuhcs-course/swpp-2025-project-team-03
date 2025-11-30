package com.example.voicetutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicetutor.ui.theme.*

@Composable
fun VTTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    maxLines: Int = 1,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    val shape = RoundedCornerShape(12.dp)

    Column(modifier = modifier) {
        label?.let { labelText ->
            Text(
                text = labelText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray700,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    if (enabled) Color.White else Gray50,
                )
                .border(
                    width = if (isError) 2.dp else 1.dp,
                    color = when {
                        isError -> Error
                        !enabled -> Gray200
                        else -> Gray200
                    },
                    shape = shape,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            enabled = enabled,
            maxLines = maxLines,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = if (enabled) Color.Black else Gray400,
            ),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray400,
                    )
                }
                innerTextField()
            },
        )

        errorMessage?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextFieldPreview() {
    VoiceTutorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VTTextField(
                value = "",
                onValueChange = {},
                label = "이름",
                placeholder = "이름을 입력하세요",
            )

            VTTextField(
                value = "김학생",
                onValueChange = {},
                label = "이름",
                placeholder = "이름을 입력하세요",
            )

            VTTextField(
                value = "",
                onValueChange = {},
                label = "비고",
                placeholder = "추가 정보를 입력하세요",
                maxLines = 3,
            )

            VTTextField(
                value = "",
                onValueChange = {},
                label = "에러 상태",
                placeholder = "에러가 있는 필드",
                isError = true,
                errorMessage = "이 필드는 필수입니다",
            )
        }
    }
}
