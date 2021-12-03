package com.lasuak.kvplayer.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lasuak.kvplayer.MainActivity.Companion.uri
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.adapter.FolderAdapter
import com.lasuak.kvplayer.adapter.FolderListener
import com.lasuak.kvplayer.databinding.FragmentFolderBinding
import com.lasuak.kvplayer.model.Folder
import com.lasuak.kvplayer.model.Video
import com.lasuak.kvplayer.viewmodel.FolderViewModel
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "FolderFragment"

class FolderFragment : Fragment(R.layout.fragment_folder), FolderListener,
    SearchView.OnQueryTextListener {
    private lateinit var binding: FragmentFolderBinding

    private lateinit var adapter: FolderAdapter
    private lateinit var viewModel: FolderViewModel
    private var mStoragePermissionGranted = false
    private val REQUEST_PERMISSION_CODE = 103

    companion object {
        var globalList = ArrayList<Video>()
        var folderList = mutableListOf<Folder>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFolderBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        //viewModel = ViewModelProvider(this).get(FolderViewModel::class.java)
        if (uri != null) {
            var position = -1
            Log.d("TAG", "onCreateView: uri not null $uri")
            globalList = viewModel.getAllVideo(requireContext())
            for (index in 0 until globalList.size) {
                Log.d("TAG", "onCreateView: ${globalList[index].name}")
                if (uri.toString().contains(globalList[index].name)) {
                    Log.d("TAG", "onCreateView: matched")
                    position = index
                }
            }
            val action =
                FolderFragmentDirections.actionFolderFragmentToPlayerFragment(position, "EXTERNAL")
            findNavController().navigate(action)
            uri = null
        } else {
            getPermissions()
            if (mStoragePermissionGranted) {
                viewModel.getFolders().observe(viewLifecycleOwner, { folder ->
                    // Update the UI
                    Log.i(TAG, "Received contacts from view model")
                    folderList.clear()
                    folderList.addAll(folder)
                    adapter.notifyDataSetChanged()
                })
            }
//            viewModel.checkAppPermission(
//                requireContext(),
//                requireActivity(),
//                binding,
//                this@FolderFragment
//            )
        }
        return binding.root
    }

    private fun initFolder() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FolderAdapter(requireContext(), folderList as ArrayList<Folder>, this)
        binding.recyclerView.adapter = adapter
        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(FolderViewModel::class.java)
    }


    private fun getPermissions() {
        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity().applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "getLocationPermission: granted")
                initFolder()
                mStoragePermissionGranted = true
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    permissions,
                    REQUEST_PERMISSION_CODE
                )
            }
        } else {
            Log.d(TAG, "getLocationPermission: not granted")
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                REQUEST_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: called.")
        mStoragePermissionGranted = false
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (grantResults.size > 0) {
                    var i = 0
                    while (i < grantResults.size) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mStoragePermissionGranted = false
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                        i++
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    mStoragePermissionGranted = true
                    //initialize our map
                  //  initFolder()
                }
            }
        }
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