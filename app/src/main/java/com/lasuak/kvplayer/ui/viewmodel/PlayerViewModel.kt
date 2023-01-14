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
import java.util.*
import kotlin.collections.ArrayList

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
        playClicked(exoPlayer)
        val pref = context.getSharedPreferences("AUDIO_TRACK", Context.MODE_PRIVATE)
        var selectedTrack = pref.getInt("selectedTrack", 0)

        val audioTracks = ArrayList<String>()

        //track fetching
        for (i in 0 until exoPlayer.currentTracksInfo.trackGroupInfos.size) {
            if (exoPlayer.currentTracksInfo.trackGroupInfos[0].trackGroup.getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT) {
                audioTracks.add(
                    Locale(
                        exoPlayer.currentTracksInfo.trackGroupInfos[i].trackGroup.getFormat(0).language.toString()
                    ).displayLanguage
                )
            }
        }

        if (audioTracks.size != 0) {
            audioTracks.removeAt(0)
        }
        if (audioTracks.size == 0) {
            audioTracks.add("Default")
            selectedTrack = 0
        }
        val tempTrack = audioTracks.toArray(arrayOfNulls<CharSequence>(audioTracks.size))

        MaterialAlertDialogBuilder(context)
            .setTitle("Select Language")
            .setOnCancelListener {
                it.dismiss()
                playClicked(exoPlayer)
            }
            //single choice dialog
            .setSingleChoiceItems(tempTrack, selectedTrack) { dialog, which ->
                Toast.makeText(
                    context,
                    "${audioTracks[which]} Selected",
                    Toast.LENGTH_SHORT
                ).show()
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setMaxVideoSizeSd()
                    .setPreferredAudioLanguage(audioTracks[which])
                    .build()
                playClicked(exoPlayer)
                val editor =
                    context.getSharedPreferences("AUDIO_TRACK", Context.MODE_PRIVATE).edit()
                editor.putInt("selectedTrack", which)
                editor.apply()
                dialog.dismiss()
            }
            .show()
    }

    fun setSubtitle(context: Context, exoPlayer: ExoPlayer, isSubtitleEnable: Boolean): Boolean {
        playClicked(exoPlayer)
        return if (isSubtitleEnable) {
            trackSelector?.parameters = DefaultTrackSelector.ParametersBuilder(context)
                .setRendererDisabled(C.TRACK_TYPE_VIDEO, true).build()
            Toast.makeText(context, context.getString(R.string.subtitle_off), Toast.LENGTH_SHORT).show()
            playClicked(exoPlayer)
            false
        } else {
            trackSelector?.parameters = DefaultTrackSelector.ParametersBuilder(context)
                .setRendererDisabled(C.TRACK_TYPE_VIDEO, false).build()
            Toast.makeText(context, context.getString(R.string.subtitle_on), Toast.LENGTH_SHORT).show()
            playClicked(exoPlayer)
            true
        }
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