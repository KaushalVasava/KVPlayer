package com.lasuak.kvplayer.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.lasuak.kvplayer.ui.adapter.viewholder.VideoViewHolder
import com.lasuak.kvplayer.databinding.VideoItemBinding
import com.lasuak.kvplayer.model.Video

class VideoAdapter(private val videoListener: VideoListener) :
    ListAdapter<Video, VideoViewHolder>(VideoDiffCallback()), Filterable {
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

    val list = mutableListOf<Video>()

    init {
        list.addAll(currentList)
    }

    fun setData(list: List<Video>) {
        this.list.clear()
        this.list.addAll(list)
        submitList(list)
    }

    override fun getFilter(): Filter = customFilter

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = mutableListOf<Video>()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(list)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()

                for (item in list) {
                    if (item.name.lowercase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            Log.d("TAG", "performFiltering: ${filteredList.size}")
            return results
        }

        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            if (constraint != "") {
                val list: List<Video> = filterResults?.values as List<Video>
                Log.d("TAG", "publishResults: ${list.size}")
                setData(list)
            }
        }
    }
}

interface VideoListener {
    fun onItemClicked(position: Int, video: Video)
    fun onItemDeleteClicked(position: Int)
    fun showDetailsClicked(video: Video)
    fun shareVideoClicked(video: Video)
}