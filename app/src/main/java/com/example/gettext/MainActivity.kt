package com.example.gettext

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.example.gettext.ui.main.MainFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_fragment.*

class MainActivity : AppCompatActivity() {

    var navController: NavController?=null
    private var photoPath: String ?= null
    override fun onCreate(savedInstanceState: Bundle?) {

        val extras = intent.extras
        if(extras!=null) {
             photoPath = extras!!.getString("imagePath")


        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)
        navController= Navigation.findNavController(findViewById(R.id.nav_host_fragment))
        val preferences = preferences(this)
        val isDark = preferences.getMode()
        if(isDark) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }else{

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add)
        val fabCamera: FloatingActionButton = findViewById(R.id.fab_camera)
        val fabGallery: FloatingActionButton = findViewById(R.id.fab_gallery)
        val fabInfoCamera: TextView = findViewById(R.id.info_fab_photo)
        val fabInfoGallery: TextView = findViewById(R.id.info_fab_gallery)
        val fabOpenAnim = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabCloseAnim = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        val fabRotateOpen = AnimationUtils.loadAnimation(this,R.anim.fab_rotate_open)
        val fabRotateClose = AnimationUtils.loadAnimation(this,R.anim.fab_rotate_close)
        var isOpen = false
         fabAdd.setOnClickListener{
             if(isOpen){
                 fabAdd.startAnimation(fabRotateClose)
                 fabCamera.startAnimation(fabCloseAnim)
                 fabGallery.startAnimation(fabCloseAnim)
                 fabInfoCamera.isVisible = false
                 fabInfoGallery.isVisible= false
                 isOpen=false

             }
             else{
                 fabAdd.startAnimation(fabRotateOpen)
                 fabCamera.startAnimation(fabOpenAnim)
                 fabGallery.startAnimation(fabOpenAnim)
                 fabInfoCamera.isVisible = true
                 fabInfoGallery.isVisible= true
                 isOpen=true
             }

         }
        fabCamera.setOnClickListener{
            fabAdd.startAnimation(fabRotateClose)
            fabCamera.startAnimation(fabCloseAnim)
            fabGallery.startAnimation(fabCloseAnim)
            fabInfoCamera.isVisible = false
            fabInfoGallery.isVisible= false
            isOpen=false
            navController!!.navigate(R.id.action_navigation_mainFragment_to_navigation_camera)

        }
        fabGallery.setOnClickListener{
            fabAdd.startAnimation(fabRotateClose)
            fabCamera.startAnimation(fabCloseAnim)
            fabGallery.startAnimation(fabCloseAnim)
            fabInfoCamera.isVisible = false
            fabInfoGallery.isVisible= false
            isOpen=false

        }
    }
    fun returnPhotoPath(): String?{
            return photoPath
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_toolbar,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val preferences = preferences(this)
        val isDark = preferences.getMode()

        when(item.itemId) {

            R.id.color_mode ->if(isDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
             //   item.icon =getDrawable(R.drawable.ic_lm)
                preferences.setMode(false)




            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
               // item.icon = getDrawable(R.drawable.ic_nm)
                preferences.setMode(true)
            }

            R.id.info -> TODO()
        }
    return true
    }
}
