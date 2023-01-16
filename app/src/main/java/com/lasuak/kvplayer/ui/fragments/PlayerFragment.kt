package com.lasuak.kvplayer.ui.fragments

import android.app.Service
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.databinding.FragmentPlayerBinding
import com.lasuak.kvplayer.model.Video
import com.lasuak.kvplayer.ui.viewmodel.PlayerViewModel
import com.lasuak.kvplayer.util.AppConstant
import com.lasuak.kvplayer.util.AppUtil
import com.lasuak.kvplayer.util.VideoUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PlayerFragment : Fragment(R.layout.fragment_player) {
    private lateinit var binding: FragmentPlayerBinding
    private val args: PlayerFragmentArgs by navArgs()
    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var audioManager: AudioManager
    private val videoList = mutableListOf<Video>()
    private var position: Int = -1
    private var isFullscreenEnable = false
    private var isLocked = false
    private var brightness = -1.0f
    private var isMuted = false
    private var isRotated = false
    private var isSubtitleEnable = true
    private var exoPlayer: ExoPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioManager = requireActivity().getSystemService(Service.AUDIO_SERVICE) as AudioManager
        binding = FragmentPlayerBinding.bind(view)
        (activity as AppCompatActivity).supportActionBar?.hide()
        position = args.position
        if (position != -1) {
            setUpResultListener()
        } else {
            videoList.clear()
            videoList.addAll(VideoUtil.getVideosByFolder(requireContext(), args.folderId))
            createPlayer()
        }
        addClickListeners()
        addSeekbarChangeListeners()
        setVisibility()
    }

    private fun createPlayer() {
        val video = if (position != -1) {
            videoList[position]
        } else {
            position = VideoUtil.findVideoPosition(args.video.id, videoList)
            args.video
        }
        viewModel.trackSelector = DefaultTrackSelector(requireContext())
        exoPlayer = ExoPlayer.Builder(requireContext())
            .setTrackSelector(viewModel.trackSelector!!)
            .build()
        binding.btnBackName.text = video.name
        val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppUtil.getRealPath(Uri.parse(video.path), requireContext()) ?: video.path
        } else {
            video.path
        }
        val mediaItem = MediaItem.fromUri(path)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        binding.videoView.player = exoPlayer

        (activity as AppCompatActivity).requestedOrientation =
            if (video.height < video.width) {
                //for landscape
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                //for portrait
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    nextClicked()
                }
            }
        })
    }

    private fun addClickListeners() {
        binding.btnBackName.setOnClickListener {
            findNavController().popBackStack(R.id.videoFragment, true)
        }
        binding.btnPrev.setOnClickListener {
            previousClicked()
        }
        binding.btnNext.setOnClickListener {
            nextClicked()
        }
        binding.exoFullscreen.setOnClickListener {
            if (isFullscreenEnable) {
                playInFullScreen(false)
            } else {
                playInFullScreen(true)
            }
        }
        binding.btnLock.setOnClickListener {
            setLockPlayer()
        }
        binding.btnScreenRotation.setOnClickListener {
            setRotation()
        }
        binding.btnMute.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isMuted = viewModel.muteClicked(isMuted, audioManager)
                if (isMuted) {
                    binding.btnMute.setImageResource(R.drawable.ic_volume_off_24)
                } else {
                    binding.btnMute.setImageResource(R.drawable.ic_volume_up_24)
                }
            }
        }

        binding.btnAudioTrack.setOnClickListener {
            exoPlayer?.let {
                viewModel.setAudioTrack(requireContext(), it)
            }
        }

        binding.exoSubtitle.setOnClickListener {
            exoPlayer?.let {
                isSubtitleEnable = viewModel.setSubtitle(requireContext(), it)
            }
        }
    }

    private fun addSeekbarChangeListeners() {
        binding.seekBarBrightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                val layout = requireActivity().window.attributes
                layout.screenBrightness = progress / 100.toFloat()
                binding.txtBrightness.text = progress.toString()
                requireActivity().window.attributes = layout
                brightness = progress / 100.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                /* no-op */
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                /* no-op */
            }
        })

        binding.seekBarVolume.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    newVolume: Int,
                    fromUser: Boolean
                ) {
                    val volume = newVolume / 10
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                    binding.txtVolume.text = newVolume.toString()

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    /* no-op */
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    /* no-op */
                }
            })
    }

    private fun setLockPlayer() {
        if (!isLocked) {
            isLocked = true
            binding.videoView.hideController()
            binding.videoView.useController = false
            binding.btnLock.setImageResource(R.drawable.ic_lock)
            changeVisibility(View.INVISIBLE)
            binding.btnLock.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background
                )
            )
        } else {
            isLocked = false
            binding.videoView.showController()
            binding.videoView.useController = true
            binding.btnLock.setImageResource(R.drawable.ic_lock_open)
            changeVisibility(View.VISIBLE)
            binding.btnLock.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
        }
    }

    private fun changeVisibility(visibility: Int) {
        binding.btnBackName.visibility = visibility
        binding.exoFullscreen.visibility = visibility
        binding.btnScreenRotation.visibility = visibility
        binding.btnPrev.visibility = visibility
        binding.btnNext.visibility = visibility
        binding.btnMute.visibility = visibility
        binding.seekBarBrightness.visibility = visibility
        binding.seekBarVolume.visibility = visibility
        binding.txtBrightness.visibility = visibility
        binding.txtVolume.visibility = visibility
        binding.btnAudioTrack.visibility = visibility
        binding.exoSubtitle.visibility = visibility

        if (isLocked)
            binding.btnLock.visibility = View.VISIBLE
        else
            binding.btnLock.visibility = visibility
    }

    private fun previousClicked() {
        exoPlayer?.stop()
        exoPlayer?.release()
        if (position == 0)
            position = videoList.size - 1
        else
            position--
        createPlayer()
        exoPlayer?.play()
    }

    private fun nextClicked() {
        exoPlayer?.stop()
        exoPlayer?.release()
        if (position >= videoList.size - 1)
            position = 0
        else
            position++

        createPlayer()
        exoPlayer?.play()
    }

    private fun setUpResultListener() {
        setFragmentResultListener(VideoFragment.VIDEO_BUNDLE_KEY) { _, bundle ->
            val videos =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList(
                        VideoFragment.VIDEO_LIST_BUNDLE_KEY,
                        Video::class.java
                    )
                } else {
                    @Suppress("deprecation")
                    bundle.getParcelableArrayList(VideoFragment.VIDEO_LIST_BUNDLE_KEY)
                }.orEmpty()
            if (videoList.isNotEmpty()) {
                videoList.clear()
            }
            videoList.addAll(videos)
            createPlayer()
        }
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

    private fun setRotation() {
        (activity as AppCompatActivity).requestedOrientation =
            if (isRotated) {
                isRotated = false
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                isRotated = true
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
    }

    private fun playInFullScreen(isEnable: Boolean) {
        if (isEnable) {
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            exoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.exoFullscreen.setImageResource(R.drawable.ic_fullscreen_exit)
        } else {
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            exoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.exoFullscreen.setImageResource(R.drawable.ic_fullscreen)
        }
        isFullscreenEnable = isEnable
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putInt(VIDEO_POSITION_BUNDLE_KEY, position)
            putBoolean(FULLSCREEN_BUNDLE_KEY, isFullscreenEnable)
            putBoolean(ORIENTATION_BUNDLE_KEY, isRotated)
            putBoolean(VIDEO_MUTE_BUNDLE_KEY, isMuted)
            putBoolean(LOCK_BUNDLE_KEY, isLocked)
            putBoolean(SUBTITLE_BUNDLE_KEY, isSubtitleEnable)
            putFloat(BRIGHTNESS_LEVEL_BUNDLE_KEY, brightness)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(
                VIDEO_POSITION_BUNDLE_KEY
            )
            isFullscreenEnable = savedInstanceState.getBoolean(
                FULLSCREEN_BUNDLE_KEY
            )
            isRotated = savedInstanceState.getBoolean(
                ORIENTATION_BUNDLE_KEY
            )
            isMuted = savedInstanceState.getBoolean(
                VIDEO_POSITION_BUNDLE_KEY
            )
            isSubtitleEnable = savedInstanceState.getBoolean(
                SUBTITLE_BUNDLE_KEY
            )
            brightness = savedInstanceState.getFloat(
                BRIGHTNESS_LEVEL_BUNDLE_KEY
            )
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        )
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun showSystemUI() {
        val insetsController = WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        )
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onPause() {
        super.onPause()
        //for pause video
        exoPlayer?.playWhenReady = false
        exoPlayer?.playbackState
        val layout = requireActivity().window.attributes
        layout.screenBrightness = -1.0f
        requireActivity().window.attributes = layout
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        val layout = requireActivity().window.attributes
        if (brightness == -1.0f) {
            brightness = layout.screenBrightness
        } else {
            layout.screenBrightness = brightness
        }
        val tempLight: Int = (100 * (brightness)).roundToInt()
        requireActivity().window.attributes = layout
        binding.seekBarBrightness.progress = tempLight
        binding.txtBrightness.text = tempLight.toString()
        exoPlayer?.let {
            it.playWhenReady = true
            it.playbackState
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showSystemUI()
        (activity as AppCompatActivity).requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_SENSOR
        requireActivity().getSharedPreferences(AppConstant.LAST_VIDEO_DATA, Context.MODE_PRIVATE)
            .edit()
            .apply {
                putLong(AppConstant.VIDEO_ID, videoList[position].id)
                putLong(AppConstant.FOLDER_ID, args.folderId)
                apply()
            }
        (activity as AppCompatActivity).supportActionBar!!.show()
        exoPlayer?.stop()
        exoPlayer?.release()
        brightness = -1.0f
    }

    companion object {
        private const val VIDEO_POSITION_BUNDLE_KEY = "video_position_bundle_key"
        private const val FULLSCREEN_BUNDLE_KEY = "full_screen_bundle_key"
        private const val ORIENTATION_BUNDLE_KEY = "orientation_bundle_key"
        private const val VIDEO_MUTE_BUNDLE_KEY = "video_mute_bundle_key"
        private const val LOCK_BUNDLE_KEY = "lock_bundle_key"
        private const val SUBTITLE_BUNDLE_KEY = "subtitle_bundle_key"
        private const val BRIGHTNESS_LEVEL_BUNDLE_KEY = "brightness_level_bundle_key"
    }
}