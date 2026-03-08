package com.atsighi.tutor.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.atsighi.tutor.ui.theme.HausaIndigo

/**
 * A global, reusable "anxiety-reducing" loading pulse.
 * This is used when the AI is initializing or thinking.
 */
@Composable
fun LoadingPulse(
    modifier: Modifier = Modifier,
    color: Color = HausaIndigo,
    size: Int = 100
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size.dp)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            drawCircle(color = color.copy(alpha = alpha))
        }
        
        // Inner stationary circle for focus
        Canvas(modifier = Modifier.size((size * 0.6).dp)) {
            drawCircle(color = color.copy(alpha = 0.1f))
        }
    }
}
