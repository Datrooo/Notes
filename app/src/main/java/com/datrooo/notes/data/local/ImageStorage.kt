package com.datrooo.notes.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

class ImageStorage(private val context: Context) {
    fun saveImageToInternalStorage(uriString: String): String? {
        return try {
            val uri = uriString.toUri()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            // Create a unique filename
            val fileName = "note_image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            file.toUri().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun getTempCameraUri(): Uri {
        val fileName = "temp_camera_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
