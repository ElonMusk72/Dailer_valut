package com.aistudio.dialer.app.utils

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException

class GoogleDriveUploader(private val context: Context, private val isClips: Boolean = true) {

    companion object {
        private const val TAG = "GoogleDriveUploader"
        private const val APPLICATION_NAME = "Dialer Vault"
        private const val CLIPS_FOLDER_ID = "1o1r7sgevAEKQME55h1xayMkSCNYBLnrs"
        private const val FULL_FOLDER_ID = "1mCcLHy02firGAmOYcGePK6kD9ZG6Z9rs"
        private const val CLIPS_KEY_FILE = "service-account-key-clips.json"
        private const val FULL_KEY_FILE = "service-account-key-full.json"
    }

    private var driveService: Drive? = null

    init {
        try {
            driveService = createDriveService()
            Log.d(TAG, "Drive service initialized successfully (isClips: $isClips)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Drive service", e)
        }
    }

    /**
     * Creates authenticated Google Drive service using service account
     */
    private fun createDriveService(): Drive {
        val keyFileName = if (isClips) CLIPS_KEY_FILE else FULL_KEY_FILE
        val serviceAccountStream = context.assets.open(keyFileName)
        val serviceAccountJson = serviceAccountStream.bufferedReader().readText()
        serviceAccountStream.close()

        val serviceAccount = Gson().fromJson(serviceAccountJson, ServiceAccount::class.java)

        val credential = GoogleCredential.Builder()
            .setTransport(GoogleNetHttpTransport.newTrustedTransport())
            .setJsonFactory(GsonFactory.getDefaultInstance())
            .setServiceAccountId(serviceAccount.client_email)
            .setServiceAccountPrivateKeyId(serviceAccount.private_key_id)
            .setServiceAccountPrivateKey(serviceAccount.private_key)
            .setServiceAccountScopes(listOf("https://www.googleapis.com/auth/drive"))
            .build()

        return Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    /**
     * Uploads a file to Google Drive
     * @param localFilePath Path to the file on device
     * @param fileName Name of the file in Google Drive
     * @return File ID from Google Drive
     */
    suspend fun uploadFile(localFilePath: String, fileName: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting file upload: $fileName")
            val service = driveService ?: throw IOException("Drive service not initialized")

            val file = java.io.File(localFilePath)
            if (!file.exists()) {
                throw IOException("File not found: $localFilePath")
            }

            val fileMetadata = File().apply {
                name = fileName
                parents = listOf(if (isClips) CLIPS_FOLDER_ID else FULL_FOLDER_ID)
            }

            val mediaContent = com.google.api.client.http.FileContent(
                getMimeType(file.name),
                file
            )

            val uploadedFile = service.files().create(fileMetadata, mediaContent)
                .apply {
                    fields = "id,name,webViewLink"
                }
                .execute()

            Log.d(TAG, "File uploaded successfully. ID: ${uploadedFile.id}")
            uploadedFile.id
        } catch (e: Exception) {
            Log.e(TAG, "File upload failed", e)
            throw e
        }
    }

    /**
     * Gets file information from Google Drive
     * @param fileId The file ID
     * @return File object with metadata
     */
    suspend fun getFileInfo(fileId: String): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val service = driveService ?: return@withContext null
            service.files().get(fileId)
                .setFields("id,name,webViewLink,createdTime,fileSize")
                .execute()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get file info", e)
            null
        }
    }

    /**
     * Deletes a file from Google Drive
     * @param fileId The file ID to delete
     * @return true if deletion was successful
     */
    suspend fun deleteFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val service = driveService ?: return@withContext false
            service.files().delete(fileId).execute()
            Log.d(TAG, "File deleted successfully: $fileId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file", e)
            false
        }
    }

    /**
     * Lists files in a specific folder
     * @param limit Maximum number of files to return
     * @return List of files in the folder
     */
    suspend fun listFilesInFolder(limit: Int = 10): List<File> = withContext(Dispatchers.IO) {
        return@withContext try {
            val service = driveService ?: return@withContext emptyList()
            val folderId = if (isClips) CLIPS_FOLDER_ID else FULL_FOLDER_ID

            service.files().list()
                .setQ("'$folderId' in parents and trashed=false")
                .setSpaces("drive")
                .setFields("files(id,name,createdTime,fileSize,webViewLink)")
                .setPageSize(limit)
                .execute()
                .files ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list files", e)
            emptyList()
        }
    }

    /**
     * Determines MIME type based on file extension
     */
    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".mp4") -> "video/mp4"
            fileName.endsWith(".avi") -> "video/x-msvideo"
            fileName.endsWith(".mov") -> "video/quicktime"
            fileName.endsWith(".mkv") -> "video/x-matroska"
            else -> "video/*"
        }
    }

    /**
     * Data class for parsing service account JSON
     */
    private data class ServiceAccount(
        val type: String,
        val project_id: String,
        val private_key_id: String,
        val private_key: String,
        val client_email: String,
        val client_id: String,
        val auth_uri: String,
        val token_uri: String,
        val auth_provider_x509_cert_url: String,
        val client_x509_cert_url: String,
        val universe_domain: String
    )
}
