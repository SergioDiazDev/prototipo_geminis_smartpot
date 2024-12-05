// BakingScreen.kt
package com.example.smartpot

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.RESULTS_RECOGNITION
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*

@Composable
fun BakingScreen(
  bakingViewModel: BakingViewModel = viewModel()
) {
  // Crear instancia de SpeakResponse
  val context = LocalContext.current
  val speakResponse = remember(context) {
    SpeakResponse(context)
  }

  // Asegurar cierre de TextToSpeech
  DisposableEffect(Unit) {
    onDispose {
      speakResponse.shutdown()
    }
  }

  // Variables de estado
  val placeholderResult = stringResource(R.string.results_placeholder)
  var result by rememberSaveable { mutableStateOf(placeholderResult) }
  var isTextSpoken by rememberSaveable { mutableStateOf(false) } // Nueva variable para controlar si el texto fue hablado
  val uiState by bakingViewModel.uiState.collectAsState()
  var pregunta by rememberSaveable { mutableStateOf("") }

  var humedad by rememberSaveable { mutableStateOf(80) }
  var luz by rememberSaveable { mutableStateOf(15000) }
  var temperatura by rememberSaveable { mutableStateOf(25) }
  var etapa by rememberSaveable { mutableStateOf("Desarrollo") }
  var isHumedadEnabled by rememberSaveable { mutableStateOf(true) }
  var isLuzEnabled by rememberSaveable { mutableStateOf(true) }
  var isTemperaturaEnabled by rememberSaveable { mutableStateOf(true) }

  // Construir el prompt
  val prePrompt = buildString {
    appendLine("Imagina que eres una planta de hierbabuena (Mentha spicata), con una personalidad alegre, bromista y encantadora.")
    appendLine("Aquí están tus condiciones actuales:")

    if (isHumedadEnabled) appendLine("- Humedad: $humedad%")
    if (isLuzEnabled) appendLine("- Luz: $luz lumens")
    if (isTemperaturaEnabled) appendLine("- Temperatura: $temperatura°C")
    appendLine("- Etapa: $etapa")
    if (pregunta.isNotEmpty()) appendLine("Además, responde a la siguiente pregunta: $pregunta")
    else appendLine("Además, con base en estos datos, responde cómo te sientes y qué necesitas.")
    appendLine("Responde siempre con una frase corta, divertida y juguetona, como si fueras una hierbabuena feliz y bromista.")
  }

  // Setup SpeechRecognizer
  val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
  val speechRecognizerIntent = remember {
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
    }
  }

  // Método para iniciar la escucha de voz
  val startListening = {
    speechRecognizer.startListening(speechRecognizerIntent)
  }

  // Método para manejar el texto recogido y enviar el texto a la función que hace la solicitud al backend
  val sendTextToBackend = {
    // Enviar el texto recogido en 'pregunta' al backend o al flujo correspondiente
    if (pregunta.isNotEmpty()) {
      bakingViewModel.sendPrompt(null, prePrompt)
    } else {
      Toast.makeText(context, "No se ha reconocido texto", Toast.LENGTH_SHORT).show()
    }
  }

  // Layout principal
  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    Text(
      text = "SmartPot",
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.padding(16.dp)
    )

    // Panel de configuración
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Text("Configura tus variables:", style = MaterialTheme.typography.bodyMedium)

      // Panel de configuración
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Text("Configura tus variables:", style = MaterialTheme.typography.bodyMedium)

        Row(modifier = Modifier.padding(top = 8.dp)) {
          Text("Humedad", modifier = Modifier.align(Alignment.CenterVertically))
          Switch(
            checked = isHumedadEnabled,
            onCheckedChange = { isHumedadEnabled = it },
            modifier = Modifier.align(Alignment.CenterVertically)
          )
          if (isHumedadEnabled) {
            TextField(
              value = humedad.toString(),
              onValueChange = { humedad = it.toIntOrNull() ?: humedad },
              label = { Text("Humedad (%)") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Row(modifier = Modifier.padding(top = 8.dp)) {
          Text("Luz", modifier = Modifier.align(Alignment.CenterVertically))
          Switch(
            checked = isLuzEnabled,
            onCheckedChange = { isLuzEnabled = it },
            modifier = Modifier.align(Alignment.CenterVertically)
          )
          if (isLuzEnabled) {
            TextField(
              value = luz.toString(),
              onValueChange = { luz = it.toIntOrNull() ?: luz },
              label = { Text("Luz (lumens)") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Row(modifier = Modifier.padding(top = 8.dp)) {
          Text("Temperatura", modifier = Modifier.align(Alignment.CenterVertically))
          Switch(
            checked = isTemperaturaEnabled,
            onCheckedChange = { isTemperaturaEnabled = it },
            modifier = Modifier.align(Alignment.CenterVertically)
          )
          if (isTemperaturaEnabled) {
            TextField(
              value = temperatura.toString(),
              onValueChange = { temperatura = it.toIntOrNull() ?: temperatura },
              label = { Text("Temperatura (°C)") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

      }

      TextField(
        value = pregunta,
        label = { Text(stringResource(R.string.label_prompt)) },
        onValueChange = { pregunta = it },
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth()
      )
    }

    Button(
      onClick = {
        // Start voice input
        startListening()
      },
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(16.dp)
    ) {
      Text("Hablar")
    }

    Button(
      onClick = {
        // Send the prompt to the API or backend
        sendTextToBackend()
      },
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(16.dp)
    ) {
      Text(text = stringResource(R.string.action_go))
    }

    // Mostrar resultado de la API o backend
    if (uiState is UiState.Loading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    } else {
      val textColor = when (uiState) {
        is UiState.Error -> MaterialTheme.colorScheme.error
        is UiState.Success -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
      }
      result = when (uiState) {
        is UiState.Error -> (uiState as UiState.Error).errorMessage
        is UiState.Success -> (uiState as UiState.Success).outputText
        else -> result
      }

      if (uiState is UiState.Success) {
        // Solo llamar a speakResponse.speak si el texto no ha sido hablado previamente
        if (!isTextSpoken) {
          result = (uiState as UiState.Success).outputText
          speakResponse.speak(result) // Llamar a SpeakResponse para la salida de voz
          isTextSpoken = true // Marcar que el texto ya fue hablado
        }
      }

      val scrollState = rememberScrollState()
      Text(
        text = result,
        textAlign = TextAlign.Start,
        color = textColor,
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .padding(16.dp)
          .fillMaxSize()
          .verticalScroll(scrollState)
      )
    }
  }

  // Configuración de SpeechRecognizer
  speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
    override fun onResults(results: Bundle?) {
      val data = results?.getStringArrayList(RESULTS_RECOGNITION)
      if (data != null && data.isNotEmpty()) {
        pregunta = data[0] // Recibir la pregunta y actualizar el estado
        sendTextToBackend() // Enviar el texto recibido a la función de backend
        isTextSpoken = false // Reiniciar la bandera cuando el texto sea actualizado
      }
    }

    override fun onError(error: Int) {
      Toast.makeText(context, "Error de voz: $error", Toast.LENGTH_SHORT).show()
    }

    // Métodos requeridos pero no utilizados
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
  })
}
