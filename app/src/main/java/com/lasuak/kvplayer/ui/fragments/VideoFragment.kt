package com.lasuak.kvplayer.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.ui.adapter.VideoAdapter
import com.lasuak.kvplayer.ui.adapter.VideoListener
import com.lasuak.kvplayer.databinding.FragmentVideoBinding
import com.lasuak.kvplayer.model.Video
import com.lasuak.kvplayer.ui.viewmodel.VideoViewModel
import com.lasuak.kvplayer.util.VideoUtil
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class VideoFragment : Fragment(R.layout.fragment_video), VideoListener {

    private lateinit var binding: FragmentVideoBinding
    private val args: VideoFragmentArgs by navArgs()
    private val viewModel: VideoViewModel by viewModels()
    private lateinit var videoAdapter: VideoAdapter

    companion object {
        const val VIDEO_LIST_BUNDLE_KEY = "video_list_bundle_key"
        const val VIDEO_BUNDLE_KEY = "video_bundle_key"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        @Suppress("deprecation")
        setHasOptionsMenu(true)
        videoAdapter = VideoAdapter(this)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            FastScrollerBuilder(this).build() // scrollbar
            adapter = videoAdapter
        }
        val videoList = VideoUtil.getVideosByFolder(requireContext(), args.folderId)
        videoAdapter.submitList(videoList)
        setVideoListResult(videoList)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchByContract = menu.findItem(R.id.search)
        val searchContractView = searchByContract.actionView as SearchView
        searchContractView.queryHint = getString(R.string.search_video)

        searchContractView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    val videoList = VideoUtil.getVideosByFolder(requireContext(), args.folderId)
                    videoAdapter.submitList(videoList)
                    videoAdapter.setData(videoList)
                } else {
                    videoAdapter.filter.filter(newText)
                }
                return false
            }
        })
        @Suppress("deprecation")
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setVideoListResult(videoList: List<Video>) {
        setFragmentResult(
            VIDEO_BUNDLE_KEY,
            bundleOf(
                VIDEO_LIST_BUNDLE_KEY to videoList,
            )
        )
    }

    override fun onItemClicked(position: Int, video: Video) {
        val action = VideoFragmentDirections.actionVideoFragmentToPlayerFragment(
            args.folderId,
            position,
            video
        )
        findNavController().navigate(action)
    }

    override fun onItemDeleteClicked(position: Int) {
        viewModel.deleteDialog(requireContext(), position, videoAdapter.currentList, videoAdapter)
    }

    override fun showDetailsClicked(video: Video) {
        viewModel.showDetailsDialog(requireContext(), video)
    }

    override fun shareVideoClicked(video: Video) {
        VideoUtil.shareVideo(requireContext(), video)
    }
}