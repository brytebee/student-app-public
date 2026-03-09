package com.atsighi.tutor.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Eco
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.atsighi.tutor.ui.components.LoadingPulse
import com.atsighi.tutor.service.AudioService
import com.atsighi.tutor.ui.theme.HausaDeepIndigo
import com.atsighi.tutor.ui.theme.HausaSoftBlue
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isFromAi: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Proactive Conversational UI: A premium, high-status interface for Hausa learning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveConversationScreen(engine: ILanguageEngine, audioService: AudioService) {
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val isRecording by audioService.isListening
    val rmsLevel by audioService.rmsLevel
    val micError by audioService.micError
    var evaluation by remember { mutableStateOf<EvaluationResult?>(null) }
    var currentContext by remember { mutableStateOf(ConversationContext(timeOfDay = "morning")) }
    var optimizationLevel by remember { mutableStateOf(OptimizationLevel.NORMAL) }
    
    val scope = rememberCoroutineScope()
    val isAudioReady by audioService.isReady
    val listState = rememberLazyListState()

    // Initial Greeting — English intro + Hausa cue read by MMS
    LaunchedEffect(Unit) {
        val greeting = engine.getNextPrompt(currentContext)
        // chatMessages.add(ChatMessage(text = greeting, isFromAi = true)) // Removed monolithic message
        // Split: English part via System TTS, Hausa cue 'Mu fara!' via MMS
        audioService.speakSegments(
            listOf(
                com.atsighi.engine.core.VoiceSegment("It's morning in Kaduna. Welcome to our lesson. When you are ready, say:", "ENGLISH"),
                com.atsighi.engine.core.VoiceSegment("Mu fara!", "TUTOR")
            ),
            slowMode = true,
            onSegmentStart = { segment ->
                chatMessages.add(ChatMessage(text = segment.text, isFromAi = true))
            }
        )
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Show mic error toasts
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(micError) {
        micError?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Malam (Teacher)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text("Hausa Language Specialist", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    if (optimizationLevel == OptimizationLevel.ECO_MODE_ACTIVE) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Eco, "Eco Mode", tint = EcoLeafGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HausaDeepIndigo,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFBFBFE)) // Premium off-white
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Chat Window with clearance for the floating mic
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 180.dp), // Massive bottom padding for mic area
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(chatMessages) { message ->
                        ChatBubble(message)
                    }
                    
                    if (!isAudioReady) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                LoadingPulse(size = 40, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }

            // Correction Overlay (Compact, appears above control center)
            evaluation?.let {
                if (!it.isCorrect) {
                    TeacherFeedbackCard(
                        result = it,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 140.dp, start = 16.dp, end = 16.dp)
                            .shadow(12.dp, shape = RoundedCornerShape(24.dp))
                    )
                }
            }

            // Floating Control Center (Glassmorphism-lite effect)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .fillMaxWidth()
                    .height(100.dp)
                    .shadow(10.dp, shape = RoundedCornerShape(32.dp)),
                color = Color.White.copy(alpha = 0.85f), // Translucent white
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) // Glassy edge
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isRecording) "Listening..." else "Muna jin ka",
                            style = MaterialTheme.typography.titleMedium,
                            color = HausaDeepIndigo,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isRecording) "Say something in Hausa" else "Hold the mic to talk",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    
                    PulseRecordButton(isRecording = isRecording, rmsLevel = rmsLevel) {
                        if (!isRecording) {
                            audioService.startListening()
                        } else {
                            audioService.stopListening()
                        }
                    }
                }
            }

            // Handle transcription result (Invisible side effect)
            DisposableEffect(audioService) {
                audioService.setListener { text ->
                    if (text.isNotBlank()) {
                        chatMessages.add(ChatMessage(text = text, isFromAi = false))
                        scope.launch {
                            val result = engine.evaluateSpeech(UserInput(text, "hau"))
                            evaluation = result
                            
                            if (result.voiceSegments != null) {
                                audioService.speakSegments(
                                    segments = result.voiceSegments!!, 
                                    slowMode = result.slowMode,
                                    onSegmentStart = { segment ->
                                        chatMessages.add(ChatMessage(text = segment.text, isFromAi = true))
                                    }
                                )
                            } else {
                                audioService.speak(
                                    text = result.teacherFeedback, 
                                    slowMode = result.slowMode,
                                    onSegmentStart = { segment ->
                                        chatMessages.add(ChatMessage(text = segment.text, isFromAi = true))
                                    }
                                )
                            }
                        }
                    }
                }
                onDispose { 
                    audioService.setListener { }
                    audioService.onAllSegmentsDone = null
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isFromAi) Alignment.Start else Alignment.End
    val bubbleColor = if (message.isFromAi) HausaDeepIndigo else HausaSoftBlue
    val textColor = if (message.isFromAi) Color.White else HausaDeepIndigo
    val shape = if (message.isFromAi) {
        RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = shape,
            color = bubbleColor,
            shadowElevation = 2.dp
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
            )
        }
    }
}

