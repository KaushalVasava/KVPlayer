package com.lasuak.kvplayer.viewmodel

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateUtils
import android.util.Log
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.adapter.VideoAdapter
import com.lasuak.kvplayer.databinding.DetailsBottomsheetDialogBinding
import com.lasuak.kvplayer.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.Math.abs
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VideoViewModel : ViewModel() {

    lateinit var adapter: VideoAdapter

    companion object {
        //        var videoList = MutableLiveData<ArrayList<Video>>()
        var videoList = ArrayList<Video>()
    }

    //get storage path of media file for android Q and above
    private fun getRealPath(uri: Uri, context: Context): String? {
        var realPath: String? = null
        try {
            if (uri.scheme!! == "content") {
                val projection = arrayOf("_data")
                val cursor = context.contentResolver.query(
                    uri,
                    projection, null, null, null
                )
                if (cursor != null) {
                    val id = cursor.getColumnIndexOrThrow("_data")
                    cursor.moveToNext()
                    try {
                        realPath = cursor.getString(id)
                    } catch (e: Exception) {
                        realPath = null
                    } finally {
                        cursor.close()
                    }
                } else if (uri.scheme!!.equals("file")) {
                    realPath = uri.path!!
                }
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            realPath = null
        }
        return realPath
    }

    //share Video files
    fun shareVideo(context: Context, lists: ArrayList<Video>, position: Int) {
        var path: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //val path1  = EXTERNAL_CONTENT_URI.toString() +"/"+lists[position].displayName
            try {
                val file = File(getRealPath(Uri.parse(lists[position].path), context)!!)
                //lists[position].path!!)
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
                    File(lists[position].path)
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        context.startActivity(
            Intent.createChooser(
                Intent().setAction(Intent.ACTION_SEND)
                    .setType("video/*")
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .putExtra(
                        Intent.EXTRA_STREAM,
                        path!!
                    ), "Sharing ${lists[position].name}"
            )
        )
    }

    //music information dialog
    fun showDetailsDialog(context: Context, lists: ArrayList<Video>, position: Int) {
        val detailsDialog = Dialog(context)
        detailsDialog.setContentView(R.layout.details_bottomsheet_dialog)

        val textPath = detailsDialog.findViewById<TextView>(R.id.filePath)
        val textName = detailsDialog.findViewById<TextView>(R.id.fileName)
        val textFormat = detailsDialog.findViewById<TextView>(R.id.fileFormat)
        val txtSize = detailsDialog.findViewById<TextView>(R.id.fileSize)
        val btnOk = detailsDialog.findViewById<MaterialButton>(R.id.okBtn)
        val resolution = detailsDialog.findViewById<TextView>(R.id.fileResolution)
        val duration = detailsDialog.findViewById<TextView>(R.id.fileDuration)
        val date_added = detailsDialog.findViewById<TextView>(R.id.fileDate)

        val path ="File Path : "+ if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getRealPath(Uri.parse(lists[position].path), context)
        } else lists[position].path
        textPath!!.text = path
        textName!!.text = lists[position].name
        val fileFormat = lists[position].type
        textFormat!!.text = fileFormat
        date_added.text = getDate(lists[position].date_added.toLong()).toString()
        val size:String
       // val length = "File Length : " + "%.2f"(lists[position].length!!.toFloat() / (1024 * 1024)) + " mb"
        if(lists[position].size>1000*1000*1000){
            Log.d("TAG", "showDetailsDialog: ")
            size =String.format("%.2f",(lists[position].size.toFloat() /(1000*1000*1000))) + " GB"
        }else{
            size =String.format("%.2f",(lists[position].size.toFloat() /(1000*1000))) + "MB"
        }
        txtSize!!.text = size

        resolution.text=lists[position].resolution.toString()
        duration.text =DateUtils.formatElapsedTime(lists[position].duration / 1000)
        //builder.show()
        btnOk!!.setOnClickListener {
            detailsDialog.dismiss()
        }
        detailsDialog.show()
    }
    private fun getDate(date : Long): String? {
        var tempDate = date
        tempDate*= 1000L
        return SimpleDateFormat("d MMM yyyy, hh:mm aa", Locale.getDefault()).format(Date(tempDate))
    }
    @SuppressLint("Recycle")
    fun getVideo(context: Context, folderId: Long): ArrayList<Video> {
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
            uri, projection,
            MediaStore.Video.Media.BUCKET_ID + " like? ", arrayOf(folderId.toString()), null
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {

                val path: String
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    path = ContentUris.withAppendedId(uri, cursor.getLong(3)).toString()
                } else {
                    path = cursor.getString(1)
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
        cursor!!.close()
        return list
    }

    private suspend fun deleteVideo(context: Context, videoUri: Uri) {
        withContext(Dispatchers.IO) {
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

    fun deleteDialog(context: Context, position: Int, lifecycleOwner: LifecycleOwner) {
        val materialAlertDialog =
            MaterialAlertDialogBuilder(context)
        materialAlertDialog.setTitle("Delete Video")
            .setMessage("Delete this video? ")
            .setCancelable(false)
            .setPositiveButton("Delete") { dialog, id ->
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
                lifecycleOwner.lifecycleScope.launch {
                    deleteVideo(context, contentUri)
                }
                videoList.removeAt(position)
                adapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }
            .show()
    }
}