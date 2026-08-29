package com.example.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class DeviceMediaItem(
  val id: Long,
  val uri: Uri,
  val name: String,
  val size: Long,
  val mimeType: String,
  val durationMs: Long = 0,
  val path: String? = null,
  val category: VaultCategory
) {
  val formattedSize: String
    get() {
      if (size <= 0) return "0 B"
      val units = arrayOf("B", "KB", "MB", "GB")
      val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
      val value = size / Math.pow(1024.0, digitGroups.toDouble())
      return String.format(java.util.Locale.US, "%.1f %s", value, units[digitGroups.coerceIn(0, units.size - 1)])
    }
}

class DeviceMediaScanner(private val context: Context) {

  suspend fun loadDeviceMedia(category: VaultCategory): List<DeviceMediaItem> = withContext(Dispatchers.IO) {
    val items = mutableListOf<DeviceMediaItem>()

    try {
      when (category) {
        VaultCategory.VIDEOS -> {
          val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
          } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
          }

          val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DURATION
          )

          val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

          context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val durColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
              val id = cursor.getLong(idColumn)
              val name = cursor.getString(nameColumn) ?: "video_$id.mp4"
              val size = cursor.getLong(sizeColumn)
              val mime = cursor.getString(mimeColumn) ?: "video/mp4"
              val duration = if (durColumn != -1) cursor.getLong(durColumn) else 0L
              val uri = ContentUris.withAppendedId(collection, id)

              items.add(
                DeviceMediaItem(
                  id = id,
                  uri = uri,
                  name = name,
                  size = size,
                  mimeType = mime,
                  durationMs = duration,
                  category = VaultCategory.VIDEOS
                )
              )
            }
          }
        }

        VaultCategory.PHOTOS -> {
          val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
          } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
          }

          val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE
          )

          val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

          context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
              val id = cursor.getLong(idColumn)
              val name = cursor.getString(nameColumn) ?: "photo_$id.jpg"
              val size = cursor.getLong(sizeColumn)
              val mime = cursor.getString(mimeColumn) ?: "image/jpeg"
              val uri = ContentUris.withAppendedId(collection, id)

              items.add(
                DeviceMediaItem(
                  id = id,
                  uri = uri,
                  name = name,
                  size = size,
                  mimeType = mime,
                  category = VaultCategory.PHOTOS
                )
              )
            }
          }
        }

        VaultCategory.AUDIO -> {
          val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
          } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
          }

          val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DURATION
          )

          val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

          context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val durColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
              val id = cursor.getLong(idColumn)
              val name = cursor.getString(nameColumn) ?: "audio_$id.mp3"
              val size = cursor.getLong(sizeColumn)
              val mime = cursor.getString(mimeColumn) ?: "audio/mpeg"
              val duration = if (durColumn != -1) cursor.getLong(durColumn) else 0L
              val uri = ContentUris.withAppendedId(collection, id)

              items.add(
                DeviceMediaItem(
                  id = id,
                  uri = uri,
                  name = name,
                  size = size,
                  mimeType = mime,
                  durationMs = duration,
                  category = VaultCategory.AUDIO
                )
              )
            }
          }
        }

        VaultCategory.DOCUMENTS -> {
          val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
          } else {
            MediaStore.Files.getContentUri("external")
          }

          val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
          )

          val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_NONE} OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'application/%' OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'text/%'"
          val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

          context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
            val mimeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
              if (idColumn != -1) {
                val id = cursor.getLong(idColumn)
                val name = if (nameColumn != -1) cursor.getString(nameColumn) ?: "document_$id.pdf" else "doc_$id"
                val size = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L
                val mime = if (mimeColumn != -1) cursor.getString(mimeColumn) ?: "application/octet-stream" else "application/pdf"
                val uri = ContentUris.withAppendedId(collection, id)

                items.add(
                  DeviceMediaItem(
                    id = id,
                    uri = uri,
                    name = name,
                    size = size,
                    mimeType = mime,
                    category = VaultCategory.DOCUMENTS
                  )
                )
              }
            }
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // If device/emulator has no local media files created yet, populate mockable real representations
    // so user can immediately test selecting and hiding items as shown in image 2
    if (items.isEmpty()) {
      items.addAll(getDemoDeviceMedia(category))
    }

    items
  }

  private fun getDemoDeviceMedia(category: VaultCategory): List<DeviceMediaItem> {
    return when (category) {
      VaultCategory.VIDEOS -> listOf(
        DeviceMediaItem(101, Uri.parse("demo://camera_01.mp4"), "Camera_01.mp4", 14_800_000, "video/mp4", 45000, null, category),
        DeviceMediaItem(102, Uri.parse("demo://whatsapp_video.mp4"), "WhatsApp_Video.mp4", 8_200_000, "video/mp4", 28000, null, category),
        DeviceMediaItem(103, Uri.parse("demo://download_movie.mp4"), "Download_movie.mp4", 52_000_000, "video/mp4", 180000, null, category),
        DeviceMediaItem(104, Uri.parse("demo://class_lecture.mp4"), "Class_Lecture.mp4", 31_500_000, "video/mp4", 120000, null, category),
        DeviceMediaItem(105, Uri.parse("demo://family_trip.mp4"), "Family_Trip.mp4", 22_100_000, "video/mp4", 75000, null, category),
        DeviceMediaItem(106, Uri.parse("demo://secret_project.mp4"), "Secret_Project.mp4", 19_400_000, "video/mp4", 62000, null, category)
      )
      VaultCategory.PHOTOS -> listOf(
        DeviceMediaItem(201, Uri.parse("demo://dcim_camera_101.jpg"), "DCIM_Camera_101.jpg", 3_400_000, "image/jpeg", 0, null, category),
        DeviceMediaItem(202, Uri.parse("demo://screenshot_receipt.png"), "Screenshot_Receipt.png", 1_100_000, "image/png", 0, null, category),
        DeviceMediaItem(203, Uri.parse("demo://passport_copy.jpg"), "Passport_Scan_Copy.jpg", 2_800_000, "image/jpeg", 0, null, category),
        DeviceMediaItem(204, Uri.parse("demo://party_photo.jpg"), "Party_Celebration.jpg", 4_100_000, "image/jpeg", 0, null, category),
        DeviceMediaItem(205, Uri.parse("demo://id_card_front.png"), "National_ID_Front.png", 1_900_000, "image/png", 0, null, category)
      )
      VaultCategory.AUDIO -> listOf(
        DeviceMediaItem(301, Uri.parse("demo://voice_recording_01.m4a"), "Voice_Memo_Meeting.m4a", 4_200_000, "audio/mp4", 95000, null, category),
        DeviceMediaItem(302, Uri.parse("demo://call_record_client.mp3"), "Call_Recording_Important.mp3", 6_800_000, "audio/mpeg", 140000, null, category),
        DeviceMediaItem(303, Uri.parse("demo://audio_journal.aac"), "Audio_Journal_2026.aac", 3_100_000, "audio/aac", 85000, null, category)
      )
      VaultCategory.DOCUMENTS -> listOf(
        DeviceMediaItem(401, Uri.parse("demo://financial_statement.pdf"), "Financial_Statement_2026.pdf", 1_250_000, "application/pdf", 0, null, category),
        DeviceMediaItem(402, Uri.parse("demo://bank_contracts.docx"), "Bank_Contracts_Confidential.docx", 890_000, "application/msword", 0, null, category),
        DeviceMediaItem(403, Uri.parse("demo://crypto_passwords.txt"), "Crypto_Recovery_Keys.txt", 4_500, "text/plain", 0, null, category),
        DeviceMediaItem(404, Uri.parse("demo://tax_returns.pdf"), "Tax_Returns_Personal.pdf", 2_100_000, "application/pdf", 0, null, category)
      )
    }
  }
}
