package com.lasuak.kvplayer.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.adapter.VideoAdapter
import com.lasuak.kvplayer.adapter.VideoListener
import com.lasuak.kvplayer.databinding.FragmentVideoBinding
import com.lasuak.kvplayer.model.Video
import com.lasuak.kvplayer.viewmodel.VideoViewModel
import java.util.*
import kotlin.collections.ArrayList

class VideoFragment : Fragment(R.layout.fragment_video), VideoListener,
    SearchView.OnQueryTextListener {
    private lateinit var binding: FragmentVideoBinding
    private val args: VideoFragmentArgs by navArgs()
    private lateinit var viewModel: VideoViewModel

    companion object {
        //        var videoList = MutableLiveData<ArrayList<Video>>()
        var videoList = ArrayList<Video>()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentVideoBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this).get(VideoViewModel::class.java)
        videoList = viewModel.getVideo(requireContext(), args.folderId)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.adapter = VideoAdapter(requireContext(),viewModel,videoList, this)
        binding.recyclerView.adapter = viewModel.adapter

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
//        if(item.itemId==R.id.online_play){
//            val action = VideoFragmentDirections.actionVideoFragmentToPlayerFragment(-1,"ON")
//            findNavController().navigate(action)
//        }
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
        viewModel.adapter.updateList(myFiles)
        return true
    }
    override fun onItemClicked(position: Int, id: Long) {
        val action = VideoFragmentDirections.actionVideoFragmentToPlayerFragment(
            position,
            videoList[position].name
        )
        findNavController().navigate(action)
    }

    override fun onItemDeleteClicked(position: Int) {
        viewModel.deleteDialog(requireContext(),position,viewLifecycleOwner)
    }
}