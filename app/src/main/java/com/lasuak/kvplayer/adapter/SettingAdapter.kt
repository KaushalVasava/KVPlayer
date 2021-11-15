package com.lasuak.kvplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.fragments.SettingsFragment
import kotlin.collections.ArrayList

class SettingAdapter constructor(private var context: Context, private var list:ArrayList<SettingsFragment.Setting>,
                                 private var listener1: SettingListener)
    : RecyclerView.Adapter<SettingAdapter.SettingViewHolder?>() {

    private val listener: SettingListener = listener1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):SettingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.settings_item, parent, false)
        return SettingViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        holder.headerName.text = list[position].headerName
        holder.subName.text = list[position].subName
        holder.itemView.setOnClickListener {
            listener.onSettingClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class SettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var headerName: TextView = itemView.findViewById(R.id.headerName)
        var subName: TextView = itemView.findViewById(R.id.subName)
        var cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
    }
}

interface SettingListener {
    fun onSettingClicked(position: Int)
}