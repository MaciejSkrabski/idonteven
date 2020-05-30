package camera


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Log.d
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import com.example.gettext.MainActivity
import com.example.gettext.R
import kotlinx.android.synthetic.main.camera.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService

class Camera:AppCompatActivity() {
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var navController: NavController?=null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cropedImage: ByteArray
    private var context = this
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.camera)
        val fabOpenAnim = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabCloseAnim = AnimationUtils.loadAnimation(this, R.anim.fab_close)


        btn_take_photo.setOnClickListener{
            takePhoto()
            btn_accept.startAnimation(fabOpenAnim)
            btn_decline.startAnimation(fabOpenAnim)
            btn_accept.isVisible = true
            btn_decline.isVisible= true
            CameraX.unbind(preview)



        }

        btn_accept.setOnClickListener{
           // navController!!.navigate(R.id.action_navigation_camera_to_navigation_mainFragment)

            val intent = Intent(context,MainActivity::class.java)
           val viewScreenShot  =loadBitmapFromView(layout_camera)
           val viewSsCroped= cropImage(viewScreenShot!!,layout_camera,layout_camera_small)


            intent.putExtra("imageCapture",cropedImage)
           // intent.putExtra("imageCapture",viewSsCroped)
            startActivity(intent)

        }
        btn_decline.setOnClickListener{
            btn_accept.startAnimation(fabCloseAnim)
            btn_decline.startAnimation(fabCloseAnim)
            btn_accept.isVisible = false
            btn_decline.isVisible= false
            startCamera()

        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        /*outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()*/
    }
    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
           // val aspectRatio = Rational(layout_camera.width,layout_camera.height)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenAspectRatio = Rational(displayMetrics.widthPixels, displayMetrics.heightPixels)
            val screenSize = Size(displayMetrics.widthPixels,displayMetrics.heightPixels)
            preview = Preview.Builder()
               // .setTargetResolution(screenSize)
                //.setTargetRotation(layout_camera.display.rotation)
                //.setTargetAspectRatio(screenAspectRatio.toInt())
              //  .setTargetAspectRatio(screenAspectRatio.toInt())
                //.setTargetRotation(Surface.ROTATION_90)
                .build()
                //Image Capture
            imageCapture = ImageCapture.Builder()
               // .setTargetResolution(screenSize)
                //.setTargetRotation(layout_camera.display.rotation)
                //.setTargetAspectRatio(screenAspectRatio.toInt())
                .build()
            // Select back camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture)
                preview?.setSurfaceProvider(layout_camera.createSurfaceProvider())

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun takePhoto() {

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
/*        // Create timestamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.ROOT
            ).format(System.currentTimeMillis()) + ".jpg")
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()*/

        // Setup image capture listener which is triggered after photo has
        // been taken
/*        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    var photo= photoFile.absoluteFile as Image
                    var bitmapphoto = photo.toBitmap()
                    bitmapphoto=  cropImage(bitmapphoto,layout_camera,layout_camera_small) as Bitmap
                    bitmapphoto as File

                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })*/

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object: ImageCapture
        .OnImageCapturedCallback() {
            @SuppressLint("UnsafeExperimentalUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
               /* val rect =Rect()

                 layout_camera_small.getGlobalVisibleRect(rect)
                image.setCropRect(rect)*/
                d("widthImage",image.width.toString())

                var imageBitmap : Bitmap =image.image!!.toBitmap()
                val imageImage = image.image
                val left = (layout_camera.width/2) - 150
                val top = (layout_camera.height/2) - 150

                 cropedImage = cropImage(imageBitmap,layout_camera,layout_camera_small)
                   // cropedImage = imageImage!!.cropRect.set(Rect(left,top,left,top)) as ByteArray


     /*          val bundle = Bundle()
                bundle.putByteArray("cropedImage",cropedImage)
                val mainFragment = MainFragment()
                mainFragment.arguments = bundle*/
                image.close()

            }

            override fun onError(exc: ImageCaptureException) {
                cropedImage=null!!
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }
        })
    }
    fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
