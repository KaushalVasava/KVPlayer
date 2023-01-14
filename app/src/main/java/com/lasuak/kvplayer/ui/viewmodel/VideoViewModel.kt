package com.lasuak.kvplayer.ui.viewmodel

import android.app.Dialog
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateUtils
import android.view.LayoutInflater
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

class VideoViewModel : ViewModel() {

    //video information dialog
    fun showDetailsDialog(context: Context, video: Video) {
        val detailsDialog = Dialog(context)
        val binding = DetailsBottomsheetDialogBinding.inflate(LayoutInflater.from(context))
        detailsDialog.setContentView(binding.root)
        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppUtil.getRealPath(Uri.parse(video.path), context)
        } else video.path
        binding.txtPath.text = path
        binding.txtName.text = video.name
        binding.txtFormat.text = video.type
        binding.txtDate.text = AppUtil.getDate(video.date_added.toLong()).toString()
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
        binding.txtSize.text = size
        binding.txtResolution.text = video.resolution.toString()
        binding.txtDuration.text = DateUtils.formatElapsedTime(video.duration / 1000)
        binding.btnOk.setOnClickListener {
            detailsDialog.dismiss()
        }
        detailsDialog.show()
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
}