package com.example.on_track_app.ui.fragments.reusable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.on_track_app.ui.theme.OnTrackAppTheme


@Composable
fun ExpandableCardItem(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            // Espacio cuando expandido
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableCardItemPreview() {
    OnTrackAppTheme(darkTheme = false) {
        ShadowCard { ExpandableCardItem(
            title = "Título de ejemplo",
            content = "Este es el contenido que aparece al expandir la tarjeta. "
        ) }

    }
}


@Composable
fun ShadowCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cornerRadius = with(LocalDensity.current) { 20.dp.toPx() }
    val shadowColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .graphicsLayer {
                shadowElevation = 0f
                shape = RoundedCornerShape(20.dp)
                clip = false
            }
            .drawBehind {
                drawRoundRect(
                    color = shadowColor,
                    cornerRadius = CornerRadius(cornerRadius),
                    topLeft = Offset(0f, 8f),
                    size = Size(size.width, size.height),
                    alpha = 0.6f,
                    blendMode = BlendMode.SrcOver
                )
            }
    ) {
        content()
    }
}
