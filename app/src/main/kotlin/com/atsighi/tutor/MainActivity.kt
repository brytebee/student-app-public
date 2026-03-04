package com.atsighi.tutor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.atsighi.engine.impl.HausaTeacherEngine
import com.atsighi.tutor.ui.screens.LiveConversationScreen
import com.atsighi.tutor.ui.theme.HausaTutorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the Private Engine (In a real app, use Hilt or Koin)
        val engine = HausaTeacherEngine("PROJECT_API_KEY") 

        setContent {
            HausaTutorTheme {
                LiveConversationScreen(engine = engine)
            }
        }
    }
}
