package com.atsighi.tutor.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.atsighi.tutor.ui.theme.HausaIndigo

/**
 * A central pulse button for recording audio.
 * The "pulse" animation triggers when the user holds the button.
 */
@Composable
fun PulseRecordButton(isRecording: Boolean, rmsLevel: Float = 0f, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Base scale from infinite transition
    val baseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Dynamic boost from RMS level (real-time voice power)
    // We additive the RMS to create a "reactive" feel
    val dynamicScale = baseScale + (rmsLevel * 0.4f)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
        // The breathing pulse ring
        if (isRecording) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = dynamicScale, scaleY = dynamicScale)) {
                drawCircle(color = HausaIndigo.copy(alpha = 0.25f))
            }
        }
        
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = HausaIndigo,
            contentColor = Color.White,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.Mic else Icons.Filled.MicNone,
                contentDescription = "Record",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
