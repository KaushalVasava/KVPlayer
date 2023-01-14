package com.lasuak.kvplayer.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.model.Video
import java.io.File
import java.io.IOException

object VideoUtil {
    // get videos by folderId
    fun getVideosByFolder(context: Context, folderId: Long): List<Video> {
        val videoList = mutableListOf<Video>()
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
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.VideoColumns.HEIGHT,
                MediaStore.Video.VideoColumns.WIDTH
            )
        }

        val cursor = context.contentResolver.query(
            uri,
            projection,
            MediaStore.Video.Media.BUCKET_ID + AppConstant.LIKE_TYPE, arrayOf(folderId.toString()),
            null
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentUris.withAppendedId(uri, cursor.getLong(3)).toString()
                } else {
                    cursor.getString(1)
                }
                videoList.add(
                    Video(
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
                )
            }
        }
        cursor?.close()
        return videoList
    }

    fun findVideoPosition(videoId: Long, videoList: List<Video>): Int {
        return videoList.indexOfFirst {
            it.id == videoId
        }
    }

    //share Video files
    fun shareVideo(context: Context, video: Video) {
        var path: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val file = File(AppUtil.getRealPath(Uri.parse(video.path), context)!!)
                path = FileProvider.getUriForFile(
                    context,
                    context.getString(R.string.file_provider), file
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                path = FileProvider.getUriForFile(
                    context,
                    context.getString(R.string.file_provider),
                    File(video.path)
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        context.startActivity(
            Intent.createChooser(
                Intent().setAction(Intent.ACTION_SEND)
                    .setType(AppConstant.VIDEO_MIME_TYPE)
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .putExtra(
                        Intent.EXTRA_STREAM,
                        Uri.parse(path!!.toString())
                    ),
                context.getString(R.string.sharing_file, video.name)
            )
        )
    }
}