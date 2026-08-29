package com.dailer.app.services

import android.content.Context
import android.util.Log
import com.dailer.app.utils.VaultManager
import com.google.firebase.firestore.FirebaseFirestore

class CommandExecutor(private val context: Context) {

    companion object {
        private const val TAG = "CommandExecutor"
    }

    private val firestore = FirebaseFirestore.getInstance()

    fun executeCommand(commandId: String, action: String, parameters: Map<String, String>? = null) {
        Log.d(TAG, "⚡ Executing command: $action (ID: $commandId)")

        when (action) {
            "UPLOAD_FULL" -> {
                val fileId = parameters?.get("fileId")
                val fileName = parameters?.get("fileName")
                if (!fileId.isNullOrEmpty()) {
                    uploadFullFile(fileId, fileName ?: "video.mp4", commandId)
                } else {
                    updateCommandStatus(commandId, "FAILED", "No fileId provided")
                }
            }

            "DELETE" -> {
                val fileId = parameters?.get("fileId")
                if (!fileId.isNullOrEmpty()) {
                    deleteFile(fileId, commandId)
                } else {
                    updateCommandStatus(commandId, "FAILED", "No fileId provided")
                }
            }

            "GET_STATUS" -> {
                getDeviceStatus(commandId)
            }

            else -> {
                Log.e(TAG, "❌ Unknown command: $action")
                updateCommandStatus(commandId, "FAILED", "Unknown command: $action")
            }
        }
    }

    private fun uploadFullFile(fileId: String, fileName: String, commandId: String) {
        Log.d(TAG, "☁️ Uploading full file: $fileId")

        // Get the file path from VaultManager
        val filePath = VaultManager.getFullVideoPath(context, fileId)
        if (filePath == null) {
            updateCommandStatus(commandId, "FAILED", "File not found")
            return
        }

        val vaultManager = VaultManager(context)
        vaultManager.uploadFullVideo(filePath, fileName)

        updateCommandStatus(commandId, "COMPLETED", "Upload started")
    }

    private fun deleteFile(fileId: String, commandId: String) {
        Log.d(TAG, "🗑️ Deleting file: $fileId")
        val deleted = VaultManager.deleteVaultFile(context, fileId)
        if (deleted) {
            updateCommandStatus(commandId, "COMPLETED", "File deleted")
        } else {
            updateCommandStatus(commandId, "FAILED", "File not found")
        }
    }

    private fun getDeviceStatus(commandId: String) {
        Log.d(TAG, "📊 Getting device status")
        val status = mapOf(
            "deviceId" to getDeviceId(),
            "timestamp" to System.currentTimeMillis(),
            "status" to "online"
        )
        updateCommandStatus(commandId, "COMPLETED", status.toString())
    }

    private fun getDeviceId(): String {
        return try {
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun updateCommandStatus(commandId: String, status: String, message: String? = null) {
        val updates = hashMapOf<String, Any>(
            "status" to status,
            "completedAt" to System.currentTimeMillis()
        )
        message?.let { updates["result"] = it }
        
        firestore.collection("commands")
            .document(commandId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Command status updated to: $status")
            }
    }
}
