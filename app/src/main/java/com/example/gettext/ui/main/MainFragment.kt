package com.example.gettext.ui.main

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.gettext.MainActivity
import com.example.gettext.R
import kotlinx.android.synthetic.main.main_fragment.*
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
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
                if(bitmap.width!=300 && bitmap.height!=300){
                /*if(bitmap!=null){
                file.delete()
                    }*/

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
                var grayscaled = toGrayscale(cropedImageFinal)
                layout_display_image_camera.setImageBitmap(grayscaled)


                    /*TODO:
                    * RIGHT HERE
                    *
                     */

                // Toast.makeText(this.context, "${activity.model}", Toast.LENGTH_SHORT).show() // works
                normalize(grayscaled) // works

                var normalized = normalize(grayscaled)
                    Log.d("PREPARATION", "normalized: $normalized")
                var prediction = predict(activity.model)
                    Log.d("PREDICTOIN", "prediction: $prediction")
                Toast.makeText(this.context,prediction,Toast.LENGTH_LONG).show()

            }else{
                    layout_display_image_camera.setImageBitmap(bitmap)
                    /*TODO:
                * RIGHT HERE
                *
                 */
                    /*
                    var prediction = predict(activity.model, normalize(toGrayscale(bitmap)))
                    Toast.makeText(this.context,prediction,Toast.LENGTH_LONG).show()
                    */

                }

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



fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
    val width = 300
    val height = 300

    val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmpGrayscale)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.setSaturation(0f)
    val f = ColorMatrixColorFilter(cm)
    paint.colorFilter = f
    c.drawBitmap(bmpOriginal, 0f, 0f, paint)
    Log.d("PREPARATION", "grayscaled: $bmpGrayscale")
    return bmpGrayscale
}

fun normalize(grayscale: Bitmap) {
    val ba = convertToByteArray(grayscale)
    for(i in ba.indices) {
        Log.d("BA", "BA ${ba[i]} ")
    }
    Log.d("ROWBYTES", "${grayscale.rowBytes} ")

    // return inputTensor
}

fun predict(model: Module): String{ //, normalized: Tensor) : String{
    val fontNames = arrayOf("Lato-Regular", "LiberationSans-Regular", "LiberationSerif-Regular")
//    val output = model.forward(IValue.from(normalized)).toTensor()
//    val scores: FloatArray = output.dataAsFloatArray
//    var largest = scores[0]
//    var largestIdx = 0
//    for (i in scores.indices) {
//        if (largest < scores[i])
//            largest = scores[i]
//            largestIdx = i
//    }
    return fontNames[2]
}

fun convertToByteArray(bitmap: Bitmap): ByteArray {
    //minimum number of bytes that can be used to store this bitmap's pixels
    val size = bitmap.byteCount

    //allocate new instances which will hold bitmap
    val buffer = ByteBuffer.allocate(size)
    val bytes = ByteArray(size)

    //copy the bitmap's pixels into the specified buffer
    bitmap.copyPixelsToBuffer(buffer)

    //rewinds buffer (buffer position is set to zero and the mark is discarded)
    buffer.rewind()

    //transfer bytes from buffer into the given destination array
    buffer.get(bytes)

    //return bitmap's pixels
    return bytes
}

