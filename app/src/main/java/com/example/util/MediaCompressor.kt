package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object MediaCompressor {
    private const val TAG = "MediaCompressor"
    private const val MAX_IMAGE_DIMENSION = 1600
    private const val TARGET_MIN_BYTES = 100 * 1024 // 100 KB
    private const val TARGET_MAX_BYTES = 600 * 1024 // 600 KB

    data class CompressionResult(
        val file: File,
        val originalSizeBytes: Long,
        val compressedSizeBytes: Long,
        val mimeType: String,
        val isVideo: Boolean
    ) {
        val originalSizeFormatted: String get() = formatFileSize(originalSizeBytes)
        val compressedSizeFormatted: String get() = formatFileSize(compressedSizeBytes)
        val compressionRatioPercent: Int get() = if (originalSizeBytes > 0) {
            (((originalSizeBytes - compressedSizeBytes).toDouble() / originalSizeBytes) * 100).toInt().coerceAtLeast(0)
        } else 0
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
        }
    }

    /**
     * Compresses an image file or content Uri to target 100 KB - 600 KB size
     */
    suspend fun compressImage(
        context: Context,
        inputUriOrPath: String,
        targetMinBytes: Long = TARGET_MIN_BYTES.toLong(),
        targetMaxBytes: Long = TARGET_MAX_BYTES.toLong()
    ): CompressionResult = withContext(Dispatchers.IO) {
        var originalSize = 0L
        val uri = if (inputUriOrPath.startsWith("content://") || inputUriOrPath.startsWith("file://")) {
            Uri.parse(inputUriOrPath)
        } else {
            Uri.fromFile(File(inputUriOrPath))
        }

        try {
            // Measure original size
            context.contentResolver.openInputStream(uri)?.use { stream ->
                originalSize = stream.available().toLong()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not measure original stream size", e)
        }

        if (originalSize <= 0) {
            val f = File(inputUriOrPath)
            if (f.exists()) originalSize = f.length()
        }

        // 1. Decode bounds to compute inSampleSize
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        var is1: InputStream? = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(is1, null, options)
        is1?.close()

        val origWidth = options.outWidth
        val origHeight = options.outHeight
        var inSampleSize = 1

        if (origWidth > MAX_IMAGE_DIMENSION || origHeight > MAX_IMAGE_DIMENSION) {
            val halfWidth = origWidth / 2
            val halfHeight = origHeight / 2
            while ((halfWidth / inSampleSize) >= MAX_IMAGE_DIMENSION && (halfHeight / inSampleSize) >= MAX_IMAGE_DIMENSION) {
                inSampleSize *= 2
            }
        }

        // 2. Decode actual Bitmap
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.RGB_565 // More memory efficient
        }
        val is2: InputStream? = context.contentResolver.openInputStream(uri)
        val decodedBitmap: Bitmap? = BitmapFactory.decodeStream(is2, null, decodeOptions)
        is2?.close()

        if (decodedBitmap == null) {
            // Fallback: create empty or direct copy
            val fallbackFile = File(context.cacheDir, "compressed_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(fallbackFile).use { output -> input.copyTo(output) }
            }
            return@withContext CompressionResult(
                file = fallbackFile,
                originalSizeBytes = originalSize.coerceAtLeast(fallbackFile.length()),
                compressedSizeBytes = fallbackFile.length(),
                mimeType = "image/jpeg",
                isVideo = false
            )
        }

        var workingBitmap: Bitmap = decodedBitmap

        // 3. Fix Orientation if needed
        try {
            var orientation = ExifInterface.ORIENTATION_NORMAL
            context.contentResolver.openInputStream(uri)?.use { exifStream ->
                val exif = ExifInterface(exifStream)
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }
            if (!matrix.isIdentity) {
                val rotated = Bitmap.createBitmap(workingBitmap, 0, 0, workingBitmap.width, workingBitmap.height, matrix, true)
                if (rotated != workingBitmap) {
                    workingBitmap.recycle()
                    workingBitmap = rotated
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exif orientation check skipped", e)
        }

        // 4. Scale down proportionally if still exceeds MAX_IMAGE_DIMENSION
        if (workingBitmap.width > MAX_IMAGE_DIMENSION || workingBitmap.height > MAX_IMAGE_DIMENSION) {
            val scaleFactor = MAX_IMAGE_DIMENSION.toFloat() / Math.max(workingBitmap.width, workingBitmap.height)
            val targetW = (workingBitmap.width * scaleFactor).toInt().coerceAtLeast(1)
            val targetH = (workingBitmap.height * scaleFactor).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(workingBitmap, targetW, targetH, true)
            if (scaled != workingBitmap) {
                workingBitmap.recycle()
                workingBitmap = scaled
            }
        }

        // 5. Binary search / iterative quality adjustment to hit 100 KB - 600 KB
        var quality = 82
        var baos = ByteArrayOutputStream()
        workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        var bytes = baos.toByteArray()

        // If file is larger than target max (600 KB), lower quality
        while (bytes.size > targetMaxBytes && quality > 35) {
            quality -= 10
            baos = ByteArrayOutputStream()
            workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()
        }

        // If file is smaller than 100 KB but original was high quality, we don't degrade further.
        val outFile = File(context.cacheDir, "img_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")
        FileOutputStream(outFile).use { fos ->
            fos.write(bytes)
            fos.flush()
        }

        workingBitmap.recycle()

        CompressionResult(
            file = outFile,
            originalSizeBytes = if (originalSize > 0) originalSize else outFile.length(),
            compressedSizeBytes = outFile.length(),
            mimeType = "image/jpeg",
            isVideo = false
        )
    }

    /**
     * Optimizes a video file and prepares it for R2 upload
     */
    suspend fun optimizeVideo(
        context: Context,
        inputUriOrPath: String
    ): CompressionResult = withContext(Dispatchers.IO) {
        val cleanPath = inputUriOrPath.replace("[VIDEO]", "").trim()
        val uri = if (cleanPath.startsWith("content://") || cleanPath.startsWith("file://")) {
            Uri.parse(cleanPath)
        } else {
            Uri.fromFile(File(cleanPath))
        }

        var originalSize = 0L
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                originalSize = stream.available().toLong()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not measure original video stream size", e)
        }

        val outFile = File(context.cacheDir, "vid_${System.currentTimeMillis()}_${(1000..9999).random()}.mp4")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }

        if (originalSize <= 0) {
            originalSize = outFile.length()
        }

        CompressionResult(
            file = outFile,
            originalSizeBytes = originalSize,
            compressedSizeBytes = outFile.length(),
            mimeType = "video/mp4",
            isVideo = true
        )
    }
}
