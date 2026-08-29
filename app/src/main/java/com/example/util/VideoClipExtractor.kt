package com.example.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

/**
 * Utility to extract a specified time clip (e.g. first 5 seconds) from a video file.
 * Uses Android native MediaExtractor and MediaMuxer to perform stream-level trimming
 * without re-encoding, preserving 100% original video resolution and quality.
 */
object VideoClipExtractor {

  private const val TAG = "VideoClipExtractor"
  private const val DEFAULT_CLIP_DURATION_US = 5_000_000L // 5 seconds in microseconds
  private const val BUFFER_SIZE = 1024 * 1024 // 1 MB buffer

  /**
   * Extracts the first [durationUs] (default 5 seconds) of a video file into a target output file.
   *
   * @param context Android context
   * @param sourceFile The source video file on disk
   * @param outputFile The destination file where the trimmed video clip will be saved
   * @param durationUs Duration in microseconds (default: 5_000_000L = 5 seconds)
   * @return The output file if extraction succeeded, or null on failure
   */
  suspend fun extractClip(
    context: Context,
    sourceFile: File,
    outputFile: File,
    durationUs: Long = DEFAULT_CLIP_DURATION_US
  ): File? = withContext(Dispatchers.IO) {
    if (!sourceFile.exists() || sourceFile.length() == 0L) {
      Log.e(TAG, "Source file does not exist or is empty: ${sourceFile.absolutePath}")
      return@withContext null
    }

    val extractor = MediaExtractor()
    var muxer: MediaMuxer? = null

    try {
      extractor.setDataSource(sourceFile.absolutePath)
      val trackCount = extractor.trackCount

      if (trackCount == 0) {
        Log.e(TAG, "No media tracks found in source file")
        return@withContext null
      }

      // Ensure parent directory exists
      outputFile.parentFile?.mkdirs()
      if (outputFile.exists()) {
        outputFile.delete()
      }

      muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
      val indexMap = HashMap<Int, Int>()

      // Add supported audio/video tracks to muxer
      for (i in 0 until trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

        if (mime.startsWith("video/") || mime.startsWith("audio/")) {
          extractor.selectTrack(i)
          val muxerTrackIndex = muxer.addTrack(format)
          indexMap[i] = muxerTrackIndex
        }
      }

      if (indexMap.isEmpty()) {
        Log.e(TAG, "No compatible audio or video tracks could be mapped for muxing")
        return@withContext null
      }

      muxer.start()

      val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
      val bufferInfo = MediaCodec.BufferInfo()

      // Read samples from selected tracks and write up to durationUs
      while (true) {
        val trackIndex = extractor.sampleTrackIndex
        if (trackIndex < 0) {
          // End of stream
          break
        }

        val muxerTrackIndex = indexMap[trackIndex]
        if (muxerTrackIndex != null) {
          buffer.clear()
          val sampleSize = extractor.readSampleData(buffer, 0)
          if (sampleSize < 0) {
            break
          }

          val sampleTime = extractor.sampleTime
          // Stop if sample timestamp exceeds requested clip duration
          if (sampleTime > durationUs) {
            // Seek if there are other tracks or finish
            extractor.advance()
            continue
          }

          bufferInfo.offset = 0
          bufferInfo.size = sampleSize
          bufferInfo.presentationTimeUs = sampleTime
          bufferInfo.flags = extractor.sampleFlags

          muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
        }

        extractor.advance()
      }

      Log.i(TAG, "Successfully extracted ${durationUs / 1_000_000}s clip to ${outputFile.absolutePath}")
      outputFile
    } catch (e: Exception) {
      Log.e(TAG, "Error extracting video clip: ${e.message}", e)
      if (outputFile.exists()) {
        outputFile.delete()
      }
      null
    } finally {
      try {
        extractor.release()
      } catch (ignored: Exception) {}

      try {
        muxer?.stop()
        muxer?.release()
      } catch (ignored: Exception) {}
    }
  }

  /**
   * Extracts a 5-second clip from a content URI into a designated local output file.
   */
  suspend fun extractClipFromUri(
    context: Context,
    sourceUri: Uri,
    outputFile: File,
    durationUs: Long = DEFAULT_CLIP_DURATION_US
  ): File? = withContext(Dispatchers.IO) {
    val extractor = MediaExtractor()
    var muxer: MediaMuxer? = null

    try {
      extractor.setDataSource(context, sourceUri, null)
      val trackCount = extractor.trackCount

      if (trackCount == 0) return@withContext null

      outputFile.parentFile?.mkdirs()
      if (outputFile.exists()) outputFile.delete()

      muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
      val indexMap = HashMap<Int, Int>()

      for (i in 0 until trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
        if (mime.startsWith("video/") || mime.startsWith("audio/")) {
          extractor.selectTrack(i)
          val muxerTrackIndex = muxer.addTrack(format)
          indexMap[i] = muxerTrackIndex
        }
      }

      if (indexMap.isEmpty()) return@withContext null

      muxer.start()
      val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
      val bufferInfo = MediaCodec.BufferInfo()

      while (true) {
        val trackIndex = extractor.sampleTrackIndex
        if (trackIndex < 0) break

        val muxerTrackIndex = indexMap[trackIndex]
        if (muxerTrackIndex != null) {
          buffer.clear()
          val sampleSize = extractor.readSampleData(buffer, 0)
          if (sampleSize < 0) break

          val sampleTime = extractor.sampleTime
          if (sampleTime > durationUs) {
            extractor.advance()
            continue
          }

          bufferInfo.offset = 0
          bufferInfo.size = sampleSize
          bufferInfo.presentationTimeUs = sampleTime
          bufferInfo.flags = extractor.sampleFlags

          muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
        }
        extractor.advance()
      }

      outputFile
    } catch (e: Exception) {
      Log.e(TAG, "Error extracting from URI: ${e.message}", e)
      if (outputFile.exists()) outputFile.delete()
      null
    } finally {
      try { extractor.release() } catch (ignored: Exception) {}
      try { muxer?.stop(); muxer?.release() } catch (ignored: Exception) {}
    }
  }
}
