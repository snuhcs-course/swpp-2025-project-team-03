package com.example.voicetutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.theme.*

enum class CardVariant {
    Default,
    Elevated,
    Outlined,
    Gradient,
    Selected
}

@Composable
fun VTCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Default,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .then(
            when (variant) {
                CardVariant.Default -> Modifier
                    .background(Color.White)
                    .shadow(
                        elevation = 4.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )

                CardVariant.Elevated -> Modifier
                    .background(Color.White)
                    .shadow(
                        elevation = 12.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.06f),
                        spotColor = Color.Black.copy(alpha = 0.12f)
                    )

                CardVariant.Outlined -> Modifier
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Gray50,
                        shape = shape
                    )
                    .shadow(
                        elevation = 2.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.02f),
                        spotColor = Color.Black.copy(alpha = 0.04f)
                    )

                CardVariant.Gradient -> Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryIndigo,
                                LightIndigo
                            )
                        )
                    )
                    .shadow(
                        elevation = 16.dp,
                        shape = shape,
                        ambientColor = PrimaryIndigo.copy(alpha = 0.15f),
                        spotColor = PrimaryIndigo.copy(alpha = 0.3f)
                    )

                CardVariant.Selected -> Modifier
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        color = PrimaryIndigo,
                        shape = shape
                    )
                    .shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = PrimaryIndigo.copy(alpha = 0.1f),
                        spotColor = PrimaryIndigo.copy(alpha = 0.2f)
                    )
            }
        )
        .then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )

    Column(
        modifier = cardModifier.padding(20.dp),
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun CardPreview() {
    VoiceTutorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTCard {
                Text(
                    text = "Default Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This is a default card with some content.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }

            VTCard(variant = CardVariant.Elevated) {
                Text(
                    text = "Elevated Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This card has more elevation and shadow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }

            VTCard(variant = CardVariant.Outlined) {
                Text(
                    text = "Outlined Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This card has a border instead of shadow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }

            VTCard(
                variant = CardVariant.Gradient,
                onClick = { }
            ) {
                Text(
                    text = "Gradient Card (Clickable)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This card has a gradient background and is clickable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
        }
    }
}
