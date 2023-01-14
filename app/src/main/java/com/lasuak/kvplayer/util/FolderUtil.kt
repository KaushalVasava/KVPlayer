package com.lasuak.kvplayer.util

import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.lasuak.kvplayer.model.Folder
import com.lasuak.kvplayer.model.Video
import java.io.File

object FolderUtil {
    // get all folders
    fun getAllFolder(context: Context): List<Folder> {
        val folderList = mutableListOf<Folder>()
        val tempFolderList = mutableListOf<Long>()
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
                if (!tempFolderList.contains(folderID)) {
                    tempFolderList.add(folderID)
                    val count = getTotalCount(context, folderID)
                    if (count != 0) {
                        folderList.add(
                            Folder(cursor.getLong(0), cursor.getString(1), count)
                        )
                    }
                }
            }
            cursor.close()
        }
        return folderList
    }

    private fun getTotalCount(context: Context, folderId: Long): Int {
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
        var videoCount = 0
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
                scannedFile(context, path)
                if (file.exists()) {
                    videoCount++
                }
            }
        }
        cursor?.close()
        return videoCount
    }

    fun findVideo(context: Context, folderId: Long, videoId: Long): Video {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.VideoColumns.MIME_TYPE,
                MediaStore.Video.VideoColumns.DATE_ADDED,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.VideoColumns.HEIGHT,
                MediaStore.Video.VideoColumns.WIDTH,
                MediaStore.Video.Media.DISPLAY_NAME
            )
        } else {
            @Suppress("deprecation")
            arrayOf(
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.VideoColumns.MIME_TYPE,
                MediaStore.Video.VideoColumns.DATE_ADDED,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.VideoColumns.HEIGHT,
                MediaStore.Video.VideoColumns.WIDTH,
                MediaStore.Video.Media.DISPLAY_NAME
                )
        }
        val cursor = context.contentResolver.query(
            uri,
            projection,
            MediaStore.Video.Media.BUCKET_ID + AppConstant.LIKE_TYPE,
            arrayOf(folderId.toString()),
            null
        )
        var video: Video? = null
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(3) == videoId) {
                    val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentUris.withAppendedId(uri, cursor.getLong(3)).toString()
                    } else {
                        cursor.getString(1)
                    }
                    video = Video(
                        cursor.getString(0),
                        path,
                        cursor.getLong(2),
                        cursor.getLong(3),
                        cursor.getDouble(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getInt(8),
                        cursor.getInt(9)
                    )
                    break
                }
            }
        }
        cursor?.close()
        return video!!
    }

    private fun scannedFile(context: Context, path: String) {
        try {
            MediaScannerConnection.scanFile(
                context, arrayOf(path),
                null
            ) { p, uri ->
                if (uri != null) {
                    Log.d("TAG", "scannedFile: $uri and $p")
                }
            }
        } catch (e: Exception) {
            Log.i("TAG", "error uri= $path")
            e.printStackTrace()
        }
    }
}