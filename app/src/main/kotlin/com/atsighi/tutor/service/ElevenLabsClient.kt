package com.atsighi.tutor.service

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ElevenLabsClient(private val apiKey: String) {
    private val client = OkHttpClient()
    private val BASE_URL = "https://api.elevenlabs.io/v1/text-to-speech"
    
    // Voice IDs
    private val MALAM_VOICE_ID = "pNInz6obpgDQGcFmaJgB" // Adam
    private val SECONDARY_VOICE_ID = "ErXwobaYiN019PkySvjV" // Antoni (High-quality contrast)
    private val ENGLISH_VOICE_ID = "EXAVITQu4vr4xnSDxMaL" // Bella (Clear English)

    data class SpeechResult(val success: Boolean, val errorMessage: String? = null)

    suspend fun generateSpeech(text: String, outputFile: File, slowMode: Boolean = false, voiceType: String = "TUTOR"): SpeechResult {
        if (apiKey.isBlank()) {
            val err = "API Key is blank!"
            android.util.Log.e("ElevenLabsClient", err)
            return SpeechResult(false, err)
        }
        
        android.util.Log.d("ElevenLabsClient", "Using Key (len: ${apiKey.length}) starting with: ${apiKey.take(4)}...")

        val mediaType = "application/json".toMediaType()
        val stability = if (slowMode) 0.8f else 0.5f
        val similarityBoost = 0.75f
        
        // Escape text for JSON
        val escapedText = text.replace("\\", "\\\\")
                             .replace("\"", "\\\"")
                             .replace("\n", "\\n")
                             .replace("\r", "\\r")

        val voiceId = when(voiceType) {
            "SECONDARY" -> SECONDARY_VOICE_ID
            "ENGLISH" -> ENGLISH_VOICE_ID
            else -> MALAM_VOICE_ID
        }

        val json = """
            {
                "text": "$escapedText",
                "model_id": "eleven_multilingual_v2",
                "voice_settings": {
                    "stability": $stability,
                    "similarity_boost": $similarityBoost
                }
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$BASE_URL/$voiceId")
            .addHeader("xi-api-key", apiKey.trim())
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "audio/mpeg")
            .post(json.toRequestBody(mediaType))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    val shortLine = errorBody.take(100).replace("\n", " ")
                    android.util.Log.e("ElevenLabsClient", "API FAILURE: ${response.code} - ${response.message}")
                    android.util.Log.e("ElevenLabsClient", "ERROR DETAILS: $errorBody")
                    
                    val userFriendlyError = if (response.code == 401) {
                        "Invalid API Key"
                    } else if (response.code == 429) {
                        "Quota Exceeded"
                    } else {
                        "Error ${response.code}: $shortLine"
                    }
                    return SpeechResult(false, userFriendlyError)
                }
                
                android.util.Log.i("ElevenLabsClient", "ELEVENLABS SUCCESS: Generated audio for voice $voiceType")
                
                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                SpeechResult(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SpeechResult(false, e.message ?: "Network error")
        }
    }
}
