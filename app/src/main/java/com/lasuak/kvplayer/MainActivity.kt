package com.lasuak.kvplayer

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.lasuak.kvplayer.databinding.ActivityMainBinding
import com.lasuak.kvplayer.fragments.VideoFragment.Companion.videoList
import com.lasuak.kvplayer.model.Video
import java.io.File
import kotlin.math.log

class MainActivity : AppCompatActivity(){
    private lateinit var binding:ActivityMainBinding
    private lateinit var navController: NavController

    companion object{
        var uri:Uri?=null
        var video:Video?=null
        var themeSelectedId = R.id.lightTheme
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val pref = getSharedPreferences("THEME", MODE_PRIVATE)
        val themeID = pref.getInt("theme", themeSelectedId)
        when(themeID){
            R.id.nightTheme ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            R.id.lightTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

       //set navigation graph
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment

        if (intent?.action == Intent.ACTION_SEND) {
            if ("video/mp4" == intent.type || "video/mkv" == intent.type || "video/x-matroska"== intent.type ) {
                uri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri?
                Log.d("TAG", "onCreate: $uri")

            }
        }
        if(intent.data?.scheme.contentEquals("content")){
            val cursor = contentResolver.query(intent.data!!, arrayOf(MediaStore.Video.Media.DATA),null,null,null)
            cursor?.let{
                it.moveToFirst()
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val file =File(path)
                video = Video(file.name,file.path,0,0,0.0,"","","",0,0)
                Log.d("TAG", "onCreate: ${video!!.name}")
            }
            cursor?.close()
        }
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)//,appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        //Pass argument appBarConfiguration in navigateUp() method
        // for hamburger icon respond to click events
        //navConfiguration
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}