package com.lasuak.kvplayer.fragments

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.adapter.VideoAdapter
import com.lasuak.kvplayer.adapter.VideoListener
import com.lasuak.kvplayer.databinding.FragmentVideoBinding
import com.lasuak.kvplayer.model.Video
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class VideoFragment : Fragment(R.layout.fragment_video), VideoListener,
    SearchView.OnQueryTextListener {
    private lateinit var binding: FragmentVideoBinding
    private lateinit var adapter: VideoAdapter
    private val args: VideoFragmentArgs by navArgs()

    companion object {
        var videoList = ArrayList<Video>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentVideoBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        videoList = getVideo(requireContext(), args.folderId)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VideoAdapter(requireContext(), videoList, this)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)

        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.queryHint = "Search Video"

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            val action = VideoFragmentDirections.actionVideoFragmentToSettings()
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val input = newText!!.lowercase(Locale.getDefault())
        val myFiles = ArrayList<Video>()
        for (item in videoList) {
            if (item.name.lowercase(Locale.getDefault()).contains(input)) {
                myFiles.add(item)
            }
        }
        adapter.updateList(myFiles)
        return true
    }

    @SuppressLint("Recycle")
    fun getVideo(context: Context, folderId: Long): ArrayList<Video> {
        val list = ArrayList<Video>()
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
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
                MediaStore.Video.VideoColumns.RESOLUTION,
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
                MediaStore.Video.VideoColumns.RESOLUTION,
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
                    Log.d(
                        "PATH", "${cursor.getString(4)}\n${cursor.getString(5)}\n" +
                                "${cursor.getString(6)}\n${cursor.getString(7)}\n"
                    )
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

    override fun onItemClicked(position: Int, id: Long) {
        val action = VideoFragmentDirections.actionVideoFragmentToPlayerFragment(
            position,
            videoList[position].name
        )
        findNavController().navigate(action)
    }

    override fun onAnyItemLongClicked(position: Int) {
    }

    override fun onItemDeleteClicked(position: Int) {
    }
}