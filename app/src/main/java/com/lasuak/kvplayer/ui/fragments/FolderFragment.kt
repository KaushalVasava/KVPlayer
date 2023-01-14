package com.lasuak.kvplayer.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.ui.adapter.FolderAdapter
import com.lasuak.kvplayer.ui.adapter.FolderListener
import com.lasuak.kvplayer.databinding.FragmentFolderBinding
import com.lasuak.kvplayer.ui.viewmodel.FolderViewModel

class FolderFragment : Fragment(R.layout.fragment_folder), FolderListener {
    private lateinit var binding: FragmentFolderBinding
    private lateinit var folderAdapter: FolderAdapter
    private val viewModel: FolderViewModel by viewModels()

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

//        if (video != null) {
//            val position = 0
//            globalList.add(0, video!!)
//            val action = FolderFragmentDirections.actionFolderFragmentToPlayerFragment(
//                position,
//                "EXTERNAL"
//            )
//            findNavController().navigate(action)
//            video = null
//        }
//        if (uri != null) {
//            var position = -1
//            globalList = viewModel.getAllVideo(requireContext())
//            for (index in 0 until globalList.size) {
//                if (uri.toString().contains(globalList[index].name)) {
//                    position = index
//                }
//            }
//            val action =
//                FolderFragmentDirections.actionFolderFragmentToPlayerFragment(position, "EXTERNAL")
//            findNavController().navigate(action)
//            uri = null
//        }

        return binding.root
    }

//    private fun refresh() {
//        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
//            .create(FolderViewModel::class.java)
//        viewModel.getFolders().observe(viewLifecycleOwner) { folder ->
//            // Update the UI
//            folderList.clear()
//            folderList.addAll(folder)
//        }
//    }

    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle Permission granted/rejected
        var mStoragePermissionGranted = false
        permissions.entries.forEach {
            mStoragePermissionGranted = it.value
        }
        if (mStoragePermissionGranted) {
            val list = viewModel.getAllFolder(requireContext())
            folderAdapter.submitList(list)
        }
    }

    override fun onFolderClicked(position: Int, id: Long) {
        val action = FolderFragmentDirections.actionFolderFragmentToVideoFragment(
                id,
                folderAdapter.currentList[position].folderName
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
            val list = viewModel.getAllFolder(requireContext())
            folderAdapter.submitList(list)
        }
    }
}