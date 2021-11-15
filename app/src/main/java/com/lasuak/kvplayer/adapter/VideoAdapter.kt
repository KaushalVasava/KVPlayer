package com.lasuak.kvplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.model.Video
import com.lasuak.kvplayer.model.shareVideo
import com.lasuak.kvplayer.model.showDetailsDialog
import com.lasuak.kvplayer.viewmodel.VideoViewModel
import java.util.ArrayList

class VideoAdapter constructor(
    private var context: Context, private var list: ArrayList<Video>,
    listener1: VideoListener
) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder?>() {
    private val listener: VideoListener = listener1
    lateinit var viewGroup: ViewGroup
    lateinit var viewModel: VideoViewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.videoTitle.text = list[position].name
        holder.videoDuration.text =
            DateUtils.formatElapsedTime(list[position].duration / 1000)
                .toString()//formattedTime(list[position].duration!!.toInt() / 1000)

        Glide.with(context).load(list[position].path)
            .into(holder.videoImage)

        holder.itemView.setOnClickListener {
            listener.onItemClicked(
                position,
                list[position].id
            )
        }
        holder.moreOption.setOnClickListener { v ->
            val popupMenu = PopupMenu(context, v)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                //   viewModel = ViewModelProvider(view).get(VideoViewModel::class.java)
//                viewModel = ViewModelProvider.Factory()
                when (item.itemId) {
                    R.id.details -> showDetailsDialog(context, list, position)
                    R.id.share -> shareVideo(context, list, position)
                }
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoTitle: TextView = itemView.findViewById(R.id.videoName)
        var videoImage: ShapeableImageView = itemView.findViewById(R.id.videoImage)
        var videoDuration: TextView = itemView.findViewById(R.id.duration)
        var moreOption: ImageView = itemView.findViewById(R.id.more_option)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<Video>?) {
        list = ArrayList()
        list.clear()
        list.addAll(newList!!)
        notifyDataSetChanged()
    }
}

interface VideoListener {
    fun onItemClicked(position: Int, id: Long)
    fun onAnyItemLongClicked(position: Int)
    fun onItemDeleteClicked(position: Int)
}