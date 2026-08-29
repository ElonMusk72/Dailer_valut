package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CallGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VaultAccent
import com.example.ui.theme.VaultDarkBackground
import com.example.ui.theme.VaultDarkCard
import com.example.ui.theme.VaultGold

@Composable
fun PermissionsWizardScreen(
  onPermissionsFinished: () -> Unit
) {
  val context = LocalContext.current
  var currentStep by remember { mutableIntStateOf(1) }

  // Permission launchers
  val contactsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { _ ->
    currentStep = 3
  }

  val callLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { _ ->
    currentStep = 4
  }

  val notificationLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { _ ->
    onPermissionsFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(VaultDarkBackground)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header Icon & Title
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(listOf(VaultAccent, Color(0xFF4F46E5)))
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Security",
          tint = Color.White,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Dialer Vault Setup",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        ),
        textAlign = TextAlign.Center
      )

      Text(
        text = "Initial Permissions Setup",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = TextSecondary
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Progress bar
      LinearProgressIndicator(
        progress = { currentStep / 4f },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = VaultAccent,
        trackColor = VaultDarkCard
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Step $currentStep of 4",
        style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Animated Card Dialog based on step
      AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
          (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
            slideOutHorizontally { width -> -width } + fadeOut()
          )
        },
        label = "wizard_step_animation"
      ) { step ->
        when (step) {
          1 -> StepAllFilesAccess(
            onGrantClicked = {
              requestAllFilesAccess(context)
              // Advance to step 2
              currentStep = 2
            },
            onSkipOrNext = {
              currentStep = 2
            }
          )
          2 -> StepGenericPermission(
            stepNumber = 2,
            icon = Icons.Default.Contacts,
            iconTint = VaultAccent,
            title = "Contacts Access",
            explanation = "This app needs access to your contacts for calling features.",
            onAllow = {
              contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
            },
            onDeny = {
              currentStep = 3
            }
          )
          3 -> StepGenericPermission(
            stepNumber = 3,
            icon = Icons.Default.Call,
            iconTint = CallGreen,
            title = "Manage Calls",
            explanation = "This app needs permission to manage calls so you can make calls from the dialer.",
            onAllow = {
              callLauncher.launch(Manifest.permission.CALL_PHONE)
            },
            onDeny = {
              currentStep = 4
            }
          )
          4 -> StepGenericPermission(
            stepNumber = 4,
            icon = Icons.Default.NotificationsActive,
            iconTint = VaultGold,
            title = "Notifications",
            explanation = "This app needs notification permission to alert you about vault activity.",
            onAllow = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              } else {
                onPermissionsFinished()
              }
            },
            onDeny = {
              onPermissionsFinished()
            }
          )
        }
      }
    }
  }
}

@Composable
private fun StepAllFilesAccess(
  onGrantClicked: () -> Unit,
  onSkipOrNext: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("step_all_files_dialog"),
    colors = CardDefaults.cardColors(containerColor = VaultDarkCard),
    shape = RoundedCornerShape(20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(VaultAccent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.FolderSpecial,
          contentDescription = "All Files",
          tint = VaultAccent,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Step 1: All Files Access",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "This app needs All Files Access to securely hide and protect your files, photos, and videos in the vault.",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = TextSecondary,
          lineHeight = 20.sp
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = onGrantClicked,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("grant_all_files_button"),
        colors = ButtonDefaults.buttonColors(containerColor = VaultAccent),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = "Grant Access",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedButton(
        onClick = onSkipOrNext,
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
          .testTag("skip_all_files_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
      ) {
        Text(text = "Next / Continue")
      }
    }
  }
}

@Composable
private fun StepGenericPermission(
  stepNumber: Int,
  icon: ImageVector,
  iconTint: Color,
  title: String,
  explanation: String,
  onAllow: () -> Unit,
  onDeny: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("step_${stepNumber}_dialog"),
    colors = CardDefaults.cardColors(containerColor = VaultDarkCard),
    shape = RoundedCornerShape(20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconTint,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Step $stepNumber: $title",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = explanation,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = TextSecondary,
          lineHeight = 20.sp
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = onDeny,
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("deny_button_step_$stepNumber"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
        ) {
          Text(text = "Deny")
        }

        Button(
          onClick = onAllow,
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("allow_button_step_$stepNumber"),
          colors = ButtonDefaults.buttonColors(containerColor = VaultAccent),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = "Allow",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
          )
        }
      }
    }
  }
}

private fun requestAllFilesAccess(context: Context) {
  try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      if (!Environment.isExternalStorageManager()) {
        try {
          val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
          }
          context.startActivity(intent)
        } catch (e: Exception) {
          val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
          }
          context.startActivity(intent)
        }
      }
    } else {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}
