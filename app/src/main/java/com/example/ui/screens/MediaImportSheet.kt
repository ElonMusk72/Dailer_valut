package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeviceMediaItem
import com.example.data.DeviceMediaScanner
import com.example.data.VaultCategory
import kotlinx.coroutines.launch

private val SheetNavy = Color(0xFF1E2A38)
private val CardBg = Color(0xFFF3F4F6)
private val SelectedBorder = Color(0xFF1E2A38)
private val TextDark = Color(0xFF16161A)
private val TextMuted = Color(0xFF6B6B76)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFilesOptionsDialog(
  onDismiss: () -> Unit,
  onSelectCategory: (VaultCategory) -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = Color.White,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Select Media to Hide",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextDark,
            fontSize = 18.sp
          )
        )
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4 Main Options: Videos, Photos, Audio, Documents
      val options = listOf(
        Triple(VaultCategory.VIDEOS, "Videos", Icons.Default.Videocam to Color(0xFF3B82F6)),
        Triple(VaultCategory.PHOTOS, "Photos", Icons.Default.Image to Color(0xFF10B981)),
        Triple(VaultCategory.AUDIO, "Audio", Icons.Default.Headphones to Color(0xFFF59E0B)),
        Triple(VaultCategory.DOCUMENTS, "Documents", Icons.Default.Description to Color(0xFF8B5CF6))
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        options.take(2).forEach { (category, title, iconInfo) ->
          CategoryOptionCard(
            title = title,
            icon = iconInfo.first,
            iconColor = iconInfo.second,
            modifier = Modifier.weight(1f),
            tag = "option_${title.lowercase()}",
            onClick = {
              onDismiss()
              onSelectCategory(category)
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        options.drop(2).forEach { (category, title, iconInfo) ->
          CategoryOptionCard(
            title = title,
            icon = iconInfo.first,
            iconColor = iconInfo.second,
            modifier = Modifier.weight(1f),
            tag = "option_${title.lowercase()}",
            onClick = {
              onDismiss()
              onSelectCategory(category)
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
private fun CategoryOptionCard(
  title: String,
  icon: ImageVector,
  iconColor: Color,
  modifier: Modifier,
  tag: String,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier
      .height(100.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag(tag),
    colors = CardDefaults.cardColors(containerColor = CardBg),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(iconColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconColor,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.SemiBold,
          color = TextDark,
          fontSize = 14.sp
        )
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceMediaPickerSheet(
  category: VaultCategory,
  onDismiss: () -> Unit,
  onHideItems: (List<DeviceMediaItem>) -> Unit,
  onImportSystemUris: (List<Uri>) -> Unit
) {
  val context = LocalContext.current
  val scanner = remember { DeviceMediaScanner(context) }
  var isLoading by remember { mutableStateOf(true) }
  val mediaItems = remember { mutableStateListOf<DeviceMediaItem>() }
  val selectedItems = remember { mutableStateListOf<DeviceMediaItem>() }

  LaunchedEffect(category) {
    isLoading = true
    val items = scanner.loadDeviceMedia(category)
    mediaItems.clear()
    mediaItems.addAll(items)
    isLoading = false
  }

  // System file picker fallback/extension
  val systemPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      onImportSystemUris(uris)
      onDismiss()
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = Color.White,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Select ${category.title} to Hide",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = TextDark,
              fontSize = 20.sp
            ),
            modifier = Modifier.testTag("picker_title")
          )
          Text(
            text = "Found ${mediaItems.size} items on device",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Select All / System Picker row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = {
            if (selectedItems.size == mediaItems.size) {
              selectedItems.clear()
            } else {
              selectedItems.clear()
              selectedItems.addAll(mediaItems)
            }
          },
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = if (selectedItems.size == mediaItems.size && mediaItems.isNotEmpty()) "Deselect All" else "Select All",
            style = MaterialTheme.typography.labelMedium.copy(color = TextDark)
          )
        }

        OutlinedButton(
          onClick = {
            val mime = when (category) {
              VaultCategory.VIDEOS -> "video/*"
              VaultCategory.PHOTOS -> "image/*"
              VaultCategory.AUDIO -> "audio/*"
              VaultCategory.DOCUMENTS -> "*/*"
            }
            systemPicker.launch(mime)
          },
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.FileOpen,
            contentDescription = null,
            tint = TextDark,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Browse Storage",
            style = MaterialTheme.typography.labelMedium.copy(color = TextDark)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Media List
      if (isLoading) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(color = SheetNavy)
        }
      } else if (mediaItems.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No ${category.title.lowercase()} found on device storage.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(mediaItems, key = { it.id }) { item ->
            val isSelected = selectedItems.contains(item)
            DeviceMediaPickerRow(
              item = item,
              isSelected = isSelected,
              onClick = {
                if (isSelected) {
                  selectedItems.remove(item)
                } else {
                  selectedItems.add(item)
                }
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Button
      Button(
        onClick = {
          if (selectedItems.isNotEmpty()) {
            onHideItems(selectedItems.toList())
            onDismiss()
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_hide_button"),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = SheetNavy,
          contentColor = Color.White,
          disabledContainerColor = SheetNavy.copy(alpha = 0.4f),
          disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        enabled = selectedItems.isNotEmpty()
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (selectedItems.isEmpty()) "Select items to hide" else "Hide to Vault (${selectedItems.size})",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
private fun DeviceMediaPickerRow(
  item: DeviceMediaItem,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(
        width = if (isSelected) 1.5.dp else 0.dp,
        color = if (isSelected) SelectedBorder else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .testTag("device_media_${item.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) Color(0xFFEAEFF5) else CardBg
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon Box
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color.White),
        contentAlignment = Alignment.Center
      ) {
        val icon = when (item.category) {
          VaultCategory.VIDEOS -> Icons.Default.Videocam
          VaultCategory.PHOTOS -> Icons.Default.Image
          VaultCategory.AUDIO -> Icons.Default.Headphones
          VaultCategory.DOCUMENTS -> Icons.Default.Description
        }
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = SheetNavy,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = TextDark
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = item.formattedSize,
          style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Checkbox
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(if (isSelected) SheetNavy else Color.Transparent)
          .border(
            width = 1.5.dp,
            color = if (isSelected) SheetNavy else Color(0xFFB0B0B8),
            shape = CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Selected",
            tint = Color.White,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
