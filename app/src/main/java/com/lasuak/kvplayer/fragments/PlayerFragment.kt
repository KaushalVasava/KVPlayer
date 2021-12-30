package com.lasuak.kvplayer.fragments

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context.MODE_PRIVATE
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.databinding.FragmentPlayerBinding
import com.lasuak.kvplayer.fragments.FolderFragment.Companion.globalList
import com.lasuak.kvplayer.fragments.VideoFragment.Companion.videoList
import com.lasuak.kvplayer.viewmodel.PlayerViewModel
import com.lasuak.kvplayer.viewmodel.PlayerViewModel.Companion.isFullscreen
import com.lasuak.kvplayer.viewmodel.PlayerViewModel.Companion.position
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG= "TAG"
class PlayerFragment : Fragment(R.layout.fragment_player) {
    private lateinit var binding: FragmentPlayerBinding
    private val args: PlayerFragmentArgs by navArgs()
    private lateinit var audioManager: AudioManager
    private lateinit var viewModel: PlayerViewModel
    private lateinit var gestureDetector: GestureDetector
    companion object{
        private var brightness = -1.0f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        audioManager = requireActivity().getSystemService(Service.AUDIO_SERVICE) as AudioManager

        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar!!.hide()

        position = args.position

        if(position == -1) {

            val youtubeLink = "http://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4"


                //            val youtubeLink =
//                args.videoName

//            object : YouTubeExtractor(this) {
//                fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, vMeta: VideoMeta?) {
//                    if (ytFiles != null) {
//                        val itag = 22
//                        val downloadUrl: String = ytFiles[itag].getUrl()
//                    }
//                }
//            }.extract(youtubeLink)
            val exoPlayer = ExoPlayer.Builder(requireContext()).build()
            val mediaItem =
                MediaItem.fromUri(youtubeLink)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            binding.videoView.player = exoPlayer

//            val exoplayer = ExoPlayer.Builder(requireContext())
//            exoplayer.setV
        }
        else {
            viewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
            if (args.videoName == "EXTERNAL")
                videoList = globalList
            viewModel.checkOrientation(requireActivity())
            viewModel.createPlayer(
                requireContext(),
                requireActivity(),
                binding,
                position
            )

//            gestureDetector = GestureDetector(requireContext(),object:
//                GestureDetector.SimpleOnGestureListener() {
//                override fun onDoubleTap(e: MotionEvent?): Boolean {
//                    Log.d("TAG", "onDoubleTap: ")
//                    return super.onDoubleTap(e)
//                }
//            })
//            binding.videoView.setOnTouchListener { v, event ->
//                onTouchEvent(event)
//                true
//            }

            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val layout = requireActivity().window.attributes
                    layout.screenBrightness = progress / 100.toFloat()
                    binding.lightText.text = progress.toString()
                    //1F
                    requireActivity().window.attributes = layout
                    //val prefLight = requireActivity().getSharedPreferences("BRIGHTNESS",MODE_PRIVATE).edit()
                    brightness = progress/100.toFloat()
//                    prefLight.putFloat("brightness", brightness)
//                    prefLight.apply()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })

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
                viewModel.previousClicked(requireContext(), requireActivity(), binding)
            }
            binding.exoNext.setOnClickListener {
                viewModel.nextClicked(requireContext(), requireActivity(), binding)
            }
            setVisibility()
            binding.exoFullscreen.setOnClickListener {
                if (isFullscreen) {
                    viewModel.playInFullScreen(false, binding)
                } else {
                    viewModel.playInFullScreen(true, binding)
                }
            }
            binding.lock.setOnClickListener {
                viewModel.lockPlayer(requireContext(), binding)
            }
            binding.exoScreenRotation.setOnClickListener {
                viewModel.setRotation(requireActivity())
            }

            binding.exoMute.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    viewModel.muteClicked(audioManager, binding)
                }
            }
            binding.exoAudioTrack.setOnClickListener {
                viewModel.setAudioTrack(requireContext())
            }

            binding.exoSubtitle.setOnClickListener {
                viewModel.setSubtitle(requireContext())
            }
        }
        return binding.root
    }

    private fun setVisibility() {
        lifecycleScope.launch {
            while (true) {
                delay(100)
                if (binding.videoView.isControllerFullyVisible)
                    viewModel.changeVisibility(View.VISIBLE, binding)
                else
                    viewModel.changeVisibility(View.INVISIBLE, binding)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //for pause video
        viewModel.exoPlayer!!.playWhenReady = false
        viewModel.exoPlayer!!.playbackState
        val layout = requireActivity().window.attributes
        layout.screenBrightness = -1.0f
        requireActivity().window.attributes = layout
        Log.d(TAG, "onPause: called $brightness")

        val prefLight = requireActivity().getSharedPreferences("BRIGHTNESS",MODE_PRIVATE).edit()
        prefLight.putFloat("brightness", brightness)
        prefLight.apply()
    }

    override fun onResume() {
        super.onResume()
        //for resume video
        val prefLight = requireActivity().getSharedPreferences("BRIGHTNESS", MODE_PRIVATE)
        val brightness1 = prefLight.getFloat("brightness",-1.0F)
        Log.d(TAG, "onResume:$brightness1 & $brightness ")
        val layout = requireActivity().window.attributes
        layout.screenBrightness = brightness1
        val tempLight:Int = (100*(brightness1)).roundToInt()
        Log.d(TAG, "onResume: $tempLight")
        requireActivity().window.attributes = layout
        binding.seekBar.progress = tempLight
        binding.lightText.text = tempLight.toString()

        if (viewModel.exoPlayer != null) {
            viewModel.exoPlayer!!.playWhenReady = true
            viewModel.exoPlayer!!.playbackState
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar!!.show()
        viewModel.exoPlayer!!.stop()
        viewModel.exoPlayer!!.release()
brightness=-1.0f
        //this is for orientation change when exit from player fragment
        viewModel.backToDefaultOrientation(requireActivity())
    }

//    private fun onTouchEvent(event: MotionEvent?): Boolean {
//        return if (gestureDetector.onTouchEvent(event))
//            true
//        else
//            requireActivity().onTouchEvent(event)
//    }

//    override fun onDown(e: MotionEvent?): Boolean {
////        Toast.makeText(requireContext(), "on down", Toast.LENGTH_SHORT).show()
//
//        return true
//    }
//
//    override fun onShowPress(e: MotionEvent?) {
////        Toast.makeText(requireContext(), "on show press", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onSingleTapUp(e: MotionEvent?): Boolean {
////        Toast.makeText(requireContext(), "on single tap Up", Toast.LENGTH_SHORT).show()
//        //   changeVisibility(View.VISIBLE)
//        return true
//    }
//
//    override fun onScroll(
//        e1: MotionEvent?,
//        e2: MotionEvent?,
//        distanceX: Float,
//        distanceY: Float
//    ): Boolean {
//        //Log.d("VOLUME", "on scroll $e1 $e2 and $distanceX and $distanceY")
//        return true
//    }
//
//    override fun onLongPress(e: MotionEvent?) {
////        Toast.makeText(requireContext(), "onLong press", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onFling(
//        e1: MotionEvent?,
//        e2: MotionEvent?,
//        velocityX: Float,
//        velocityY: Float
//    ): Boolean {
//        try {
//            val diffY = e2!!.y - e1!!.y
//            val diffX = e2.x - e1.x
//
//            if (abs(diffX) < abs(diffY)) {
//                if (diffY > 0) {
//                    Log.d("VOLUME", "on DOWN ${e2.y - e1.y} y1${e1.y} and y2${e2.y} ")
//                    val diff = abs(e2.y - e1.y)
//                    val layout = requireActivity().window.attributes
//                    layout.screenBrightness = diff / 100.toFloat()
//                    binding.lightText.text = diff.toString()
//                    //1F
//                    requireActivity().window.attributes = layout
//                    binding.seekBar.progress = (diff * 10).toInt()
//
//                } else {
//                    Log.d("VOLUME", "on UP ${e2.y - e1.y} y1${e1.y} and y2${e2.y} ")
//                    val diff = abs(e2.y - e1.y)
//                    val layout = requireActivity().window.attributes
//                    layout.screenBrightness = diff / 100.toFloat()
//                    binding.lightText.text = diff.toString()
//                    //1F
//                    requireActivity().window.attributes = layout
//                    binding.seekBar.progress = (diff * 10).toInt()
//
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return true
//    }

}