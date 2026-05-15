package com.datrooo.notes.data.local

import android.content.Context
import androidx.core.net.toUri
import java.io.File

class ImageStorage(private val context: Context) {
    
    /**
     * Copies the image from the given URI to the app's internal storage.
     * Returns the URI string of the saved image file.
     */
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
}
