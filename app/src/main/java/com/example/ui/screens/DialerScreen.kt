package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallGreenPressed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VaultDarkBackground
import com.example.ui.theme.VaultDarkCard
import com.example.ui.theme.VaultKeypadButton

private data class KeypadItem(
  val digit: String,
  val letters: String,
  val secondaryInput: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialerScreen(
  savedPin: String?,
  onOpenVault: () -> Unit
) {
  val context = LocalContext.current
  var dialedNumber by remember { mutableStateOf("") }

  val keypadRows = remember {
    listOf(
      listOf(
        KeypadItem("1", ""),
        KeypadItem("2", "A B C"),
        KeypadItem("3", "D E F")
      ),
      listOf(
        KeypadItem("4", "G H I"),
        KeypadItem("5", "J K L"),
        KeypadItem("6", "M N O")
      ),
      listOf(
        KeypadItem("7", "P Q R S"),
        KeypadItem("8", "T U V"),
        KeypadItem("9", "W X Y Z")
      ),
      listOf(
        KeypadItem("*", ""),
        KeypadItem("0", "+", secondaryInput = "+"),
        KeypadItem("#", "")
      )
    )
  }

  fun handleKeypadPress(digit: String) {
    if (dialedNumber.length < 24) {
      dialedNumber += digit
    }
  }

  fun handleLongPress(keypadItem: KeypadItem) {
    if (keypadItem.secondaryInput != null && dialedNumber.length < 24) {
      dialedNumber += keypadItem.secondaryInput
    }
  }

  fun handleDelete() {
    if (dialedNumber.isNotEmpty()) {
      dialedNumber = dialedNumber.dropLast(1)
    }
  }

  fun handleClearAll() {
    dialedNumber = ""
  }

  fun handleCallPress() {
    if (dialedNumber.isEmpty()) {
      Toast.makeText(context, "Enter a number or PIN", Toast.LENGTH_SHORT).show()
      return
    }

    // Check if entered digits match vault PIN
    if (savedPin != null && dialedNumber == savedPin) {
      Toast.makeText(context, "🔓 Vault Opened", Toast.LENGTH_LONG).show()
      dialedNumber = ""
      onOpenVault()
    } else {
      // Normal phone call trigger -> redirect to system dialer
      try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
          data = Uri.parse("tel:${Uri.encode(dialedNumber)}")
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
      } catch (e: Exception) {
        Toast.makeText(context, "Could not open system dialer", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VaultDarkBackground)
      .padding(horizontal = 24.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top App Brand Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Phone,
        contentDescription = "Phone",
        tint = CallGreen,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Phone",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary,
          letterSpacing = 1.sp
        )
      )
    }

    // Number display area
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f, fill = false)
        .padding(vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      if (dialedNumber.isEmpty()) {
        Text(
          text = "Dial a number",
          style = MaterialTheme.typography.headlineSmall.copy(
            color = TextMuted,
            fontWeight = FontWeight.Normal
          ),
          textAlign = TextAlign.Center
        )
      } else {
        Text(
          text = dialedNumber,
          style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = if (dialedNumber.length <= 6) 3.sp else 1.sp
          ),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("dialed_number_display")
        )
      }
    }

    // Keypad Grid
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      keypadRows.forEach { rowItems ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          rowItems.forEach { item ->
            KeypadButton(
              item = item,
              onClick = { handleKeypadPress(item.digit) },
              onLongClick = { handleLongPress(item) }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Bottom Action Row: Spacer | Call Button | Delete Button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left dummy placeholder for balance
      Box(
        modifier = Modifier.size(68.dp),
        contentAlignment = Alignment.Center
      ) {
        // Empty balance container
      }

      // Call Button (Vibrant Emerald Green)
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(
            Brush.verticalGradient(
              listOf(CallGreen, CallGreenPressed)
            )
          )
          .testTag("call_button")
          .combinedClickable(
            onClick = { handleCallPress() }
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Call,
          contentDescription = "Call",
          tint = Color.White,
          modifier = Modifier.size(36.dp)
        )
      }

      // Delete Button (Backspace)
      Box(
        modifier = Modifier.size(68.dp),
        contentAlignment = Alignment.Center
      ) {
        if (dialedNumber.isNotEmpty()) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(VaultDarkCard)
              .testTag("delete_button")
              .combinedClickable(
                onClick = { handleDelete() },
                onLongClick = { handleClearAll() }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Backspace,
              contentDescription = "Backspace",
              tint = TextSecondary,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeypadButton(
  item: KeypadItem,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(76.dp)
      .clip(CircleShape)
      .background(VaultKeypadButton)
      .testTag("keypad_button_${item.digit}")
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = item.digit,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary,
          fontSize = if (item.digit in listOf("*", "#")) 30.sp else 28.sp
        )
      )
      if (item.letters.isNotEmpty()) {
        Text(
          text = item.letters,
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
          )
        )
      }
    }
  }
}
