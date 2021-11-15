package com.lasuak.kvplayer.viewmodel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.google.android.material.button.MaterialButton
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.model.Video
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException

class VideoViewModel : ViewModel() {

    //get storage path of media file for android Q and above
    fun getRealPath(uri: Uri, context: Context): String? {
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
                    File(lists[position].path!!)
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val sendIntent = Intent()
        // Put the Uri and MIME type in the result Intent
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "video/*"
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        sendIntent.putExtra(Intent.EXTRA_STREAM, path!!)//Uri.parse(list[position].path))
        context.startActivity(
            Intent.createChooser(
                sendIntent,
                "Sharing ${lists[position].name}"
            )
        )
    }

    //music information dialog
    @SuppressLint("SetTextI18n")
    fun showDetailsDialog(context: Context, lists: ArrayList<Video>, position: Int) {
        val detailsDialog = Dialog(context)
        detailsDialog.setContentView(R.layout.details_bottomsheet_dialog)
        val textPath = detailsDialog.findViewById<TextView>(R.id.filePath)
        val textName = detailsDialog.findViewById<TextView>(R.id.fileName)
        val textFormat = detailsDialog.findViewById<TextView>(R.id.fileFormat)
        val textLength = detailsDialog.findViewById<TextView>(R.id.fileLength)
        val btnOk = detailsDialog.findViewById<MaterialButton>(R.id.okBtn)

        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getRealPath(Uri.parse(lists[position].path), context)
        } else lists[position].path
        textPath!!.text = "File Path: $path"
        textName!!.text = "Name: " + lists[position].name
        val fileFormat = "File Format: " + lists[position].type
        textFormat!!.text = fileFormat
        val length  = (lists[position].size/(1024*1024)).toString() + " mb"
//        val length = "File Length : " + "%.2f"(lists[position].length!!.toFloat() / (1024 * 1024)) + " mb"
        textLength!!.text = length
        //builder.show()
        btnOk!!.setOnClickListener {
            detailsDialog.dismiss()
        }
        detailsDialog.show()
    }

}