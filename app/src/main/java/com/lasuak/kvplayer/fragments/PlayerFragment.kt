package com.lasuak.kvplayer.fragments

import android.app.Service
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.databinding.FragmentPlayerBinding
import com.lasuak.kvplayer.fragments.VideoFragment.Companion.videoList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class PlayerFragment : Fragment(R.layout.fragment_player), GestureDetector.OnGestureListener {
    private lateinit var binding: FragmentPlayerBinding
    private val args: PlayerFragmentArgs by navArgs()
    private var exoPlayer: ExoPlayer? = null
    private lateinit var audioManager: AudioManager
    private lateinit var gestureDetector: GestureDetector

    private var isMute = 1 //100 for unmute & -100 for mute
    //private var brightness: Int=255

    companion object {
        private var position = -1
        private var isFullscreen = false
        private var isLocked = false
        private var isRotate = false
        private lateinit var trackSelector: DefaultTrackSelector
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        audioManager = requireActivity().getSystemService(Service.AUDIO_SERVICE) as AudioManager

        binding = FragmentPlayerBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar!!.hide()

        position = args.position


        //swipe gesture for next and previous song and back to home fragment
        gestureDetector = GestureDetector(requireContext(), this)
        container!!.setOnTouchListener { v, event ->
            onTouchEvent(event)
            true
        }

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
//                (activity as AppCompatActivity).window.setFlags(
//                 WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val attrs = (activity as AppCompatActivity).window.attributes
            attrs.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
            (activity as AppCompatActivity).window.attributes = attrs
            //   }
        }

        //playClicked(position)
        createPlayer(position)
//        brightness = Settings.System.getInt(
//            requireContext().contentResolver,
//            Settings.System.SCREEN_BRIGHTNESS, 0
//        )

        //  binding.seekBar.progress = brightness

//        binding.seekBar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
//            override fun onStartTrackingTouch(slider: Slider) {
//            }
//
//            override fun onStopTrackingTouch(slider: Slider) {
//                val progress = slider.value
//                Log.d("TOUCH", "onStopTrackingTouch: $progress")
//                val layout = requireActivity().window.attributes
//                layout.screenBrightness = progress
//                //1F
//                requireActivity().window.attributes = layout
////
//            }
//        })

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
//                  Settings.System.putInt(requireContext().contentResolver,
//                  Settings.System.SCREEN_BRIGHTNESS,progress)
                val layout = requireActivity().window.attributes
                Log.d("Seek", "onProgressChanged: ${layout.screenBrightness} and  $progress ")
                layout.screenBrightness = progress / 100.toFloat()
                binding.lightText.text = progress.toString()
                //1F
                requireActivity().window.attributes = layout
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

//        binding.seekBarVolume.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
//            override fun onStartTrackingTouch(slider: Slider) {
//            }
//
//            override fun onStopTrackingTouch(slider: Slider) {
//                var value = slider.value
//                value *= 100
//                Log.d("TOUCH", "onStopTrackingTouch: $value and v ${value.toInt()}")
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value.toInt(), 0)
//            }
//        })
        binding.seekBarVolume.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    newVolume: Int,
                    fromUser: Boolean
                ) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })


        binding.backBtn.setOnClickListener {
            findNavController().popBackStack(R.id.videoFragment, true)
        }

        binding.exoPrev.setOnClickListener {
            previousClicked()
        }
        binding.exoNext.setOnClickListener {
            nextClicked()
        }
        setVisibility()
        binding.exoFullscreen.setOnClickListener {
            if (isFullscreen) {
                playInFullScreen(false)
            } else {
                playInFullScreen(true)
            }
        }
        binding.lock.setOnClickListener {
            lockPlayer()
        }
        binding.exoScreenRotation.setOnClickListener {
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

        binding.exoMute.setOnClickListener {
            muteClicked()
        }
        binding.exoAudioTrack.setOnClickListener {
            playClicked()
            val pref =requireActivity().getSharedPreferences("AUDIO_TRACK", MODE_PRIVATE)
            var selectedTrack = pref.getInt("selectedTrack",0)

            val audioTracks = ArrayList<String>()

            //track fetching
//            for(i in 0..exoPlayer!!.currentTrackGroups.length-1){
//                if(exoPlayer!!.currentTrackGroups[i].getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT){
//                    audioTracks.add(Locale(exoPlayer!!.currentTrackGroups[i].getFormat(0).language.toString()).displayLanguage)
//                }
//            }
            for (i in 0..exoPlayer!!.currentTracksInfo.trackGroupInfos.size - 1) {
                if (exoPlayer!!.currentTracksInfo.trackGroupInfos[0].trackGroup.getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT) {
                    audioTracks.add(
                        Locale(
                            exoPlayer!!.currentTracksInfo.trackGroupInfos[i].trackGroup.getFormat(0).language.toString()
                        ).displayLanguage
                    )
                }
            }

            if(audioTracks.size!=0){
                audioTracks.removeAt(0)
            }
            if(audioTracks.size==0){
                audioTracks.add("Default")
                selectedTrack =0
            }
            val tempTrack = audioTracks.toArray(arrayOfNulls<CharSequence>(audioTracks.size))

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Language")
                 .setOnCancelListener {
                     it.dismiss()
                     playClicked()
                 }
//                .setSingleChoiceItems(tempTrack, position, DialogInterface.OnClickListener{
//                })
                .setSingleChoiceItems(tempTrack, selectedTrack) { dialog, which ->
                    Toast.makeText(
                        requireContext(),
                        "${audioTracks[which]} Selected",
                        Toast.LENGTH_SHORT
                    ).show()
//                    trackSelector.setParameters(
//                        trackSelector.buildUponParameters()
//                            .setPreferredAudioLanguage(audioTracks[which])
//                    )
                    exoPlayer!!.trackSelectionParameters = exoPlayer!!.trackSelectionParameters
                        .buildUpon()
                        .setMaxVideoSizeSd()
                        .setPreferredAudioLanguage(audioTracks[which])
                        .build();
                    playClicked()
                    val editor  =requireActivity().getSharedPreferences("AUDIO_TRACK",MODE_PRIVATE).edit()
                    editor.putInt("selectedTrack",which)
                    editor.apply()
                    dialog.dismiss()
                }
//                 .setItems(tempTrack){_,pos ->
//                     //track selection
//                     Toast.makeText(requireContext(), "${audioTracks[pos]} Selected", Toast.LENGTH_SHORT).show()
//                     trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTracks[pos]))
//                 }
//                 })
                .show()
//               playClicked()
        }
        return binding.root
    }

    private fun setVisibility() {
        lifecycleScope.launch {
            while (true) {
                delay(100)
                if (binding.videoView.isControllerFullyVisible)
                    changeVisibility(View.VISIBLE)
                else
                    changeVisibility(View.INVISIBLE)
            }
        }
    }

    private fun changeVisibility(visibility: Int) {
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

        if (isLocked)
            binding.lock.visibility = View.VISIBLE
        else
            binding.lock.visibility = visibility
    }

    private fun lockPlayer() {
        if (!isLocked) {
            isLocked = true
            binding.videoView.hideController()
            binding.videoView.useController = false
            binding.lock.setImageResource(R.drawable.ic_lock)
            changeVisibility(View.INVISIBLE)
            binding.lock.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background
                )
            )
        } else {
            isLocked = false
            binding.videoView.showController()
            binding.videoView.useController = true
            binding.lock.setImageResource(R.drawable.ic_lock_open)
            changeVisibility(View.VISIBLE)
            binding.lock.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.transparent
                )
            )
        }

    }

    private fun playInFullScreen(isEnable: Boolean) {
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

    private fun previousClicked() {
        exoPlayer!!.stop()
        exoPlayer!!.release()
        if (position == 0)
            position = videoList.size - 1
        else
            position--
        createPlayer(position)
        exoPlayer!!.play()
    }

    private fun nextClicked() {
        exoPlayer!!.stop()
        exoPlayer!!.release()
        if (position == videoList.size - 1)
            position = 0
        else
            position++

        createPlayer(position)
        exoPlayer!!.play()
    }

    private fun createPlayer(pos: Int) {
        binding.videoName.text = videoList[position].name
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        val mediaItem =
            MediaItem.fromUri(videoList[pos].path)
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.prepare()
        //  exoPlayer!!.play()
        binding.videoView.player = exoPlayer

        trackSelector = DefaultTrackSelector(requireContext())

        if (videoList[position].height < videoList[position].width) {//for landscape
            (activity as AppCompatActivity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else { //for portrait
            (activity as AppCompatActivity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        exoPlayer!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    nextClicked()
                }
            }
        })
    }

    private fun playClicked() {
        if (exoPlayer!!.isPlaying) {
            exoPlayer!!.pause()
        } else {
            exoPlayer!!.play()
        }

//        if (exoPlayer != null) {
//            exoPlayer!!.playWhenReady = true
//            exoPlayer!!.playbackState
//        }else {
//            binding.videoName.text = videoList[position].name
//            exoPlayer = ExoPlayer.Builder(requireContext()).build()
//            val mediaItem =
//                MediaItem.fromUri(videoList[pos].path)
//            exoPlayer!!.setMediaItem(mediaItem)
//            exoPlayer!!.prepare()
//            exoPlayer!!.play()
//        }
        //trackSelector = DefaultTrackSelector(requireContext())

//        if (videoList[position].height < videoList[position].width) {//for landscape
//            (activity as AppCompatActivity).requestedOrientation =
//                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//
//        } else {//for portrait
//            (activity as AppCompatActivity).requestedOrientation =
//                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        }
//        exoPlayer!!.addListener(object : Player.Listener {
//            override fun onPlaybackStateChanged(playbackState: Int) {
//                super.onPlaybackStateChanged(playbackState)
//                if (playbackState == Player.STATE_ENDED) {
//                    nextClicked()
//                }
//            }
//        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun muteClicked() {
        //service.muteClicked();
        if (isMute == 1) {
            audioManager.adjustVolume(
                AudioManager.ADJUST_MUTE,
                AudioManager.FLAG_PLAY_SOUND
            )
            binding.exoMute.setImageResource(R.drawable.ic_volume_off_24)
            isMute = 0
        } else {
            audioManager.adjustVolume(
                AudioManager.ADJUST_UNMUTE,
                AudioManager.FLAG_PLAY_SOUND
            )
            binding.exoMute.setImageResource(R.drawable.ic_volume_up_24)
            isMute = 1
        }
    }

    override fun onPause() {
        super.onPause()
        //for pause video
        exoPlayer!!.playWhenReady = false
        exoPlayer!!.playbackState
    }

    override fun onResume() {
        super.onResume()
        //for resume video
        if (exoPlayer != null) {
            exoPlayer!!.playWhenReady = true
            exoPlayer!!.playbackState
        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer!!.stop()
        exoPlayer!!.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar!!.show()
        exoPlayer!!.stop()
        exoPlayer!!.release()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = (activity as AppCompatActivity).window.insetsController
            if (controller != null) {
                if (controller.systemBarsBehavior == 0) {
                    controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
                } else {
                    controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
                }
            }
        } else {
//                (activity as AppCompatActivity).window.setFlags(
//                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            val attrs = (activity as AppCompatActivity).window.attributes
            attrs.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            (activity as AppCompatActivity).window.attributes = attrs
        }

//        if (!isRotate) {
//            (activity as AppCompatActivity).requestedOrientation =
//                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        } else {
//            (activity as AppCompatActivity).requestedOrientation =
//                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        }
    }

    private fun onTouchEvent(event: MotionEvent?): Boolean {
//        when(event?.action){
//            MotionEvent.ACTION_DOWN->{
//                Log.d("TOUCH", "onTouchEvent: DOWN")
//
//            }
//            MotionEvent.ACTION_UP->{
//                Log.d("TOUCH", "onTouchEvent: UP")
//            }
//        }
//        return true
        return if (gestureDetector.onTouchEvent(event))
            true
        else
            requireActivity().onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent?): Boolean {
//        Toast.makeText(requireContext(), "on down", Toast.LENGTH_SHORT).show()
        //  onBackPressed()
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
//        Toast.makeText(requireContext(), "on show press", Toast.LENGTH_SHORT).show()
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
//        Toast.makeText(requireContext(), "on single tap Up", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d("VOLUME", "on scroll $e1 $e2 and $distanceX and $distanceY")
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
//        Toast.makeText(requireContext(), "onLong press", Toast.LENGTH_SHORT).show()
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        try {
            val diffY = e2!!.y - e1!!.y
            val diffX = e2.x - e1.x

            if (Math.abs(diffX) < Math.abs(diffY)) {
                if (diffY > 0) {
                    Log.d("VOLUME", "on scroll $e1 $e2 and $velocityX and $velocityY")

//                    binding.seekBarVolume.setOnSeekBarChangeListener(object :
//                        SeekBar.OnSeekBarChangeListener {
//                        override fun onProgressChanged(
//                            seekBar: SeekBar?,
//                            newVolume: Int,
//                            fromUser: Boolean
//                        ) {
//                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
//                        }
//
//                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                        }
//
//                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                        }
//
//                    })
                } else {

                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

}