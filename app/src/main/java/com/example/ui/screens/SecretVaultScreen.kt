package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.DeviceMediaItem
import com.example.data.VaultCategory
import com.example.data.VaultFile
import com.example.data.VaultFileManager
import com.example.data.VaultPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val VaultNavy = Color(0xFF1E2A38)
private val VaultFabColor = Color(0xFF1A2B4C)
private val VaultLightBg = Color(0xFFFFFFFF)
private val VaultTextDark = Color(0xFF16161A)
private val VaultTextGray = Color(0xFF6B6B76)
private val VaultDivider = Color(0xFFE5E7EB)

// Card background palette matching image 2
private val CardColors = listOf(
  Color(0xFF869E83), // Sage green
  Color(0xFF7A96A2), // Dusty slate
  Color(0xFF7A9488), // Moss grey
  Color(0xFF6E8882), // Soft forest
  Color(0xFF7388A3), // Steel blue
  Color(0xFF7B8491)  // Slate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretVaultScreen(
  onLockVault: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val fileManager = remember { VaultFileManager(context) }
  val prefs = remember { VaultPreferences(context) }

  var selectedCategory by remember { mutableStateOf(VaultCategory.VIDEOS) }
  var filesList by remember { mutableStateOf<List<VaultFile>>(emptyList()) }
  var selectedFileForDetail by remember { mutableStateOf<VaultFile?>(null) }
  var showPlusOptionsDialog by remember { mutableStateOf(false) }
  var categoryForMediaPicker by remember { mutableStateOf<VaultCategory?>(null) }
  var showSettingsMenu by remember { mutableStateOf(false) }
  var showChangePinDialog by remember { mutableStateOf(false) }
  var showSearchDialog by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  fun refreshFiles() {
    filesList = fileManager.getFiles(selectedCategory)
  }

  LaunchedEffect(selectedCategory) {
    refreshFiles()
  }

  // Prepopulate sample items if vault is completely empty on first launch
  LaunchedEffect(Unit) {
    if (fileManager.getAllFilesCount() == 0) {
      withContext(Dispatchers.IO) {
        fileManager.createSampleNote(VaultCategory.VIDEOS, "Camera_01.mp4", "Hidden Video Data")
        fileManager.createSampleNote(VaultCategory.VIDEOS, "WhatsApp_Video.mp4", "Hidden Video Data")
        fileManager.createSampleNote(VaultCategory.VIDEOS, "Download_movie.mp4", "Hidden Video Data")
        fileManager.createSampleNote(VaultCategory.VIDEOS, "Class_Lecture.mp4", "Hidden Video Data")
        fileManager.createSampleNote(VaultCategory.VIDEOS, "Family_Trip.mp4", "Hidden Video Data")
        fileManager.createSampleNote(VaultCategory.VIDEOS, "Secret_Project.mp4", "Hidden Video Data")

        fileManager.createSampleNote(VaultCategory.AUDIO, "Voice_Recording_01.mp3", "Confidential voice memo")
        fileManager.createSampleNote(VaultCategory.AUDIO, "Call_Client_Meeting.m4a", "Client call records")

        fileManager.createSampleNote(VaultCategory.PHOTOS, "Personal_Photo_01.jpg", "Hidden Photo")
        fileManager.createSampleNote(VaultCategory.PHOTOS, "Receipt_Scan.png", "Financial receipt")

        fileManager.createSampleNote(VaultCategory.DOCUMENTS, "Private_Keys.txt", "Crypto seed backup")
        fileManager.createSampleNote(VaultCategory.DOCUMENTS, "Tax_Document_2026.pdf", "Personal tax return")
      }
      refreshFiles()
    }
  }

  Scaffold(
    containerColor = VaultLightBg,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = VaultLightBg,
          titleContentColor = VaultTextDark,
          actionIconContentColor = VaultTextDark,
          navigationIconContentColor = VaultTextDark
        ),
        navigationIcon = {
          IconButton(
            onClick = {
              Toast.makeText(context, "Secret Vault Menu", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("vault_menu_button")
          ) {
            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Menu",
              tint = VaultTextDark
            )
          }
        },
        title = {
          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Vault",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = VaultTextDark.copy(alpha = 0.85f)
              ),
              modifier = Modifier.testTag("vault_screen_title")
            )
          }
        },
        actions = {
          // Search Icon
          IconButton(
            onClick = { showSearchDialog = true },
            modifier = Modifier.testTag("vault_search_button")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = VaultTextDark
            )
          }

          // Settings Gear Icon with dropdown
          Box {
            IconButton(
              onClick = { showSettingsMenu = true },
              modifier = Modifier.testTag("vault_settings_button")
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = VaultTextDark
              )
            }

            DropdownMenu(
              expanded = showSettingsMenu,
              onDismissRequest = { showSettingsMenu = false },
              modifier = Modifier.background(Color.White)
            ) {
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Key,
                      contentDescription = null,
                      tint = VaultNavy,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Change PIN", color = VaultTextDark, fontWeight = FontWeight.Medium)
                  }
                },
                onClick = {
                  showSettingsMenu = false
                  showChangePinDialog = true
                },
                modifier = Modifier.testTag("menu_change_pin")
              )

              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Lock,
                      contentDescription = null,
                      tint = Color(0xFFDC2626),
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Lock Vault", color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                  }
                },
                onClick = {
                  showSettingsMenu = false
                  onLockVault()
                },
                modifier = Modifier.testTag("menu_lock_vault")
              )
            }
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showPlusOptionsDialog = true },
        containerColor = VaultFabColor,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(56.dp)
          .testTag("add_files_fab")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Files",
          tint = Color.White,
          modifier = Modifier.size(28.dp)
        )
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(VaultLightBg)
    ) {
      // Category Tab Row matching Image 2
      TabRow(
        selectedTabIndex = selectedCategory.ordinal,
        containerColor = VaultLightBg,
        contentColor = VaultNavy,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedCategory.ordinal]),
            color = VaultNavy,
            height = 2.5.dp
          )
        },
        divider = {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(VaultDivider)
          )
        }
      ) {
        VaultCategory.values().forEach { category ->
          val isSelected = selectedCategory == category

          Tab(
            selected = isSelected,
            onClick = { selectedCategory = category },
            modifier = Modifier.testTag("tab_${category.name.lowercase()}"),
            text = {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                Icon(
                  imageVector = getCategoryTabIcon(category),
                  contentDescription = category.title,
                  tint = if (isSelected) VaultNavy else VaultTextGray,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = if (category == VaultCategory.DOCUMENTS) "Apps" else category.title,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) VaultNavy else VaultTextGray,
                    fontSize = 12.sp
                  )
                )
              }
            }
          )
        }
      }

      val displayedFiles = if (searchQuery.isBlank()) {
        filesList
      } else {
        filesList.filter { it.name.contains(searchQuery, ignoreCase = true) }
      }

      if (displayedFiles.isEmpty()) {
        EmptyVaultPlaceholder(
          category = selectedCategory,
          onAddClicked = { categoryForMediaPicker = selectedCategory }
        )
      } else {
        // 3-Column Grid matching Image 2
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(displayedFiles, key = { it.id }) { vaultFile ->
            val colorIndex = Math.abs(vaultFile.name.hashCode()) % CardColors.size
            val cardColor = CardColors[colorIndex]

            VaultMediaGridCard(
              vaultFile = vaultFile,
              cardColor = cardColor,
              onClick = { selectedFileForDetail = vaultFile }
            )
          }
        }
      }
    }
  }

  // Plus (+) 4-Options Dialog (Videos, Photos, Audio, Documents)
  if (showPlusOptionsDialog) {
    AddFilesOptionsDialog(
      onDismiss = { showPlusOptionsDialog = false },
      onSelectCategory = { cat ->
        categoryForMediaPicker = cat
      }
    )
  }

  // Device Media Picker for Selected Category
  if (categoryForMediaPicker != null) {
    val targetCat = categoryForMediaPicker!!
    DeviceMediaPickerSheet(
      category = targetCat,
      onDismiss = { categoryForMediaPicker = null },
      onHideItems = { selectedMediaItems ->
        coroutineScope.launch(Dispatchers.IO) {
          var count = 0
          selectedMediaItems.forEach { item ->
            val imported = fileManager.importDeviceMediaItem(item)
            if (imported != null) count++
          }
          withContext(Dispatchers.Main) {
            Toast.makeText(context, "Hidden $count ${targetCat.title.lowercase()} in vault", Toast.LENGTH_SHORT).show()
            selectedCategory = targetCat
            refreshFiles()
          }
        }
      },
      onImportSystemUris = { uris ->
        coroutineScope.launch(Dispatchers.IO) {
          var count = 0
          uris.forEach { uri ->
            val imported = fileManager.importFile(uri, targetCat)
            if (imported != null) count++
          }
          withContext(Dispatchers.Main) {
            Toast.makeText(context, "Hidden $count file(s) in vault", Toast.LENGTH_SHORT).show()
            selectedCategory = targetCat
            refreshFiles()
          }
        }
      }
    )
  }

  // Change PIN Dialog
  if (showChangePinDialog) {
    ChangePinDialog(
      currentSavedPin = prefs.vaultPin ?: "",
      onDismiss = { showChangePinDialog = false },
      onPinChanged = { newPin ->
        prefs.vaultPin = newPin
        Toast.makeText(context, "Vault PIN updated successfully!", Toast.LENGTH_SHORT).show()
        showChangePinDialog = false
      }
    )
  }

  // Search Dialog
  if (showSearchDialog) {
    AlertDialog(
      onDismissRequest = { showSearchDialog = false },
      containerColor = Color.White,
      title = {
        Text("Search Hidden Files", fontWeight = FontWeight.Bold, color = VaultTextDark)
      },
      text = {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Enter filename...", color = VaultTextGray) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = VaultTextDark,
            unfocusedTextColor = VaultTextDark
          )
        )
      },
      confirmButton = {
        Button(
          onClick = { showSearchDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = VaultNavy)
        ) {
          Text("Search")
        }
      },
      dismissButton = {
        TextButton(onClick = {
          searchQuery = ""
          showSearchDialog = false
        }) {
          Text("Clear", color = VaultTextGray)
        }
      }
    )
  }

  // File Detail & Action Dialog
  if (selectedFileForDetail != null) {
    val file = selectedFileForDetail!!
    VaultFileDetailDialog(
      vaultFile = file,
      onDismiss = { selectedFileForDetail = null },
      onDelete = {
        fileManager.deleteFile(file)
        Toast.makeText(context, "File removed from vault", Toast.LENGTH_SHORT).show()
        selectedFileForDetail = null
        refreshFiles()
      }
    )
  }
}

