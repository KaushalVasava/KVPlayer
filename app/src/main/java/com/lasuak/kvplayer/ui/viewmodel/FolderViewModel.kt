package com.lasuak.kvplayer.ui.viewmodel

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.lasuak.kvplayer.model.Folder
import com.lasuak.kvplayer.util.AppConstant
import com.lasuak.kvplayer.util.AppUtil
import java.io.File

class FolderViewModel : ViewModel() {
    fun getAllFolder(context: Context): List<Folder> {
        val list = ArrayList<Folder>()
        val tempList = ArrayList<Long>()
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_MODIFIED
            )
        } else {
            @Suppress("deprecation")
            arrayOf(
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_MODIFIED
            )
        }
        val sortOrder = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + AppConstant.ORDER_ASC
        val cursor = context.contentResolver.query(
            uri, projection, null, null,
            sortOrder
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val folderID = cursor.getLong(0)
                if (!tempList.contains(folderID)) {
                    tempList.add(folderID)
                    val count = getTotalCount(context, folderID)
                    if (count != 0) {
                        list.add(
                            Folder(cursor.getLong(0), cursor.getString(1), count)
                        )
                    }
                }
            }
            cursor.close()
        }
        return list
    }

    private fun getTotalCount(context: Context, folderId: Long): Int {
        var count = 0
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
            )
        } else {
            @Suppress("deprecation")
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
            )
        }
        val cursor = context.contentResolver.query(
            uri,
            projection,
            MediaStore.Video.Media.BUCKET_ID + AppConstant.LIKE_TYPE,
            arrayOf(folderId.toString()),
            null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val tempPath = ContentUris.withAppendedId(uri, cursor.getLong(0)).toString()
                    val realPath =
                        AppUtil.getRealPath(Uri.parse(tempPath), context)
                    realPath ?: tempPath
                } else {
                    cursor.getString(1)
                }
                val file = File(path)
                if (file.exists()) {
                    count++
                }
            }
        }
        cursor?.close()
        return count
    }
}