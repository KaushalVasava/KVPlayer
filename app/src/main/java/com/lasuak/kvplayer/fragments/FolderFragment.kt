package com.lasuak.kvplayer.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.adapter.FolderAdapter
import com.lasuak.kvplayer.adapter.FolderListener
import com.lasuak.kvplayer.databinding.FragmentFolderBinding
import com.lasuak.kvplayer.model.Folder
import java.util.*
import kotlin.collections.ArrayList

class FolderFragment : Fragment(R.layout.fragment_folder), FolderListener,
    SearchView.OnQueryTextListener {
    private lateinit var binding: FragmentFolderBinding
    private lateinit var adapter: FolderAdapter

    companion object {
        var folderList = ArrayList<Folder>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFolderBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        Dexter.withActivity(requireActivity())
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                    folderList = getAllFolder(requireContext())
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    adapter = FolderAdapter(requireContext(), folderList, this@FolderFragment)
                    binding.recyclerView.adapter = adapter
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            })
            .check()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)

        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.queryHint = "Search Folder"

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            val action = FolderFragmentDirections.actionFolderFragmentToSettings()
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Recycle")
    private fun getAllFolder(context: Context): ArrayList<Folder> {
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
                        "FOLDER","all :${cursor.getString(2)} and ${cursor.getString(3)}"
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
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Video.Media._ID,
            )
        } else {
            @Suppress("deprecation")
            arrayOf(
                MediaStore.Video.Media._ID,
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

    override fun onFolderClicked(position: Int, id: Long) {
        val action = FolderFragmentDirections.actionFolderFragmentToVideoFragment(
            id,
            folderList[position].folderName
        )
        findNavController().navigate(action)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val input = newText!!.lowercase(Locale.getDefault())
        val myFiles = ArrayList<Folder>()
        for (item in folderList) {
            if (item.folderName.lowercase(Locale.getDefault()).contains(input)) {
                myFiles.add(item)
            }
        }
        adapter.updateList(myFiles)
        return true
    }
}