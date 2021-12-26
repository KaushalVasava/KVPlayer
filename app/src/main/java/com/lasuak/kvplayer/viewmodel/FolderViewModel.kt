package com.lasuak.kvplayer.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lasuak.kvplayer.model.Folder
import com.lasuak.kvplayer.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException

private const val TAG = "FolderViewModel"

class FolderViewModel(application: Application) : AndroidViewModel(application) {

    private val foldersLiveData: MutableLiveData<MutableList<Folder>>

    init {
        Log.i(TAG, "init")

        foldersLiveData = MutableLiveData()
        foldersLiveData.value = setFolders(application.applicationContext)
    }

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

    fun getFolders(): LiveData<MutableList<Folder>> {
        return foldersLiveData
    }

    private fun setFolders(context: Context): MutableList<Folder> {
        Log.i(TAG, "createContacts")
        val list = mutableListOf<Folder>()
        val tempList = ArrayList<Long>()

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val sortOrder = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC"
        val cursor = context.contentResolver.query(
            uri, projection, null, null,
            sortOrder
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val folderID = cursor.getLong(0)
                if (!tempList.contains(folderID)) {
                    tempList.add(folderID)
                    Log.d(
                        "FOLDER", "all :${cursor.getString(2)} and ${cursor.getString(3)}"
                    )
                    val count = getTotalCount(context, folderID)
                    list.add(
                        Folder(cursor.getLong(0), cursor.getString(1), count)
                    )
                }
            }
            cursor.close()
        }
        return list
    }

    @SuppressLint("Recycle")
    fun getAllVideo(context: Context): ArrayList<Video> {
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
                MediaStore.Video.VideoColumns.WIDTH
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

        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {

                val path: String
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    path = ContentUris.withAppendedId(uri, cursor.getLong(3)).toString()
                    Log.d("PATH", cursor.getString(0))
                } else {
                    path = cursor.getString(2)
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

    @SuppressLint("Recycle")
    fun getAllFolder(context: Context): ArrayList<Folder> {
        val list = ArrayList<Folder>()
        val tempList = ArrayList<Long>()

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val sortOrder = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC"
        val cursor = context.contentResolver.query(
            uri, projection, null, null,
            sortOrder
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val folderID = cursor.getLong(0)
                if (!tempList.contains(folderID)) {
                    tempList.add(folderID)
                    Log.d(
                        "FOLDER", "all :${cursor.getString(2)} and ${cursor.getString(3)}"
                    )
                    val count = getTotalCount(context, folderID)
                    list.add(
                        Folder(cursor.getLong(0), cursor.getString(1), count)
                    )
                }
            }
            cursor.close()
        }
        return list
    }

    @SuppressLint("Recycle")
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
                MediaStore.Video.Media.RELATIVE_PATH
            )
        } else {
            @Suppress("deprecation")
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA
            )
        }

        val cursor = context.contentResolver.query(
            uri, projection,
            MediaStore.Video.Media.BUCKET_ID + " like? ", arrayOf(folderId.toString()), null
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                    count++
            }
        }
        cursor!!.close()
        return count
    }
}