@Composable
private fun VaultMediaGridCard(
  vaultFile: VaultFile,
  cardColor: Color,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("vault_item_${vaultFile.id}"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Thumbnail / Tile matching Image 2
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = cardColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        if (vaultFile.category == VaultCategory.PHOTOS && vaultFile.file.exists() && vaultFile.file.length() > 0) {
          AsyncImage(
            model = vaultFile.file,
            contentDescription = vaultFile.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          // Play icon in white circular disc matching image 2
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (vaultFile.category == VaultCategory.AUDIO) Icons.Default.Audiotrack
              else if (vaultFile.category == VaultCategory.DOCUMENTS) Icons.Default.Description
              else Icons.Default.PlayArrow,
              contentDescription = "Play",
              tint = cardColor.copy(alpha = 0.9f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Display Name under tile matching image 2
    Text(
      text = vaultFile.name,
      style = MaterialTheme.typography.bodySmall.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = VaultTextDark,
        lineHeight = 14.sp
      ),
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Start,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
private fun EmptyVaultPlaceholder(
  category: VaultCategory,
  onAddClicked: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.FolderOff,
          contentDescription = null,
          tint = VaultTextGray,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "No hidden ${category.title.lowercase()} yet",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = VaultTextDark
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Tap the + button to select and hide ${category.title.lowercase()} from your mobile phone.",
        style = MaterialTheme.typography.bodySmall.copy(
          color = VaultTextGray,
          lineHeight = 18.sp
        ),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = onAddClicked,
        colors = ButtonDefaults.buttonColors(containerColor = VaultFabColor),
        shape = RoundedCornerShape(20.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Hide ${category.title}")
      }
    }
  }
}

@Composable
private fun ChangePinDialog(
  currentSavedPin: String,
  onDismiss: () -> Unit,
  onPinChanged: (String) -> Unit
) {
  var oldPinInput by remember { mutableStateOf("") }
  var newPinInput by remember { mutableStateOf("") }
  var confirmNewPinInput by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Key,
          contentDescription = null,
          tint = VaultNavy,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Change Vault PIN",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = VaultTextDark
          )
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = oldPinInput,
          onValueChange = { oldPinInput = it.filter { c -> c.isDigit() }.take(6) },
          label = { Text("Current PIN") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = VaultTextDark,
            unfocusedTextColor = VaultTextDark
          )
        )

        OutlinedTextField(
          value = newPinInput,
          onValueChange = { newPinInput = it.filter { c -> c.isDigit() }.take(6) },
          label = { Text("New PIN (4-6 digits)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = VaultTextDark,
            unfocusedTextColor = VaultTextDark
          )
        )

        OutlinedTextField(
          value = confirmNewPinInput,
          onValueChange = { confirmNewPinInput = it.filter { c -> c.isDigit() }.take(6) },
          label = { Text("Confirm New PIN") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = VaultTextDark,
            unfocusedTextColor = VaultTextDark
          )
        )

        if (errorText != null) {
          Text(
            text = errorText ?: "",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color(0xFFDC2626),
              fontWeight = FontWeight.Medium
            )
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (oldPinInput != currentSavedPin) {
            errorText = "Current PIN is incorrect."
            return@Button
          }
          if (newPinInput.length < 4) {
            errorText = "New PIN must be at least 4 digits."
            return@Button
          }
          if (newPinInput != confirmNewPinInput) {
            errorText = "New PIN confirmation does not match."
            return@Button
          }
          onPinChanged(newPinInput)
        },
        colors = ButtonDefaults.buttonColors(containerColor = VaultNavy)
      ) {
        Text("Save PIN")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = VaultTextGray)
      }
    }
  )
}

@Composable
private fun VaultFileDetailDialog(
  vaultFile: VaultFile,
  onDismiss: () -> Unit,
  onDelete: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      shape = RoundedCornerShape(20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Hidden File Details",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = VaultTextDark
            )
          )
          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = VaultTextGray
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info details card
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          DetailRowItem(label = "Filename", value = vaultFile.name)
          DetailRowItem(label = "Type", value = vaultFile.category.title)
          DetailRowItem(label = "Size", value = vaultFile.formattedSize)
          DetailRowItem(label = "Protection", value = "Encrypted (.nomedia)")
          DetailRowItem(label = "Hidden Date", value = vaultFile.formattedDate)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Delete")
          }

          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = "Close", color = VaultTextDark)
          }
        }
      }
    }
  }
}

@Composable
private fun DetailRowItem(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(color = VaultTextGray)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        color = VaultTextDark
      ),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun getCategoryTabIcon(category: VaultCategory): ImageVector {
  return when (category) {
    VaultCategory.VIDEOS -> Icons.Default.Videocam
    VaultCategory.AUDIO -> Icons.Default.Headphones
    VaultCategory.PHOTOS -> Icons.Default.Image
    VaultCategory.DOCUMENTS -> Icons.Default.Apps
  }
}
