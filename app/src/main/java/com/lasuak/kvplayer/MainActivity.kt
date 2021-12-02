package com.lasuak.kvplayer

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.lasuak.kvplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){
    private lateinit var binding:ActivityMainBinding
    private lateinit var navController: NavController

    companion object{
        var uri:Uri?=null
        var themeSelectedId = com.lasuak.kvplayer.R.id.lightTheme
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val pref = getSharedPreferences("THEME", MODE_PRIVATE)
        val themeID = pref.getInt("theme", themeSelectedId)
        when(themeID){
            com.lasuak.kvplayer.R.id.nightTheme ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            com.lasuak.kvplayer.R.id.lightTheme -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        //editor.putInt("colorNo", whichTheme)

//        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
//            true
//        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars =
//            true
        //set navigation graph
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment

//        val inflater = navHostFragment.navController.navInflater
//        val graph = inflater.inflate(R.navigation.main_nav_graph)
        //shared text received from other apps
        if (intent?.action == Intent.ACTION_SEND) {
            if ("video/mp4" == intent.type || "video/mkv" == intent.type || "video/x-matroska"== intent.type ) {
                uri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri?
                //  getStringExtra(Intent.EXTRA_STREAM)
                Log.d("TAG", "onCreate: $uri")

            }
        }
//        if (uri==null)
//            graph.startDestination = R.id.folderFragment
//        else
//            graph.startDestination = R.id.playerFragment

//        navHostFragment.navController.graph = graph
        //set controller for navigation between fragments
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