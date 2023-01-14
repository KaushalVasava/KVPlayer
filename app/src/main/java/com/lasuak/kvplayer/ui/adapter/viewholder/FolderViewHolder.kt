package com.lasuak.kvplayer.ui.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.ui.adapter.FolderListener
import com.lasuak.kvplayer.databinding.FolderItemBinding
import com.lasuak.kvplayer.model.Folder

class FolderViewHolder(
    private val binding: FolderItemBinding,
    private val listener: FolderListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(folder: Folder) {
        binding.txtName.text = folder.folderName
        binding.txtTotalVideos.text =
            binding.root.context.getString(R.string.total_videos, folder.totalVideo.toString())

        binding.root.setOnClickListener {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onFolderClicked(
                    position,
                    folder
                )
            }
        }
    }
}