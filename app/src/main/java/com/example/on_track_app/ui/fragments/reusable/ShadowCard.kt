package com.example.on_track_app.ui.fragments.reusable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

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