package com.aistudio.dialer.app.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.aistudio.dialer.app.utils.GoogleDriveUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val COMMAND_UPLOAD_FULL = "UPLOAD_FULL"
        private const val COMMAND_DELETE = "DELETE"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        remoteMessage.data.let {
            val command = it["command"] ?: return
            val videoPath = it["video_path"] ?: ""
            val videoName = it["video_name"] ?: ""

            Log.d(TAG, "Command: $command, Path: $videoPath, Name: $videoName")

            when (command) {
                COMMAND_UPLOAD_FULL -> handleUploadFull(videoPath, videoName)
                COMMAND_DELETE -> handleDelete(videoPath)
                else -> Log.w(TAG, "Unknown command: $command")
            }
        }
    }

    private fun handleUploadFull(videoPath: String, videoName: String) {
        Log.d(TAG, "Handling UPLOAD_FULL command")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uploader = GoogleDriveUploader(applicationContext, isClips = false)
                val fileId = uploader.uploadFile(videoPath, videoName)
                Log.d(TAG, "Upload successful. File ID: $fileId")
            } catch (e: Exception) {
                Log.e(TAG, "Upload failed", e)
            }
        }
    }

    private fun handleDelete(videoPath: String) {
        Log.d(TAG, "Handling DELETE command for: $videoPath")
        // Implement file deletion if needed
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM Token: $token")
        // Save token to your backend for sending commands later
    }
}
