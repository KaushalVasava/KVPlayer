package com.lasuak.kvplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.R
import androidx.navigation.ui.setupActionBarWithNavController
import com.lasuak.kvplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){
    private lateinit var binding:ActivityMainBinding
    private lateinit var navController: NavController

    companion object{
        var themeSelectedId = com.lasuak.kvplayer.R.id.lightTheme
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
//            true
//        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars =
//            true
        //set navigation graph
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment

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