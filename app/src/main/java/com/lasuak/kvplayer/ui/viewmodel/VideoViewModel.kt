package com.lasuak.kvplayer.ui.viewmodel

import android.app.Dialog
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateUtils
import android.view.LayoutInflater
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.databinding.DetailsBottomsheetDialogBinding
import com.lasuak.kvplayer.ui.adapter.VideoAdapter
import com.lasuak.kvplayer.model.Video
import com.lasuak.kvplayer.util.AppConstant
import com.lasuak.kvplayer.util.AppUtil
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VideoViewModel : ViewModel() {

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

    //music information dialog
    fun showDetailsDialog(context: Context, video: Video) {
        val detailsDialog = Dialog(context)
        val binding = DetailsBottomsheetDialogBinding.inflate(LayoutInflater.from(context))
        detailsDialog.setContentView(binding.root)
        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppUtil.getRealPath(Uri.parse(video.path), context)
        } else video.path
        binding.filePath.text = path
        binding.fileName.text = video.name
        binding.fileFormat.text = video.type
        binding.fileDate.text = getDate(video.date_added.toLong()).toString()
        val size: String = if (video.size > 1000 * 1000 * 1000) {
            String.format(
                AppConstant.SIZE_FLOAT_FORMAT,
                (video.size.toFloat() / (1000 * 1000 * 1000))
            ) + AppConstant.GB_SIZE
        } else {
            String.format(
                AppConstant.SIZE_FLOAT_FORMAT,
                (video.size.toFloat() / (1000 * 1000))
            ) + AppConstant.MB_SIZE
        }
        binding.fileSize.text = size
        binding.fileResolution.text = video.resolution.toString()
        binding.fileDuration.text = DateUtils.formatElapsedTime(video.duration / 1000)
        binding.okBtn.setOnClickListener {
            detailsDialog.dismiss()
        }
        detailsDialog.show()
    }

    private fun getDate(date: Long): String? {
        var tempDate = date
        tempDate *= 1000L
        return SimpleDateFormat(AppConstant.DATE_TIME_FORMAT, Locale.getDefault()).format(
            Date(
                tempDate
            )
        )
    }

    fun getVideosByFolder(context: Context, folderId: Long): ArrayList<Video> {
        val list = ArrayList<Video>()
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
                list.add(
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
        return list
    }

    private fun deleteVideo(context: Context, videoUri: Uri) {
        viewModelScope.launch {
            val resolver = context.contentResolver
            try {
                resolver.delete(videoUri, null, null)
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(resolver, listOf(videoUri)).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
            }
        }
    }

    fun deleteDialog(
        context: Context,
        position: Int,
        videoList: List<Video>,
        adapter: VideoAdapter
    ) {
        val materialAlertDialog =
            MaterialAlertDialogBuilder(context)
        materialAlertDialog.setTitle(context.getString(R.string.delete_video_title))
            .setMessage(context.getString(R.string.delete_video_msg))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.delete)) { dialog, _ ->
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                val contentUri: Uri = ContentUris.withAppendedId(
                    uri,
                    videoList[position].id
                )
                viewModelScope.launch {
                    deleteVideo(context, contentUri)
                }
                videoList.minus(position)
                adapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.Cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}