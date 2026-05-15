package com.datrooo.notes.data.local

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import java.io.File
import java.io.FileInputStream

class ImageStorage(private val context: Context) {
    private val prefs = context.getSharedPreferences("image_prefs", Context.MODE_PRIVATE)

    fun getPreferredPackage(): String? {
        val pkg = prefs.getString("preferred_viewer_package", null)
        if (pkg != null) {
            try {
                context.packageManager.getPackageInfo(pkg, 0)
                return pkg
            } catch (_: PackageManager.NameNotFoundException) {
                prefs.edit { remove("preferred_viewer_package") }
            }
        }
        return null
    }

    fun setPreferredPackage(packageName: String?) {
        if (packageName == null) {
            prefs.edit { remove("preferred_viewer_package") }
        } else {
            prefs.edit { putString("preferred_viewer_package", packageName) }
        }
    }

    fun getAvailableViewers(): List<Pair<String, String>> {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType("content://temp".toUri(), "image/*")
        }
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        
        return resolveInfos.map { 
            it.activityInfo.packageName to it.loadLabel(context.packageManager).toString() 
        }
    }

    fun saveImageToInternalStorage(uriString: String): String? {
        return try {
            val uri = uriString.toUri()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
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
            file,
        )
    }

    fun getShareableUri(uriString: String): Uri {
        val uri = uriString.toUri()
        val path = uri.path ?: throw IllegalArgumentException("Invalid URI")
        val file = File(path)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    fun saveImageToGallery(uriString: String): Boolean {
        return try {
            val sourceUri = uriString.toUri()
            val sourcePath = sourceUri.path ?: return false
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return false

            val fileName = "NoteImage_${System.currentTimeMillis()}.jpg"
            val resolver = context.contentResolver

            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val imageUri = resolver.insert(imageCollection, contentValues) ?: return false

            resolver.openOutputStream(imageUri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
