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
import com.atsighi.tutor.ui.theme.HausaIndigo
import com.atsighi.tutor.ui.theme.EcoLeafGreen

/**
 * Animated feedback card that slides up when the AI finishes evaluation.
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
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Friendly Status Header
                    Text(
                        text = if (it.isCorrect) "Ị gbalịala! (Well done!)" else "Dẹ̀mọ́ (Almost there!)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (it.isCorrect) EcoLeafGreen else HausaIndigo
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Teacher's Explanation (Naija flavored)
                    Text(
                        text = it.teacherFeedback,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                    )
                    
                    if (!it.isCorrect) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                        
                        Text(
                            text = "Correct Way:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = it.correctedText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = HausaIndigo
                        )
                    }
                }
            }
        }
    }
}
