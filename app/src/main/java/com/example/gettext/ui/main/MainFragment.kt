package com.example.gettext.ui.main

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.text.BoringLayout
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import camera.Camera
import com.example.gettext.MainActivity
import com.example.gettext.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.camera.*
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.main_fragment.view.*
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class MainFragment : Fragment() {


    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var containerLayout: ConstraintLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {




          val root =inflater.inflate(R.layout.main_fragment, container, false)
        containerLayout = root.findViewById(R.id.container_main_fragment)
        if(containerLayout==null){
            Log.d("Container:","null")
        }else{
            Log.d("Container:","NotNull")
        }
        return root

    }

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
        if (image!=null) {
            val file= File(image)
            val bitmap = BitmapFactory.decodeFile(image)

            val rotatedBitmap = bitmap.rotate(90F)
            val hpr =hpr(arH,arW,rotatedBitmap.height,rotatedBitmap.width)

            Log.d("BitmapImageResolution: ","${rotatedBitmap.height}x${rotatedBitmap.width}")
            val aspecRatio = aspectRatio(height,width)
            Log.d("aspectRatioFragment",aspecRatio)

            val newHeight = arH * (hpr-1)
            val newWidth = arW * (hpr-1)
            Log.d("NewImageResolution: ","${newHeight}x${newWidth}")
            val cropedImage = cropImage(rotatedBitmap,newWidth,newHeight)

            val resizedbitmap =Bitmap.createScaledBitmap(cropedImage,width,height,false)
           val cropedImage2 = cropImage(resizedbitmap,300,300)
            layout_display_image_camera.setImageBitmap(cropedImage2)
            Toast.makeText(this.context,image,Toast.LENGTH_LONG).show()




        }
        else{
            Toast.makeText(this.context,"There is no image!",Toast.LENGTH_LONG).show()
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





