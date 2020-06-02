package com.example.gettext.ui.main

import android.R.attr.name
import android.R.attr.visibility
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import camera.Camera
import com.example.gettext.MainActivity
import com.example.gettext.R
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class MainFragment : Fragment() {


    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel


lateinit var root: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {




           root =inflater.inflate(R.layout.main_fragment, container, false)
        return root

    }

    @SuppressLint("WrongThread")
    override fun onResume() {
        val activity: MainActivity? = activity as MainActivity?
       // val metrics = DisplayMetrics().also { containerLayout.display.getRealMetrics(it) }

      // val width = root.layout_display_image_camera.width
       // Log.d("WidthOnResume",width.toString())
        val width =getActivity()!!.windowManager.defaultDisplay.width
        val height = getActivity()!!.windowManager.defaultDisplay.height
        //Greatest common divisor
        val gcd = gcd(height,width)
        //Aspect ratio
        val arH = height/gcd
        val arW = width/gcd

        Log.d("AspectRatioScreen","${arH}:${arW}")
        Log.d("Displayresolution : ","${height}x${width}")
        val image =activity!!.returnPhotoPath()
        val filesDir: File = this.context!!.externalMediaDirs.firstOrNull().let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        val imageFile = File(filesDir, SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS", Locale.ROOT
        ).format(System.currentTimeMillis()) + ".jpg")

        if (image!=null) {
            val file= File(image)
            if (file.exists()) {
                var overlap:TextView = root.findViewById(R.id.ovelap)
                val layout_text : ConstraintLayout= root.findViewById(R.id.layout_text)
                var main_text: TextView = root.findViewById(R.id.main_text)
                main_text.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."

                overlap.isVisible = true
                layout_text.isVisible= true
                var fOut= FileOutputStream(imageFile)
                //imageFile is a file of the final 300x300 image, to get path- imageFile.absolutePath like below

                Log.d("ImageFileDir",imageFile.absolutePath)

                val bitmap = BitmapFactory.decodeFile(image)
                if(bitmap!=null){
                file.delete()
                    }

                val rotatedBitmap = bitmap.rotate(90F)
                val hpr = hpr(arH, arW, rotatedBitmap.height, rotatedBitmap.width)

                Log.d("BitmapImageResolution: ", "${rotatedBitmap.height}x${rotatedBitmap.width}")
                val aspecRatio = aspectRatio(height, width)
                Log.d("aspectRatioFragment", aspecRatio)

                val newHeight = arH * (hpr - 1)
                val newWidth = arW * (hpr - 1)
                Log.d("NewImageResolution: ", "${newHeight}x${newWidth}")
                val cropedImage = cropImage(rotatedBitmap, newWidth, newHeight)

                val resizedbitmap = Bitmap.createScaledBitmap(cropedImage, width, height, false)
                val cropedImageFinal = cropImage(resizedbitmap, 300, 300)
                cropedImageFinal.compress(Bitmap.CompressFormat.JPEG,100,fOut)
                Log.d("CropedImageFinal","${cropedImageFinal.height}x${cropedImageFinal.width}")
                fOut.flush()
                fOut.close()

                layout_display_image_camera.setImageBitmap(cropedImageFinal)
                //   Toast.makeText(this.context,image,Toast.LENGTH_LONG).show()


            }

        }
        else{

        }
        super.onResume()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

    }
    fun gcd(height: Int, width: Int): Int {
        var i = 1
        var gcd = 1
        while (i <= height && i <= width) {
            if(height % i == 0 && width % i == 0 ){
                gcd = i
            }
            ++i
        }
        return gcd
    }
    // Function return highes possible mutlipler in certian resolution
    fun hpr(arH:Int,arW:Int,height: Int,width: Int): Int{
        var i=1
        while(i * arH <= height && i * arW <= width){
            ++i
        }
        return i
    }
    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
        }
    private fun cropImage(bitmap: Bitmap,widths: Int,heights: Int): Bitmap {


        val heightReal = bitmap.height
        val widthReal = bitmap.width

        val widthFinal = widthReal/2 - widths/2
        val heightFinal = heightReal/2 - heights/2

        val bitmapFinal = Bitmap.createBitmap(
            bitmap,
            widthFinal, heightFinal, widths,heights
        )

        return bitmapFinal
    }
    private fun aspectRatio(width: Int, height: Int): String {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - 1.3) <= abs(previewRatio - 1.7)) {
            return "4:3"
        }
        return "16:9"
    }






