package com.lasuak.kvplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.model.Folder
import com.lasuak.kvplayer.model.Video
import kotlin.collections.ArrayList

class FolderAdapter constructor(
    private var context: Context, private var list: ArrayList<Folder>,
    listener1: FolderListener
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder?>() {

    private val listener: FolderListener = listener1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.folder_item, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.folderName.text = list[position].folderName
        val total = list[position].totalVideo.toString() + " videos"
        holder.songCount.text = total

        holder.itemView.setOnClickListener {
            listener.onFolderClicked(
                position,
                list[position].folderId
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<Folder>?) {
        list = ArrayList()
        list.clear()
        list.addAll(newList!!)
        notifyDataSetChanged()
    }

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var folderName: TextView = itemView.findViewById(R.id.folderName)
        var songCount: TextView = itemView.findViewById(R.id.totalSong)
    }
}

interface FolderListener {
    fun onFolderClicked(position: Int, id: Long)
}