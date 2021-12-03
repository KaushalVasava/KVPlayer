package com.lasuak.kvplayer.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.databinding.FragmentPlayerBinding
import com.lasuak.kvplayer.viewmodel.VideoViewModel.Companion.videoList
import java.util.*
import kotlin.collections.ArrayList

class PlayerViewModel : ViewModel() {

    var exoPlayer: ExoPlayer? = null
    private var isMute = 1 //100 for unmute & -100 for mute

    companion object {
        var position = -1
        var isFullscreen = false
        var isLocked = false
        var isRotate = false
        var isSubtitle = true
        lateinit var trackSelector: DefaultTrackSelector
    }

    fun checkOrientation(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = (activity as AppCompatActivity).window.insetsController
            if (controller != null) {
                if (controller.systemBarsBehavior == 0) {
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
                }
            }
        } else {
            val attrs = (activity as AppCompatActivity).window.attributes
            attrs.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
            (activity as AppCompatActivity).window.attributes = attrs
        }
    }

    private fun playClicked() {
        if (exoPlayer!!.isPlaying) {
            exoPlayer!!.pause()
        } else {
            exoPlayer!!.play()
        }
    }

    fun createPlayer(
        context: Context,
        activity: Activity,
        binding: FragmentPlayerBinding,
        pos: Int
    ) {
        exoPlayer = ExoPlayer.Builder(context).build()

            binding.videoName.text = videoList[pos].name
            val mediaItem =
                MediaItem.fromUri(videoList[pos].path)
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.prepare()
        binding.videoView.player = exoPlayer

        trackSelector = DefaultTrackSelector(context)

            if (videoList[pos].height < videoList[pos].width) {
                //for landscape
                (activity as AppCompatActivity).requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                //for portrait
                (activity as AppCompatActivity).requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        exoPlayer!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    nextClicked(context,activity,binding)
                }
            }
        })
    }

    fun lockPlayer(context: Context, binding: FragmentPlayerBinding) {
        if (!isLocked) {
            isLocked = true
            binding.videoView.hideController()
            binding.videoView.useController = false
            binding.lock.setImageResource(R.drawable.ic_lock)
            changeVisibility(View.INVISIBLE,binding)
            binding.lock.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.background
                )
            )
        } else {
            isLocked = false
            binding.videoView.showController()
            binding.videoView.useController = true
            binding.lock.setImageResource(R.drawable.ic_lock_open)
            changeVisibility(View.VISIBLE,binding)
            binding.lock.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.transparent
                )
            )
        }

    }

    fun changeVisibility(visibility: Int, binding: FragmentPlayerBinding) {
        binding.titleBar.visibility = visibility
        binding.exoFullscreen.visibility = visibility
        binding.exoScreenRotation.visibility = visibility
        binding.exoPrev.visibility = visibility
        binding.exoNext.visibility = visibility
        binding.exoMute.visibility = visibility
        binding.seekBar.visibility = visibility
        binding.seekBarVolume.visibility = visibility
        binding.lightText.visibility = visibility
        binding.exoAudioTrack.visibility = visibility
        binding.exoSubtitle.visibility = visibility

        if (isLocked)
            binding.lock.visibility = View.VISIBLE
        else
            binding.lock.visibility = visibility
    }

    fun previousClicked(context: Context,
                                activity: Activity,
                                binding: FragmentPlayerBinding,
                                ) {
        exoPlayer!!.stop()
        exoPlayer!!.release()
        if (position == 0)
            position = videoList.size - 1
        else
            position--
        createPlayer(context, activity, binding, position)
        exoPlayer!!.play()
    }

    fun nextClicked(context: Context,
                            activity: Activity,
                            binding: FragmentPlayerBinding, ) {
        exoPlayer!!.stop()
        exoPlayer!!.release()
        if (position >= videoList.size - 1)
            position = 0
        else
            position++

        createPlayer(context, activity, binding, position)
        exoPlayer!!.play()
    }

    fun playInFullScreen(isEnable: Boolean,binding: FragmentPlayerBinding) {
        if (isEnable) {
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            exoPlayer!!.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.exoFullscreen.setImageResource(R.drawable.ic_fullscreen_exit)
            isFullscreen = true
        } else {
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            exoPlayer!!.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.exoFullscreen.setImageResource(R.drawable.ic_fullscreen)
            isFullscreen = false
        }
    }

    fun setRotation(activity: Activity){
        if (isRotate) {
            (activity as AppCompatActivity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            isRotate = false
        } else {
            (activity as AppCompatActivity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isRotate = true
        }
    }

    fun setAudioTrack(context: Context) {
       playClicked()
       val pref = context.getSharedPreferences("AUDIO_TRACK", Context.MODE_PRIVATE)
       var selectedTrack = pref.getInt("selectedTrack", 0)

       val audioTracks = ArrayList<String>()

       //track fetching
       for (i in 0 until exoPlayer!!.currentTracksInfo.trackGroupInfos.size) {
           if (exoPlayer!!.currentTracksInfo.trackGroupInfos[0].trackGroup.getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT) {
               audioTracks.add(
                   Locale(
                       exoPlayer!!.currentTracksInfo.trackGroupInfos[i].trackGroup.getFormat(0).language.toString()
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
               playClicked()
           }
           //single choice dialog
           .setSingleChoiceItems(tempTrack, selectedTrack) { dialog, which ->
               Toast.makeText(
                   context,
                   "${audioTracks[which]} Selected",
                   Toast.LENGTH_SHORT
               ).show()
               exoPlayer!!.trackSelectionParameters = exoPlayer!!.trackSelectionParameters
                   .buildUpon()
                   .setMaxVideoSizeSd()
                   .setPreferredAudioLanguage(audioTracks[which])
                   .build()
               playClicked()
               val editor =
                   context.getSharedPreferences("AUDIO_TRACK", Context.MODE_PRIVATE).edit()
               editor.putInt("selectedTrack", which)
               editor.apply()
               dialog.dismiss()
           }
           .show()
   }

    fun setSubtitle(context: Context){
        playClicked()
        if (isSubtitle) {
            trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(context)
                .setRendererDisabled(C.TRACK_TYPE_VIDEO, true).build()
            Toast.makeText(context, "Subtitle turned off", Toast.LENGTH_SHORT).show()
            isSubtitle = false
        } else {
            trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(context)
                .setRendererDisabled(C.TRACK_TYPE_VIDEO, false).build()
            Toast.makeText(context, "Subtitle turned on", Toast.LENGTH_SHORT).show()
            isSubtitle = true
        }
        playClicked()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun muteClicked(audioManager:AudioManager,binding: FragmentPlayerBinding) {
        if (isMute == 1) { //for mute
            audioManager.adjustVolume(
                AudioManager.ADJUST_MUTE,
                AudioManager.FLAG_PLAY_SOUND
            )
            binding.exoMute.setImageResource(R.drawable.ic_volume_off_24)
            isMute = 0
        } else {
            //for unmute
            audioManager.adjustVolume(
                AudioManager.ADJUST_UNMUTE,
                AudioManager.FLAG_PLAY_SOUND
            )
            binding.exoMute.setImageResource(R.drawable.ic_volume_up_24)
            isMute = 1
        }
    }

    fun backToDefaultOrientation(activity: Activity){
        (activity as AppCompatActivity).requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_SENSOR

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = activity.window.insetsController
            if (controller != null) {
                if (controller.systemBarsBehavior == 0) {
                    controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
                }
            }
        } else {
            val attrs = (activity as AppCompatActivity).window.attributes
            attrs.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            (activity as AppCompatActivity).window.attributes = attrs
        }
    }

}