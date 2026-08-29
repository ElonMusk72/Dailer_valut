package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightBg = Color(0xFFFFFFFF)
private val LightKeyBg = Color(0xFFEBEBEF)
private val DarkButtonBg = Color(0xFF1E161C)
private val BorderNormal = Color(0xFFB0B0B8)
private val BorderFocused = Color(0xFF1E161C)
private val TextDark = Color(0xFF16161A)
private val TextGray = Color(0xFF6B6B76)
private val ErrorRed = Color(0xFFC93B2B)
private val KeypadKeyBg = Color(0xFFFFFFFF)

@Composable
fun PinSetupScreen(
  onPinCreated: (String) -> Unit
) {
  val context = LocalContext.current

  var pin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var activeField by remember { mutableStateOf(0) } // 0 = enter code, 1 = confirm code

  val codesMismatch = confirmPin.isNotEmpty() && pin != confirmPin
  val isReadyToContinue = pin.length >= 4 && pin == confirmPin

  fun handleContinue() {
    if (pin.length < 4) {
      Toast.makeText(context, "Please enter at least 4 digits", Toast.LENGTH_SHORT).show()
      return
    }
    if (pin != confirmPin) {
      Toast.makeText(context, "Codes don't match. Please re-enter.", Toast.LENGTH_SHORT).show()
      return
    }
    onPinCreated(pin)
  }

  fun handleKeypadPress(digit: String) {
    if (activeField == 0) {
      if (pin.length < 8) {
        pin += digit
        if (pin.length >= 4 && confirmPin.isEmpty()) {
          // Keep user focused or allow moving
        }
      }
    } else {
      if (confirmPin.length < 8) {
        confirmPin += digit
      }
    }
  }

  fun handleBackspace() {
    if (activeField == 0) {
      if (pin.isNotEmpty()) pin = pin.dropLast(1)
    } else {
      if (confirmPin.isNotEmpty()) {
        confirmPin = confirmPin.dropLast(1)
      } else {
        activeField = 0
      }
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = LightBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(36.dp))

      // Top Key Icon Circle
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(LightKeyBg)
          .testTag("pin_setup_key_icon"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Key,
          contentDescription = "Key Icon",
          tint = TextDark,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Title
      Text(
        text = "Create Your Secret Code",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          color = TextDark,
          fontSize = 24.sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("pin_setup_title")
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Subtitle
      Text(
        text = "This code will unlock the vault from the dialer.",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = TextGray,
          fontSize = 14.sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("pin_setup_subtitle")
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Enter Code Box
      CodeInputField(
        label = "Enter code",
        value = pin,
        isFocused = activeField == 0,
        onClick = { activeField = 0 },
        tag = "enter_code_field"
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Confirm Code Box
      CodeInputField(
        label = "Confirm code",
        value = confirmPin,
        isFocused = activeField == 1,
        onClick = { activeField = 1 },
        tag = "confirm_code_field"
      )

      // Mismatch Error Text
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(26.dp)
          .padding(top = 4.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        if (codesMismatch) {
          Text(
            text = "Codes don't match.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = ErrorRed,
              fontWeight = FontWeight.Medium,
              fontSize = 13.sp
            ),
            modifier = Modifier.testTag("pin_mismatch_error")
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Continue Button
      Button(
        onClick = { handleContinue() },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("pin_continue_button"),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DarkButtonBg,
          contentColor = Color.White,
          disabledContainerColor = DarkButtonBg.copy(alpha = 0.5f),
          disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        enabled = isReadyToContinue
      ) {
        Text(
          text = "Continue",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
          )
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      // On-Screen Keypad matching screenshot
      SetupKeypad(
        onDigit = { handleKeypadPress(it) },
        onBackspace = { handleBackspace() }
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
private fun CodeInputField(
  label: String,
  value: String,
  isFocused: Boolean,
  onClick: () -> Unit,
  tag: String
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(64.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(Color.White)
      .border(
        width = if (isFocused) 1.5.dp else 1.dp,
        color = if (isFocused) BorderFocused else BorderNormal,
        shape = RoundedCornerShape(10.dp)
      )
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) { onClick() }
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag(tag)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          color = TextGray,
          fontSize = 12.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = if (value.isEmpty()) "" else value,
        style = MaterialTheme.typography.bodyLarge.copy(
          color = TextDark,
          fontSize = 18.sp,
          fontWeight = FontWeight.Normal,
          letterSpacing = 1.sp
        )
      )
    }
  }
}

@Composable
private fun SetupKeypad(
  onDigit: (String) -> Unit,
  onBackspace: () -> Unit
) {
  val rows = listOf(
    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
    listOf("" to "", "0" to "+", "BACK" to "")
  )

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color(0xFFE4E4EB).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      .padding(6.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    rows.forEach { row ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        row.forEach { (main, sub) ->
          Box(
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
          ) {
            if (main == "BACK") {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.White)
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.Black.copy(alpha = 0.1f))
                  ) { onBackspace() }
                  .testTag("keypad_backspace"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Backspace,
                  contentDescription = "Backspace",
                  tint = TextDark,
                  modifier = Modifier.size(20.dp)
                )
              }
            } else if (main.isNotEmpty()) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.White)
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.Black.copy(alpha = 0.1f))
                  ) { onDigit(main) }
                  .testTag("keypad_digit_$main"),
                contentAlignment = Alignment.Center
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = main,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Medium,
                      fontSize = 18.sp,
                      color = TextDark
                    )
                  )
                  if (sub.isNotEmpty()) {
                    Text(
                      text = sub,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                      )
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
