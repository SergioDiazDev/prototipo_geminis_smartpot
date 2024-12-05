//MainActivity.ky
package com.example.smartpot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smartpot.ui.theme.SmartPotTheme


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SmartPotTheme {
        BakingScreen()
      }
    }
  }
}
