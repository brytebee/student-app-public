package com.atsighi.tutor.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

import android.media.MediaPlayer
import com.atsighi.tutor.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AudioService(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false
    private var resultListener: ((String) -> Unit)? = null
    private var pendingSpeech: String? = null
    private var speakingJob: kotlinx.coroutines.Job? = null
    
    private val elevenLabsClient = ElevenLabsClient(BuildConfig.ELEVEN_LABS_API_KEY)
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Listening states
    var isListening = mutableStateOf(false)
        private set
    private var shouldBeListening = false
    private var lastRecognizerIntent: Intent? = null

    // Expose readiness state for UI animations
    var isReady = mutableStateOf(false)
        private set

    init {
        tts = TextToSpeech(context, this)
        setupSpeechRecognizer()
    }

    fun setListener(listener: (String) -> Unit) {
        resultListener = listener
    }

    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { Log.d("AudioService", "Ready for speech") }
                override fun onBeginningOfSpeech() { Log.d("AudioService", "Beginning of speech") }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { Log.d("AudioService", "End of speech") }
                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                        SpeechRecognizer.ERROR_SERVER -> "Error from server"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error"
                    }
                    Log.e("AudioService", "Speech error: $message")
                    
                    shouldBeListening = false
                    isListening.value = false
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        // User actually said something, stop the loop to process
                        shouldBeListening = false
                        isListening.value = false
                        resultListener?.invoke(matches[0])
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ha", "NG"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("AudioService", "Hausa language not supported, falling back to UK")
                tts?.setLanguage(Locale.UK)
            }
            
            isTtsInitialized = true
            isReady.value = true
            
            // Flush pending speech
            pendingSpeech?.let {
                speak(it)
                pendingSpeech = null
            }
        } else {
            Log.e("AudioService", "Initialization failed")
        }
    }

    private val mmsTtsEngine = com.atsighi.engine.tts.MmsTtsEngine(context)

    fun speak(
        text: String, 
        slowMode: Boolean = false, 
        voiceType: String = "TUTOR",
        onSegmentStart: ((com.atsighi.engine.core.VoiceSegment) -> Unit)? = null
    ) {
        speakSegments(
            listOf(com.atsighi.engine.core.VoiceSegment(text, voiceType)), 
            slowMode,
            onSegmentStart
        )
    }

    private var hasShownSuccessToast = false
    var onAllSegmentsDone: (() -> Unit)? = null // Called after all segments play — use to restart mic

    fun speakSegments(
        segments: List<com.atsighi.engine.core.VoiceSegment>,
        slowMode: Boolean = false,
        onSegmentStart: ((com.atsighi.engine.core.VoiceSegment) -> Unit)? = null
    ) {
        // Enforce exact mutual exclusivity: The mouth disables the ear immediately
        stopListening()
        stopSpeaking() // Ensure any existing job is completely cancelled
        
        speakingJob = scope.launch {
            for (segment in segments) {
                val text = segment.text
                val voiceType = segment.voiceType
                
                // Notify caller that this segment is starting
                launch(Dispatchers.Main) {
                    onSegmentStart?.invoke(segment)
                }

                // Route: ENGLISH segments → System TTS (MMS only knows Hausa)
                if (voiceType == "ENGLISH") {
                    Log.d("AudioService", "Routing ENGLISH to System TTS: $text")
                    kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
                        launch(Dispatchers.Main) {
                            if (isTtsInitialized) {
                                tts?.setLanguage(java.util.Locale.UK)
                                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "eng_${System.currentTimeMillis()}")
                                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                                    override fun onStart(utteranceId: String?) {}
                                    override fun onDone(utteranceId: String?) { if (cont.isActive) cont.resume(Unit) {} }
                                    override fun onError(utteranceId: String?) { if (cont.isActive) cont.resume(Unit) {} }
                                })
                            } else {
                                if (cont.isActive) cont.resume(Unit) {}
                            }
                        }
                    }
                    // 600ms pause so the Hausa voice doesn't cut in immediately — smoother handover
                    kotlinx.coroutines.delay(600)
                    continue
                }

                // Route: TUTOR / SECONDARY → Local Meta MMS (Hausa only)
                val audioData = mmsTtsEngine.synthesize(text)
                if (audioData != null) {
                    if (!hasShownSuccessToast) {
                        launch(Dispatchers.Main) {
                            android.widget.Toast.makeText(context, "✅ Meta MMS Hausa Voice Active", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        hasShownSuccessToast = true
                    }
                    playPcmData(audioData)
                    continue
                }

                // Final fallback to System TTS for Hausa (if MMS fails to load)
                Log.w("AudioService", "MMS failed, using System TTS for: $text")
                launch(Dispatchers.Main) {
                    if (isTtsInitialized) {
                        tts?.setLanguage(java.util.Locale("ha", "NG"))
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            }
            // All segments done — notify caller (e.g. to restart mic for user input)
            launch(Dispatchers.Main) {
                onAllSegmentsDone?.invoke()
            }
        }
    }

    private suspend fun playPcmData(data: FloatArray) {
        // Convert FloatArray to 16-bit PCM ByteArray
        val pcmData = ByteArray(data.size * 2)
        for (i in data.indices) {
            val sample = (data[i] * 32767).toInt().coerceIn(-32768, 32767).toShort()
            pcmData[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcmData[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }

        val tempFile = File(context.cacheDir, "mms_temp.wav")
        writeWavHeader(tempFile, pcmData.size)
        tempFile.appendBytes(pcmData)
        
        playAudioFileBlocking(tempFile)
    }

    private fun writeWavHeader(file: File, pcmDataSize: Int) {
        val fos = FileOutputStream(file)
        val totalDataLen = pcmDataSize + 36
        val byteRate = 16000 * 2 // 16kHz, 16-bit
        
        val header = ByteArray(44)
        header[0] = 'R'.toByte(); header[1] = 'I'.toByte(); header[2] = 'F'.toByte(); header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.toByte(); header[9] = 'A'.toByte(); header[10] = 'V'.toByte(); header[11] = 'E'.toByte()
        header[12] = 'f'.toByte(); header[13] = 'm'.toByte(); header[14] = 't'.toByte(); header[15] = ' '.toByte()
        header[16] = 16 // Header length
        header[20] = 1 // PCM
        header[22] = 1 // Mono
        header[24] = (16000 and 0xff).toByte()
        header[25] = ((16000 shr 8) and 0xff).toByte()
        header[26] = ((16000 shr 16) and 0xff).toByte()
        header[27] = ((16000 shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // Block align
        header[34] = 16 // Bits per sample
        header[36] = 'd'.toByte(); header[37] = 'a'.toByte(); header[38] = 't'.toByte(); header[39] = 'a'.toByte()
        header[40] = (pcmDataSize and 0xff).toByte()
        header[41] = ((pcmDataSize shr 8) and 0xff).toByte()
        header[42] = ((pcmDataSize shr 16) and 0xff).toByte()
        header[43] = ((pcmDataSize shr 24) and 0xff).toByte()
        
        fos.write(header)
        fos.close()
    }

    private suspend fun playAudioFileBlocking(file: File) {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener { 
                        it.release()
                        mediaPlayer = null
                        file.delete()
                        // Ensure playback logic completes
                        if (continuation.isActive) continuation.resume(Unit) { }
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("AudioService", "MediaPlayer Error: $what, $extra")
                        if (continuation.isActive) continuation.resume(Unit) { }
                        true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (continuation.isActive) continuation.resume(Unit) { }
            }
        }
    }

    fun stopSpeaking() {
        speakingJob?.cancel()
        speakingJob = null
        
        tts?.stop()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    fun startListening() {
        if (isListening.value) {
            Log.d("AudioService", "Already listening, ignoring double-start")
            return
        }

        // Enforce exact mutual exclusivity: The ear disables the mouth immediately
        stopSpeaking()
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ha-NG") // Hausa
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Sannu! Speak now...")
        }
        lastRecognizerIntent = intent
        shouldBeListening = true
        isListening.value = true
        
        // Ensure run on main thread just in case
        scope.launch(Dispatchers.Main) {
            speechRecognizer?.startListening(intent)
        }
    }

    fun stopListening() {
        shouldBeListening = false
        isListening.value = false
        
        scope.launch(Dispatchers.Main) {
            speechRecognizer?.stopListening()
        }
    }

    fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
