package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    fun getLocalFilePathFromUri(context: Context, uri: Uri): String {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val mimeType = contentResolver.getType(uri) ?: ""
            val extension = if (mimeType.contains("video")) "mp4" else "jpg"
            val tempFile = File(context.cacheDir, "media_${System.currentTimeMillis()}_${(1000..9999).random()}.$extension")
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(tempFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            uri.toString()
        }
    }
}
