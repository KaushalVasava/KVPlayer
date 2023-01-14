package com.lasuak.kvplayer.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.lasuak.kvplayer.R
import com.lasuak.kvplayer.databinding.ActivityMainBinding
import com.lasuak.kvplayer.model.Video
import com.lasuak.kvplayer.util.AppConstant.CONTENT
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        var uri: Uri? = null
        var video: Video? = null
        private const val VIDEO_MP4 = "video/mp4"
        private const val VIDEO_MKV = "video/mkv"
        private const val VIDEO_X_MATROSKA = "video/x-matroska"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment

        if (intent?.action == Intent.ACTION_SEND) {
            if (VIDEO_MP4 == intent.type || VIDEO_MKV == intent.type || VIDEO_X_MATROSKA == intent.type) {
                uri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri?
            }
        }
        if (intent.data?.scheme.contentEquals(CONTENT)) {
            val cursor = contentResolver.query(
                intent.data!!,
                arrayOf(MediaStore.Video.Media.DATA),
                null,
                null,
                null
            )
            cursor?.let {
                it.moveToFirst()
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val file = File(path)
                video = Video(
                    file.name,
                    file.path,
                    0,
                    0,
                    0.0,
                    "",
                    "",
                    "",
                    0,
                    0
                )
            }
            cursor?.close()
        }
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}