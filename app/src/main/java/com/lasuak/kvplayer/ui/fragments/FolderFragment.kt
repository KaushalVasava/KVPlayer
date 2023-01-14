package com.lasuak.kvplayer.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.ui.adapter.FolderAdapter
import com.lasuak.kvplayer.ui.adapter.FolderListener
import com.lasuak.kvplayer.databinding.FragmentFolderBinding
import com.lasuak.kvplayer.model.Folder
import com.lasuak.kvplayer.util.FolderUtil

class FolderFragment : Fragment(R.layout.fragment_folder), FolderListener {

    private lateinit var binding: FragmentFolderBinding
    private lateinit var folderAdapter: FolderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFolderBinding.inflate(inflater, container, false)
        folderAdapter = FolderAdapter(this)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            this.adapter = folderAdapter
        }
        checkPermission()
        binding.btnResumeVideo.setOnClickListener {
            getSharedPrefData()
        }
        return binding.root
    }


    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var mStoragePermissionGranted = false
        permissions.entries.forEach {
            mStoragePermissionGranted = it.value
        }
        if (mStoragePermissionGranted) {
            val list = FolderUtil.getAllFolder(requireContext())
            folderAdapter.submitList(list)
        }
    }

    override fun onFolderClicked(position: Int, folder: Folder) {
        val action = FolderFragmentDirections.actionFolderFragmentToVideoFragment(
            folder.id,
            folder.folderName
        )
        findNavController().navigate(action)
    }

    private fun checkPermission() {
        val array = if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            array.toString()
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(array)
        } else {
            val list = FolderUtil.getAllFolder(requireContext())
            folderAdapter.submitList(list)
        }
    }

    private fun getSharedPrefData() {
        val pref = requireContext().getSharedPreferences("LAST_VIDEO_DATA", Context.MODE_PRIVATE)
        val videoId = pref.getLong("VIDEO_ID", -1L)
        val folderId = pref.getLong("FOLDER_ID", -1L)
        if (folderId == -1L && videoId == -1L) {
            Toast.makeText(requireContext(), "No recently played video found", Toast.LENGTH_SHORT)
                .show()
        } else {
            val video = FolderUtil.findVideo(requireContext(), folderId, videoId)
            val action = FolderFragmentDirections.actionFolderFragmentToPlayerFragment(
                folderId,
                -1,
                video
            )
            findNavController().navigate(action)
        }
    }
}