/*    fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }*/

    companion object {
        private const val TAG = "CameraXBasic"
      //  private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }
    fun loadBitmapFromView(v: View): Bitmap? {
        v.isDrawingCacheEnabled = true
        v.buildDrawingCache(true)
        val b = Bitmap.createBitmap(v.drawingCache)
        v.isDrawingCacheEnabled = false
        return b
    }
     fun cropImage(bitmap: Bitmap, frame: View, reference: View): ByteArray {

        val heightOriginal = frame.height
        d("frame.height",heightOriginal.toString())
        val widthOriginal = frame.width
        val heightFrame = reference.height
        val widthFrame = reference.width
        val leftFrame = reference.left
        val topFrame = reference.top

        d("heightframe",heightFrame.toString())
        d("widthfram",widthFrame.toString())
        d("heightbiger",heightOriginal.toString())
        d("widthbiger",widthOriginal.toString())

        val heightReal = bitmap.height
        val widthReal = bitmap.width
        d("heightimage",heightReal.toString())
        d("widthimage",widthReal.toString())
        val widthFinal = widthFrame * widthReal / widthOriginal
       val heightFinal = heightFrame * heightReal / heightOriginal
        val leftFinal =( widthReal / 2) - 150
        val topFinal = (heightReal / 2) - 150
        d("leftFrame",leftFrame.toString())
        d("leftFinal",leftFinal.toString())
        d("topFrame",topFrame.toString())
        d("topFinal",topFinal.toString())
        d("widthtest",(widthOriginal/2-150).toString())


        val bitmapFinal = Bitmap.createBitmap(
            bitmap,
            leftFinal  , topFinal, 300, 300
        )

        val stream = ByteArrayOutputStream()
        bitmapFinal.compress(
            Bitmap.CompressFormat.PNG,
            100,
            stream
        ) //100 is the best quality possibe
        return stream.toByteArray()
    }









    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {






        val root = inflater.inflate(R.layout.camera, container, false)
        val textureView: TextureView = root.findViewById(R.id.layout_camera)


        fun StartCamera() {
            CameraX.unbindAll()
            val aspectRatio = Rational(layout_camera.width,layout_camera.height)
            val screenSize = Size(layout_camera.width,layout_camera.height)
            val previewConfig: PreviewConfig  = PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screenSize)
                .build()
            val preview = Preview(previewConfig)
            preview.setOnPreviewOutputUpdateListener{
                val parent: ViewGroup = textureView.parent as ViewGroup

            textureView.surfaceTexture = it.surfaceTexture

            }
        }
       // val previewConfig = PreviewConfig
       // val surfaceProvider = previewView.createSurfaceProvider(cameraInfo)
        StartCamera()
        navController= Navigation.findNavController(activity!!.findViewById(R.id.nav_host_fragment))
        val btn_take_photo:ImageView = root.findViewById(R.id.btn_take_photo)
        val btn_accept: ImageView = root.findViewById(R.id.btn_accept)
        val btn_decline: ImageView = root.findViewById(R.id.btn_decline)
        val fabOpenAnim = AnimationUtils.loadAnimation(root.context, R.anim.fab_open)
        val fabCloseAnim = AnimationUtils.loadAnimation(root.context, R.anim.fab_close)

        btn_take_photo.setOnClickListener{
            btn_accept.startAnimation(fabOpenAnim)
            btn_decline.startAnimation(fabOpenAnim)
            btn_accept.isVisible = true
            btn_decline.isVisible= true
        }
        btn_accept.setOnClickListener{
            navController!!.navigate(R.id.action_navigation_camera_to_navigation_mainFragment)
        }
        btn_decline.setOnClickListener{
            btn_accept.startAnimation(fabCloseAnim)
            btn_decline.startAnimation(fabCloseAnim)
            btn_accept.isVisible = false
            btn_decline.isVisible= false
        }
        return root
    }

    override fun onResume() {
        val btn_add = activity!!.findViewById<FloatingActionButton>(R.id.fab_add)
        val btn_camera = activity!!.findViewById<FloatingActionButton>(R.id.fab_camera)
        val btn_gallery = activity!!.findViewById<FloatingActionButton>(R.id.fab_gallery)
        val toolbar:androidx.appcompat.widget.Toolbar = activity!!.findViewById(R.id.toolbar)
        btn_add.hide()
        btn_add.isClickable = false
        btn_camera.isClickable = false
        btn_gallery.isClickable = false
        btn_camera.hide()
        btn_gallery.hide()
        toolbar.isVisible = false

        super.onResume()
    }

    override fun onStop() {
        val btn_add = activity!!.findViewById<FloatingActionButton>(R.id.fab_add)
        val btn_camera = activity!!.findViewById<FloatingActionButton>(R.id.fab_camera)
        val btn_gallery = activity!!.findViewById<FloatingActionButton>(R.id.fab_gallery)
        val toolbar: androidx.appcompat.widget.Toolbar= activity!!.findViewById(R.id.toolbar)
        toolbar.isVisible=true
        //  btn_camera.show()
        btn_add.isClickable= true
        btn_camera.isClickable = true
        btn_gallery.isClickable = true
        btn_add.show()
        // btn_gallery.show()


        super.onStop()
    }
*/
}