//SpeakRezponse.kt
package com.example.smartpot

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class SpeakResponse(context: Context, private val onReady: (() -> Unit)? = null) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingTexts = mutableListOf<String>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("SpeakResponse", "Idioma no soportado o datos faltantes.")
                } else {
                    // Configurar la voz y la velocidad
                    setVoiceAndRate()

                    isInitialized = true
                    Log.i("SpeakResponse", "TTS inicializado con éxito.")
                    onReady?.invoke() // Notificar que TTS está listo
                    // Procesar textos pendientes
                    pendingTexts.forEach { speak(it) }
                    pendingTexts.clear()
                }
            } else {
                Log.e("SpeakResponse", "Error al inicializar TextToSpeech.")
            }
        }
    }

    private fun setVoiceAndRate() {
        // Cambiar la velocidad
        tts?.setSpeechRate(1.2f) // Ajusta la velocidad aquí

        // Cambiar la voz (opcional)
        val voices = tts?.voices
        voices?.forEach { voice ->
            if (voice.name.contains("en_us")) { // Por ejemplo, seleccionar una voz en inglés de EE.UU.
                tts?.voice = voice
                return@forEach
            }
        }

        // Si no se encuentra la voz, usa la predeterminada
        tts?.setVoice(tts?.defaultVoice)
    }

    fun speak(text: String) {
        print(text)
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("SpeakResponse", "TTS aún no está inicializado. Encolando texto.")
            pendingTexts.add(text) // Guardar en cola si no está inicializado
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()

    }
}