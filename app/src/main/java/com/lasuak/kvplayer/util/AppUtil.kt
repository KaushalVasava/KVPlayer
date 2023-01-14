package com.lasuak.kvplayer.util

import android.content.Context
import android.net.Uri
import java.lang.IllegalArgumentException

object AppUtil {
    //get storage path of media file for android Q and above
    fun getRealPath(uri: Uri, context: Context): String? {
        var realPath: String? = null
        try {
            if (uri.scheme == AppConstant.CONTENT) {
                val projection = arrayOf(AppConstant.DATA)
                val cursor = context.contentResolver.query(
                    uri,
                    projection, null, null, null
                )
                if (cursor != null) {
                    val id = cursor.getColumnIndexOrThrow(AppConstant.DATA)
                    cursor.moveToNext()
                    try {
                        realPath = cursor.getString(id)
                    } catch (e: Exception) {
                        realPath = null
                    } finally {
                        cursor.close()
                    }
                } else if (uri.scheme == AppConstant.FILE) {
                    realPath = uri.path
                }
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            realPath = null
        }
        return realPath
    }
}