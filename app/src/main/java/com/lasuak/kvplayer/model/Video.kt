package com.lasuak.kvplayer.model

import android.content.Context
import android.widget.Toast

data class Video(
    val name: String,
    val path: String,
    val duration: Long,
    val id: Long,
    val size: Double,
    val type: String,
    val date_added: String,
    val resolution: String?,
    val height: Int,
    val width: Int
)

fun notifyUser(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

