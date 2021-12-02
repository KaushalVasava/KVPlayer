package com.lasuak.kvplayer.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.lasuak.kvplayer.MainActivity.Companion.uri
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.adapter.FolderListener
import com.lasuak.kvplayer.databinding.FragmentFolderBinding
import com.lasuak.kvplayer.model.Folder
import com.lasuak.kvplayer.model.Video
import com.lasuak.kvplayer.viewmodel.FolderViewModel
import com.lasuak.kvplayer.viewmodel.FolderViewModel.Companion.folderList
import java.util.*
import kotlin.collections.ArrayList

class FolderFragment : Fragment(R.layout.fragment_folder), FolderListener,
    SearchView.OnQueryTextListener {
    private lateinit var binding: FragmentFolderBinding
    //private lateinit var adapter: FolderAdapter
    private lateinit var viewModel: FolderViewModel

   companion object{
       var globalList = ArrayList<Video>()
   }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFolderBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this).get(FolderViewModel::class.java)
        if(uri!=null)
        {
            var position = -1
            Log.d("TAG", "onCreateView: uri not null $uri")
            globalList = viewModel.getAllVideo(requireContext())
            for(index in 0 until globalList.size){
                Log.d("TAG", "onCreateView: ${globalList[index].name}")
                if(uri.toString().contains(globalList[index].name))
                {
                    Log.d("TAG", "onCreateView: matched")
                    position=index
                }
            }
            val action = FolderFragmentDirections.actionFolderFragmentToPlayerFragment(position,"EXTERNAL")
            findNavController().navigate(action)
            uri = null
        }
        else {
            viewModel.checkAppPermission(
                requireContext(),
                requireActivity(),
                binding,
                this@FolderFragment
            )
        }
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
        viewModel.adapter.updateList(myFiles)
        return true
    }
}