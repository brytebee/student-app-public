package com.atsighi.tutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.atsighi.engine.core.*
import com.atsighi.tutor.ui.components.PulseRecordButton
import com.atsighi.tutor.ui.components.TeacherFeedbackCard
import com.atsighi.tutor.ui.theme.HausaIndigo
import com.atsighi.tutor.ui.theme.EcoLeafGreen
import kotlinx.coroutines.launch

/**
 * Proactive Conversational UI: The heart of the student experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveConversationScreen(engine: ILanguageEngine) {
    var isRecording by remember { mutableStateOf(false) }
    var evaluation by remember { mutableStateOf<EvaluationResult?>(null) }
    var currentContext by remember { mutableStateOf(ConversationContext(timeOfDay = "morning")) }
    var aiSpeechText by remember { mutableStateOf(engine.getNextPrompt(currentContext)) }
    var optimizationLevel by remember { mutableStateOf(OptimizationLevel.NORMAL) }
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Native Tutor AI") },
                actions = {
                    // Eco Indicator: Visual feedback of thermal safety
                    if (optimizationLevel == OptimizationLevel.ECO_MODE_ACTIVE) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Eco, "Eco Mode", tint = EcoLeafGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HausaIndigo,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // The Teacher's Prompt Bubble
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Malam (Teacher)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = aiSpeechText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            // The Listener (Centerpiece)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isRecording) "Listening..." else "Hold to speak",
                    style = MaterialTheme.typography.labelLarge,
                    color = HausaIndigo.copy(alpha = 0.7f)
                )
                PulseRecordButton(isRecording = isRecording) {
                    isRecording = !isRecording
                    if (!isRecording) {
                        // Trigger evaluation logic
                        scope.launch {
                            // Mocking a response for demonstration
                            evaluation = engine.evaluateSpeech(UserInput("Ina kwan", "hau"))
                        }
                    }
                }
            }

            // Correction Area
            TeacherFeedbackCard(result = evaluation)
        }
    }
}
