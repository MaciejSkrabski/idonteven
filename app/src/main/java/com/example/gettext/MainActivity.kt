package com.example.gettext

//ADDED
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.main_activity.*
import org.pytorch.*
import org.pytorch.torchvision.*
import org.pytorch.Module
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var model: Module
    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    var navController: NavController?=null
    private var photoPath: String ?= null
    override fun onCreate(savedInstanceState: Bundle?) {

        val extras = intent.extras
        if(extras!=null) {
             photoPath = extras!!.getString("imagePath")


        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        //ADDED
        model = Module.load(assetFilePath(application, "mobilejit.pt"))
        Log.d("PYTORCH", "onCreate: $model")



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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    pickImageFromGallery();
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }


    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    fun returnPhotoPath(): String?{
            return photoPath
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            val path = convertMediaUriToPath(data?.data)
            Log.d("DataFromGallery",path)


            photoPath = path!!

        }
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

            R.id.info -> {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.info_layout,null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(dialogView)
                val mAlertDialog = mBuilder.show()

            }
        }
    return true
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    fun convertMediaUriToPath(uri: Uri?): String? {
        val proj = arrayOf<String>(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, proj, null, null, null)
        val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path: String = cursor.getString(column_index)
        cursor.close()
        return path
    }

    fun assetFilePath(context: Context, asset: String): String {
        val file = File(context.filesDir, asset)

        try {
            val inpStream: InputStream = context.assets.open(asset)
            try {
                val outStream = FileOutputStream(file, false)
                val buffer = ByteArray(4 * 1024)
                var read: Int

                while (true) {
                    read = inpStream.read(buffer)
                    if (read == -1) {
                        break
                    }
                    outStream.write(buffer, 0, read)
                }
                outStream.flush()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
