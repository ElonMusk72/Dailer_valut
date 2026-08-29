package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.VaultPreferences
import com.example.ui.screens.DialerScreen
import com.example.ui.screens.PermissionsWizardScreen
import com.example.ui.screens.PinSetupScreen
import com.example.ui.screens.SecretVaultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VaultDarkBackground

enum class AppScreen {
  PERMISSIONS,
  PIN_SETUP,
  DIALER,
  VAULT
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = VaultDarkBackground
        ) {
          DialerVaultApp()
        }
      }
    }
  }
}

@Composable
fun DialerVaultApp() {
  val context = androidx.compose.ui.platform.LocalContext.current
  val prefs = remember { VaultPreferences(context) }

  var currentScreen by remember {
    mutableStateOf(
      when {
        !prefs.isPermissionsCompleted -> AppScreen.PERMISSIONS
        !prefs.hasPin() -> AppScreen.PIN_SETUP
        else -> AppScreen.DIALER
      }
    )
  }

  var currentPin by remember { mutableStateOf(prefs.vaultPin) }

  // Handle Android hardware back button
  BackHandler(enabled = currentScreen == AppScreen.VAULT) {
    currentScreen = AppScreen.DIALER
  }

  AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
      if (targetState == AppScreen.VAULT) {
        (slideInHorizontally { it } + fadeIn()).togetherWith(
          slideOutHorizontally { -it } + fadeOut()
        )
      } else {
        (fadeIn()).togetherWith(fadeOut())
      }
    },
    label = "main_screen_transition"
  ) { screen ->
    when (screen) {
      AppScreen.PERMISSIONS -> {
        PermissionsWizardScreen(
          onPermissionsFinished = {
            prefs.isPermissionsCompleted = true
            if (prefs.hasPin()) {
              currentScreen = AppScreen.DIALER
            } else {
              currentScreen = AppScreen.PIN_SETUP
            }
          }
        )
      }

      AppScreen.PIN_SETUP -> {
        PinSetupScreen(
          onPinCreated = { pin ->
            prefs.vaultPin = pin
            currentPin = pin
            currentScreen = AppScreen.DIALER
          }
        )
      }

      AppScreen.DIALER -> {
        DialerScreen(
          savedPin = currentPin ?: prefs.vaultPin,
          onOpenVault = {
            currentScreen = AppScreen.VAULT
          }
        )
      }

      AppScreen.VAULT -> {
        SecretVaultScreen(
          onLockVault = {
            currentScreen = AppScreen.DIALER
          }
        )
      }
    }
  }
}

