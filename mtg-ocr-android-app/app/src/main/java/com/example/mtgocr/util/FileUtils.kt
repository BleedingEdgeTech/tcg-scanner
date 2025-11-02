package com.example.mtgocr.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {

    fun saveImageToExternalStorage(context: Context, imageUri: Uri, fileName: String): Boolean {
        return try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input?.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun getSavedImages(context: Context): List<File> {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return directory?.listFiles()?.toList() ?: emptyList()
    }

    fun deleteImage(file: File): Boolean {
        return file.delete()
    }
}