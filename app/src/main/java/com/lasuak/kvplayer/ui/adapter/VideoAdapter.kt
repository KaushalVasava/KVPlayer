package com.lasuak.kvplayer.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.lasuak.kvplayer.ui.adapter.viewholder.VideoViewHolder
import com.lasuak.kvplayer.databinding.VideoItemBinding
import com.lasuak.kvplayer.model.Video

class VideoAdapter(private val videoListener: VideoListener) :
    ListAdapter<Video, VideoViewHolder>(VideoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding, videoListener)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Video, newItem: Video) = oldItem == newItem
    }
}

interface VideoListener {
    fun onItemClicked(position: Int, id: Long)
    fun onItemDeleteClicked(position: Int)
    fun showDetailsClicked(video: Video)
    fun shareVideoClicked(video: Video)
}