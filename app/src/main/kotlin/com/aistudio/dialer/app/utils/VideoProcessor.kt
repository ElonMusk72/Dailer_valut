package com.aistudio.dialer.app.utils

import android.content.Context
import android.util.Log
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File

class VideoProcessor(private val context: Context) {

    companion object {
        private const val TAG = "VideoProcessor"
        private const val CLIP_DURATION = 5 // seconds
    }

    /**
     * Extracts first 5 seconds of video without changing resolution or compressing
     * @param inputVideoPath Path to the original video file
     * @param outputClipPath Path where the clip will be saved
     * @return true if extraction was successful
     */
    fun extractClip(inputVideoPath: String, outputClipPath: String): Boolean {
        return try {
            Log.d(TAG, "Starting clip extraction from: $inputVideoPath")

            val command = arrayOf(
                "-i", inputVideoPath,
                "-t", CLIP_DURATION.toString(),
                "-c", "copy",  // Copy codec without re-encoding
                "-y",  // Overwrite output file
                outputClipPath
            )

            val rc = FFmpeg.execute(command)
            if (rc == 0) {
                Log.d(TAG, "Clip extraction successful: $outputClipPath")
                true
            } else {
                Log.e(TAG, "FFmpeg execution failed with return code: $rc")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting clip", e)
            false
        }
    }

    /**
     * Generates a unique filename for the clip
     * @param originalFileName Original video file name
     * @return Clip filename with _clip suffix
     */
    fun generateClipFileName(originalFileName: String): String {
        val nameWithoutExt = originalFileName.substringBeforeLast(".")
        val extension = originalFileName.substringAfterLast(".")
        return "${nameWithoutExt}_clip.${extension}"
    }

    /**
     * Gets the app's cache directory for storing clips temporarily
     * @return Clip storage directory
     */
    fun getClipStorageDir(): File {
        val clipsDir = File(context.cacheDir, "video_clips")
        if (!clipsDir.exists()) {
            clipsDir.mkdirs()
        }
        return clipsDir
    }
}
