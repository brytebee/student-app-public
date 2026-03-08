package com.atsighi.tutor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.atsighi.engine.impl.HausaTeacherEngine
import com.atsighi.tutor.service.AudioService
import com.atsighi.tutor.ui.screens.LiveConversationScreen
import com.atsighi.tutor.ui.theme.HausaTutorTheme

class MainActivity : ComponentActivity() {
    private lateinit var audioService: AudioService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request microphone permission at start
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // Initialize the Private Engine (using secure GEMINI_API_KEY from BuildConfig)
        val engine = HausaTeacherEngine(BuildConfig.GEMINI_API_KEY) 

        // Initialize AudioService
        audioService = AudioService(this)

        setContent {
            HausaTutorTheme {
                LiveConversationScreen(engine = engine, audioService = audioService)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioService.onDestroy()
    }
}
