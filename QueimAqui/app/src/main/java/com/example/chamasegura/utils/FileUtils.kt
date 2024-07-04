package com.example.chamasegura.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

object FileUtils {
    fun getPath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.moveToFirst()
            val columnIndex = cursor?.getColumnIndexOrThrow(projection[0])
            return cursor?.getString(columnIndex ?: 0)
        } finally {
            cursor?.close()
        }
    }
}