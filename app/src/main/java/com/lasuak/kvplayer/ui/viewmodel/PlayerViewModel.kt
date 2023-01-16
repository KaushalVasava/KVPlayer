package com.lasuak.kvplayer.ui.viewmodel

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.util.AppConstant
import java.util.*

class PlayerViewModel : ViewModel() {
    var trackSelector: DefaultTrackSelector? = null

    private fun playClicked(exoPlayer: ExoPlayer) {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun setAudioTrack(context: Context, exoPlayer: ExoPlayer) {
        if (exoPlayer.isPlaying) {
            playClicked(exoPlayer)
        }
        val pref = context.getSharedPreferences(AppConstant.AUDIO_TRACK, Context.MODE_PRIVATE)
        var selectedTrack = pref.getInt(AppConstant.SELECTED_AUDIO_TRACK, 0)

        val audioTracks = mutableListOf<String>()
        //track fetching
        val trackGroup = exoPlayer.currentTracks.groups
        for (i in 0 until trackGroup.size) {
            val d = trackGroup[i].mediaTrackGroup.getFormat(0)
            if (trackGroup[i].mediaTrackGroup.type == C.TRACK_TYPE_AUDIO) {
                audioTracks.add(
                    trackGroup[i].mediaTrackGroup.getFormat(0).language.toString()
                )
            }
        }
        if (audioTracks.isEmpty()) {
            audioTracks.add(context.getString(R.string.default_language))
            selectedTrack = 0
        }
        val tempTrack = audioTracks.map { s ->
            if (s == context.getString(R.string.default_language)) {
                s
            } else {
                Locale(s).displayLanguage
            }
        }.toTypedArray()

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.select_language))
            .setOnCancelListener {
                it.dismiss()
                playClicked(exoPlayer)
            }
            //single choice dialog
            .setSingleChoiceItems(tempTrack, selectedTrack) { dialog, which ->
                Toast.makeText(
                    context,
                    context.getString(R.string.item_selected, tempTrack[which]),
                    Toast.LENGTH_SHORT
                ).show()
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setMaxVideoSizeSd()
                    .setPreferredAudioLanguage(audioTracks[which])
                    .build()
                playClicked(exoPlayer)
                val editor =
                    context.getSharedPreferences(AppConstant.AUDIO_TRACK, Context.MODE_PRIVATE)
                        .edit()
                editor.putInt(AppConstant.SELECTED_AUDIO_TRACK, which)
                editor.apply()
                dialog.dismiss()
            }
            .show()
    }

    fun setSubtitle(context: Context, exoPlayer: ExoPlayer): Boolean {
        if (exoPlayer.isPlaying) {
            playClicked(exoPlayer)
        }
        val pref = context.getSharedPreferences(AppConstant.SUBTITLE_TRACK, Context.MODE_PRIVATE)
        var selectedTrack = pref.getInt(AppConstant.SELECTED_SUBTITLE_TRACK, 0)

        val subtitleTracks = mutableListOf<String>()
        subtitleTracks.add(context.getString(R.string.off_subtitle))
        //track fetching
        val trackGroup = exoPlayer.currentTracks.groups
        for (i in 0 until trackGroup.size) {
            if (trackGroup[i].mediaTrackGroup.type == C.TRACK_TYPE_TEXT) {
                subtitleTracks.add(
                    trackGroup[i].mediaTrackGroup.getFormat(0).language.toString()
                )
            }
        }
        val tempTrack = subtitleTracks.mapIndexed { index, s ->
            if (index == 0) {
                s
            } else {
                Locale(s).displayLanguage
            }
        }.toTypedArray()

        if (subtitleTracks.isEmpty()) {
            selectedTrack = 0
        }
        var isSubtitleSelected = false
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.select_subtitle))
            .setOnCancelListener {
                trackSelector?.setParameters(
                    DefaultTrackSelector.Parameters.Builder(context)
                        .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
                )
                it.dismiss()
                playClicked(exoPlayer)
            }
            //single choice dialog
            .setSingleChoiceItems(tempTrack, selectedTrack) { dialog, which ->
                if (which == 0) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.subtitle_off),
                        Toast.LENGTH_SHORT
                    ).show()
                    trackSelector?.setParameters(
                        DefaultTrackSelector.Parameters.Builder(context)
                            .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
                    )
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.item_selected, tempTrack[which]),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isSubtitleSelected = which != 0
                trackSelector?.setParameters(
                    DefaultTrackSelector.Parameters.Builder(context)
                        .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                        .setPreferredTextLanguage(subtitleTracks[which])
                )
                playClicked(exoPlayer)
                val editor =
                    context.getSharedPreferences(AppConstant.SUBTITLE_TRACK, Context.MODE_PRIVATE)
                        .edit()
                editor.putInt(AppConstant.SELECTED_SUBTITLE_TRACK, which)
                editor.apply()
                dialog.dismiss()
            }
            .show()
        return isSubtitleSelected
    }

    fun muteClicked(isMuted: Boolean, audioManager: AudioManager): Boolean {
        return if (isMuted) {
            //for unmute
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustVolume(
                    AudioManager.ADJUST_UNMUTE,
                    AudioManager.FLAG_PLAY_SOUND
                )
            }
            false
        } else {
            //for mute
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustVolume(
                    AudioManager.ADJUST_MUTE,
                    AudioManager.FLAG_PLAY_SOUND
                )
            }
            true
        }
    }
}