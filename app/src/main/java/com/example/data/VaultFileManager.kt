package com.example.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VaultCategory(val title: String, val mimeFilter: String) {
  VIDEOS("Videos", "video/*"),
  PHOTOS("Photos", "image/*"),
  DOCUMENTS("Documents", "*/*"),
  AUDIO("Audio", "audio/*")
}

data class VaultFile(
  val id: String,
  val name: String,
  val file: File,
  val category: VaultCategory,
  val sizeBytes: Long,
  val dateAdded: Long,
  val mimeType: String
) {
  val formattedSize: String
    get() {
      if (sizeBytes <= 0) return "0 B"
      val units = arrayOf("B", "KB", "MB", "GB", "TB")
      val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
      val value = sizeBytes / Math.pow(1024.0, digitGroups.toDouble())
      return String.format(Locale.US, "%.1f %s", value, units[digitGroups.coerceIn(0, units.size - 1)])
    }

  val formattedDate: String
    get() {
      val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
      return sdf.format(Date(dateAdded))
    }
}

class VaultFileManager(private val context: Context) {

  private val baseVaultDir: File
    get() {
      val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, ".secret_vault")
      if (!dir.exists()) {
        dir.mkdirs()
      }
      // Create .nomedia file to ensure gallery and media scanners ignore this folder completely
      val nomedia = File(dir, ".nomedia")
      if (!nomedia.exists()) {
        try {
          nomedia.createNewFile()
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      return dir
    }

  init {
    ensureVaultStructure()
  }

  private fun ensureVaultStructure() {
    val root = baseVaultDir
    VaultCategory.values().forEach { category ->
      val catDir = File(root, category.name.lowercase())
      if (!catDir.exists()) {
        catDir.mkdirs()
      }
      val nomedia = File(catDir, ".nomedia")
      if (!nomedia.exists()) {
        try {
          nomedia.createNewFile()
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  private fun getCategoryDir(category: VaultCategory): File {
    val catDir = File(baseVaultDir, category.name.lowercase())
    if (!catDir.exists()) {
      catDir.mkdirs()
    }
    return catDir
  }

  fun getFiles(category: VaultCategory): List<VaultFile> {
    val dir = getCategoryDir(category)
    val files = dir.listFiles { file ->
      file.isFile && file.name != ".nomedia"
    } ?: emptyArray()

    return files.map { file ->
      val mimeType = getMimeTypeForFile(file)
      VaultFile(
        id = file.name,
        name = file.name,
        file = file,
        category = category,
        sizeBytes = file.length(),
        dateAdded = file.lastModified(),
        mimeType = mimeType
      )
    }.sortedByDescending { it.dateAdded }
  }

  fun getAllFilesCount(): Int {
    return VaultCategory.values().sumOf { getFiles(it).size }
  }

  fun importDeviceMediaItem(item: DeviceMediaItem): VaultFile? {
    return try {
      val targetDir = getCategoryDir(item.category)
      var targetFile = File(targetDir, item.name)
      if (targetFile.exists()) {
        val extension = targetFile.extension
        val baseName = targetFile.nameWithoutExtension
        val newName = "${baseName}_${System.currentTimeMillis()}${if (extension.isNotEmpty()) ".$extension" else ""}"
        targetFile = File(targetDir, newName)
      }

      if (item.uri.scheme == "content") {
        val inputStream: InputStream? = context.contentResolver.openInputStream(item.uri)
        if (inputStream != null) {
          FileOutputStream(targetFile).use { output ->
            inputStream.use { input ->
              input.copyTo(output)
            }
          }
        } else {
          targetFile.writeText("Protected secret data for ${item.name}")
        }
      } else {
        // Create demo hidden file representation
        targetFile.writeText("Secret Vault Protected file: ${item.name} (${item.formattedSize})")
      }

      VaultFile(
        id = targetFile.name,
        name = targetFile.name,
        file = targetFile,
        category = item.category,
        sizeBytes = if (targetFile.length() > 0) targetFile.length() else item.size,
        dateAdded = targetFile.lastModified(),
        mimeType = item.mimeType
      )
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun importFile(uri: Uri, category: VaultCategory): VaultFile? {
    return try {
      val contentResolver = context.contentResolver
      val fileName = getFileName(contentResolver, uri) ?: "vault_file_${System.currentTimeMillis()}"
      val targetDir = getCategoryDir(category)

      // Avoid collision
      var targetFile = File(targetDir, fileName)
      if (targetFile.exists()) {
        val extension = targetFile.extension
        val baseName = targetFile.nameWithoutExtension
        val newName = "${baseName}_${System.currentTimeMillis()}${if (extension.isNotEmpty()) ".$extension" else ""}"
        targetFile = File(targetDir, newName)
      }

      val inputStream: InputStream? = contentResolver.openInputStream(uri)
      if (inputStream != null) {
        val outputStream = FileOutputStream(targetFile)
        inputStream.use { input ->
          outputStream.use { output ->
            input.copyTo(output)
          }
        }

        val mime = contentResolver.getType(uri) ?: getMimeTypeForFile(targetFile)

        VaultFile(
          id = targetFile.name,
          name = targetFile.name,
          file = targetFile,
          category = category,
          sizeBytes = targetFile.length(),
          dateAdded = targetFile.lastModified(),
          mimeType = mime
        )
      } else {
        null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun deleteFile(vaultFile: VaultFile): Boolean {
    return try {
      if (vaultFile.file.exists()) {
        vaultFile.file.delete()
      } else {
        true
      }
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun createVideoClip(vaultFile: VaultFile, durationSeconds: Long = 5): VaultFile? {
    if (vaultFile.category != VaultCategory.VIDEOS) return null
    val targetDir = getCategoryDir(VaultCategory.VIDEOS)
    val clipName = "${vaultFile.file.nameWithoutExtension}_clip_${durationSeconds}s.mp4"
    val clipFile = File(targetDir, clipName)
    val extracted = com.example.util.VideoClipExtractor.extractClip(
      context = context,
      sourceFile = vaultFile.file,
      outputFile = clipFile,
      durationUs = durationSeconds * 1_000_000L
    ) ?: return null

    return VaultFile(
      id = extracted.name,
      name = extracted.name,
      file = extracted,
      category = VaultCategory.VIDEOS,
      sizeBytes = extracted.length(),
      dateAdded = extracted.lastModified(),
      mimeType = "video/mp4"
    )
  }

  fun createSampleNote(category: VaultCategory, title: String, content: String): VaultFile? {
    return try {
      val dir = getCategoryDir(category)
      val file = File(dir, "$title.txt")
      file.writeText(content)
      VaultFile(
        id = file.name,
        name = file.name,
        file = file,
        category = category,
        sizeBytes = file.length(),
        dateAdded = file.lastModified(),
        mimeType = "text/plain"
      )
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    var name: String? = null
    try {
      val cursor = contentResolver.query(uri, null, null, null, null)
      cursor?.use {
        if (it.moveToFirst()) {
          val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            name = it.getString(nameIndex)
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    if (name.isNullOrEmpty()) {
      name = uri.lastPathSegment
    }
    return name
  }

  private fun getMimeTypeForFile(file: File): String {
    val extension = file.extension
    if (extension.isNotEmpty()) {
      val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
      if (!mime.isNullOrEmpty()) return mime
    }
    return "*/*"
  }
}
