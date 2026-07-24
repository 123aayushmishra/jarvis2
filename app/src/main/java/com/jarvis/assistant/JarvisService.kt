package com.jarvis.assistant

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class JarvisService : Service(), RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var tts: TextToSpeech
    private val handler = Handler(Looper.getMainLooper())
    private var awake = false
    private var restartPending = false

    companion object {
        const val CHANNEL_ID = "jarvis_channel"
        const val NOTIF_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("jarvis_prefs", MODE_PRIVATE)
        tts = TextToSpeech(this) { }
        startForeground(NOTIF_ID, buildNotification("Jarvis sun raha hai..."))
        startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Jarvis", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Jarvis Active")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) return

        handler.post {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(this)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200)
            }
            speechRecognizer?.startListening(intent)
        }
    }

    private fun restartListening(delayMs: Long = 400) {
        if (restartPending) return
        restartPending = true
        handler.postDelayed({
            restartPending = false
            startListening()
        }, delayMs)
    }

    // ---- RecognitionListener callbacks ----
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val transcript = matches?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""
        handleTranscript(transcript)
        restartListening()
    }

    override fun onError(error: Int) {
        restartListening(800)
    }

    override fun onEndOfSpeech() { restartListening() }
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    // ---- Command handling ----
    private fun handleTranscript(text: String) {
        if (text.isBlank()) return

        val containsWake = text.contains("jarvis") || text.contains("jarves") || text.contains("jaarvis")

        if (containsWake) {
            val rest = text.replace(Regex("jarvis|jarves|jaarvis"), "").trim()
            if (rest.isNotEmpty() && runCommand(rest)) {
                awake = false
                updateNotification("Jarvis sun raha hai...")
            } else {
                awake = true
                updateNotification("Sun raha hu... command bolo")
                handler.postDelayed({ awake = false; updateNotification("Jarvis sun raha hai...") }, 6000)
            }
        } else if (awake) {
            if (runCommand(text)) {
                awake = false
                updateNotification("Jarvis sun raha hai...")
            }
        }
    }

    private fun runCommand(text: String): Boolean {
        return when {
            text.contains("chaitanya") -> { callNumber("Chaitanya", "num_chaitanya"); true }
            text.contains("gauri") -> { callNumber("Gauri didi", "num_gauri"); true }
            text.contains("tanni") -> { callNumber("Tanni", "num_tanni"); true }
            text.contains("mummy") -> { callNumber("Mummy", "num_mummy"); true }
            text.contains("papa") -> { callNumber("Papa", "num_papa"); true }
            text.contains("shankar") -> { callNumber("Shankar", "num_shankar"); true }
            text.contains("whatsapp") -> { openApp("com.whatsapp", "WhatsApp"); true }
            text.contains("instagram") -> { openApp("com.instagram.android", "Instagram"); true }
            text.contains("spotify") || text.contains("gana") || text.contains("song") || text.contains("album") -> {
                openSpotify(); true
            }
            else -> false
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun callNumber(name: String, prefKey: String) {
        val number = prefs.getString(prefKey, "") ?: ""
        if (number.isBlank()) {
            speak("$name ka number save nahi hai")
            return
        }
        speak("Calling $name")
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        val action = if (hasPermission) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val callIntent = Intent(action).apply {
            data = Uri.parse("tel:$number")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { startActivity(callIntent) } catch (e: SecurityException) {
            val dial = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(dial)
        }
    }

    private fun openApp(packageName: String, label: String) {
        speak("Opening $label")
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(launchIntent)
        } else {
            speak("$label installed nahi hai")
        }
    }

    private fun openSpotify() {
        speak("Playing your album")
        val link = prefs.getString("spotify_link", "") ?: ""
        val uri = if (link.isNotBlank()) Uri.parse(link) else Uri.parse("spotify:")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.spotify.music")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            openApp("com.spotify.music", "Spotify")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        tts.shutdown()
    }
}
