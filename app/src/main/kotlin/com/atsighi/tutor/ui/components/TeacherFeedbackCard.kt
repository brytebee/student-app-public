package com.atsighi.tutor.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atsighi.engine.core.EvaluationResult
import androidx.compose.foundation.shape.RoundedCornerShape
import com.atsighi.tutor.ui.theme.HausaDeepIndigo
import com.atsighi.tutor.ui.theme.EcoLeafGreen

/**
 * Animated feedback card that slides up when the AI finishes evaluation.
 * Designed with a premium, academic feel.
 */
@Composable
fun TeacherFeedbackCard(result: EvaluationResult?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = result != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        result?.let {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Friendly Status Header
                    Text(
                        text = if (it.isCorrect) "Madallah! (Excellent!)" else "Sannu! (Keep trying!)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (it.isCorrect) EcoLeafGreen else HausaDeepIndigo
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Teacher's Explanation
                    Text(
                        text = it.teacherFeedback,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                    )
                    
                    if (!it.isCorrect) {
                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                        
                        Text(
                            text = "A DAI-DAI TAKE (CORRECTLY):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = it.correctedText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = HausaDeepIndigo
                        )
                    }
                }
            }
        }
    }
}
