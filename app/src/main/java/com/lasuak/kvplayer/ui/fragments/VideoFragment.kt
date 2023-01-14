package com.lasuak.kvplayer.ui.fragments

import android.os.Bundle
import android.view.*
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

class VideoFragment : Fragment(R.layout.fragment_video), VideoListener {
    private lateinit var binding: FragmentVideoBinding
    private val args: VideoFragmentArgs by navArgs()
    private val viewModel: VideoViewModel by viewModels()
    private lateinit var videoAdapter: VideoAdapter

    companion object {
        //        private const val FOLDER_LIST_BUNDLE_KEY = "folder_list_bundle_key"
        const val VIDEO_LIST_BUNDLE_KEY = "video_list_bundle_key"

        //        private const val FOLDER_BUNDLE_KEY = "folder_bundle_key"
        const val VIDEO_BUNDLE_KEY = "video_bundle_key"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)

        videoAdapter = VideoAdapter(this)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = videoAdapter
        }
        val videoList = viewModel.getVideosByFolder(requireContext(), args.folderId)
        videoAdapter.submitList(videoList)
        setVideoListResult(videoList)
        return binding.root
    }

    private fun setVideoListResult(videoList: List<Video>) {
        setFragmentResult(
            VIDEO_BUNDLE_KEY,
            bundleOf(
                VIDEO_LIST_BUNDLE_KEY to videoList,
            )
        )
    }

    override fun onItemClicked(position: Int, id: Long) {
        val action = VideoFragmentDirections.actionVideoFragmentToPlayerFragment(
                position,
                videoAdapter.currentList[position].name
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
        viewModel.shareVideo(requireContext(), video)
    }
}