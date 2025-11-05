package com.example.voicetutor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicetutor.ui.theme.*

enum class ButtonVariant {
    Primary,
    Secondary,
    Outline,
    Outlined,
    Ghost,
    Gradient,
    Danger
}

enum class ButtonSize {
    Small,
    Medium,
    Large
}

@Composable
fun VTButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    fullWidth: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    maxLines: Int = 1,
    lineHeightMultiplier: Float = 1.0f
) {
    val shape = RoundedCornerShape(16.dp)
    val contentPadding = when (size) {
        ButtonSize.Small -> PaddingValues(
            horizontal = 12.dp, 
            vertical = if (maxLines > 1) 6.dp else 8.dp
        )
        ButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    }
    
    val minHeight = when (size) {
        ButtonSize.Small -> if (maxLines > 1) 52.dp else 36.dp
        ButtonSize.Medium -> 40.dp
        ButtonSize.Large -> 44.dp
    }
    
    val fontSize = when (size) {
        ButtonSize.Small -> 12.sp
        ButtonSize.Medium -> 14.sp
        ButtonSize.Large -> 14.sp
    }

    val buttonModifier = modifier
        .let { if (fullWidth) it.fillMaxWidth() else it }
        .heightIn(min = minHeight)
        .clip(shape)
        .then(
            when (variant) {
                ButtonVariant.Primary -> Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryIndigo, PrimaryPurple)
                        )
                    )
                    .shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = PrimaryIndigo.copy(alpha = 0.15f),
                        spotColor = PrimaryIndigo.copy(alpha = 0.3f)
                    )

                ButtonVariant.Secondary -> Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Gray600, Gray700)
                        )
                    )
                    .shadow(
                        elevation = 4.dp,
                        shape = shape,
                        ambientColor = Gray500.copy(alpha = 0.15f),
                        spotColor = Gray500.copy(alpha = 0.2f)
                    )

                ButtonVariant.Outline -> Modifier
                    .border(
                        BorderStroke(1.5.dp, PrimaryIndigo),
                        shape = shape
                    )
                    .background(Color.White)
                    .shadow(
                        elevation = 2.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )

                ButtonVariant.Outlined -> Modifier
                    .border(
                        BorderStroke(1.5.dp, PrimaryIndigo),
                        shape = shape
                    )
                    .background(Color.White)
                    .shadow(
                        elevation = 2.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )

                ButtonVariant.Ghost -> Modifier
                    .background(Color.Transparent)

                ButtonVariant.Gradient -> Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6), // purple-500
                                Color(0xFFEC4899), // pink-500
                                PrimaryIndigo
                            )
                        )
                    )
                    .shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = Color(0xFF8B5CF6).copy(alpha = 0.3f),
                        spotColor = Color(0xFF8B5CF6).copy(alpha = 0.3f)
                    )

                ButtonVariant.Danger -> Modifier
                    .background(Error)
                    .shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = Error.copy(alpha = 0.15f),
                        spotColor = Error.copy(alpha = 0.3f)
                    )
            }
        )
        .clickable(enabled = enabled) { if (enabled) onClick() }

    val textColor = when (variant) {
        ButtonVariant.Primary, ButtonVariant.Secondary, ButtonVariant.Gradient, ButtonVariant.Danger -> Color.White
        ButtonVariant.Outline, ButtonVariant.Outlined, ButtonVariant.Ghost -> PrimaryIndigo
    }

    Box(
        modifier = buttonModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = if (maxLines > 1) Arrangement.Start else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let { 
                it()
                Spacer(modifier = Modifier.width(if (maxLines > 1) 4.dp else 8.dp))
            }
            
            Text(
                text = text,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = maxLines,
                overflow = if (maxLines == 1) androidx.compose.ui.text.style.TextOverflow.Ellipsis else androidx.compose.ui.text.style.TextOverflow.Visible,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = (fontSize.value * lineHeightMultiplier).sp
            )
            
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(if (maxLines > 1) 4.dp else 8.dp))
                it()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonPreview() {
    VoiceTutorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTButton(text = "Primary Button", onClick = {})
            VTButton(text = "Secondary Button", onClick = {}, variant = ButtonVariant.Secondary)
            VTButton(text = "Outline Button", onClick = {}, variant = ButtonVariant.Outline)
            VTButton(text = "Ghost Button", onClick = {}, variant = ButtonVariant.Ghost)
            VTButton(text = "Gradient Button", onClick = {}, variant = ButtonVariant.Gradient)
            VTButton(text = "Large Button", onClick = {}, size = ButtonSize.Large)
            VTButton(text = "Small Button", onClick = {}, size = ButtonSize.Small)
            VTButton(text = "Full Width", onClick = {}, fullWidth = true)
            VTButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}
