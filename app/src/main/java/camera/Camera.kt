package camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils

import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.gettext.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.camera.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Camera:AppCompatActivity() {
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var navController: NavController?=null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera)
        val fabOpenAnim = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabCloseAnim = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        btn_take_photo.setOnClickListener{
            btn_accept.startAnimation(fabOpenAnim)
            btn_decline.startAnimation(fabOpenAnim)
            btn_accept.isVisible = true
            btn_decline.isVisible= true
            takePhoto()
        }
        btn_accept.setOnClickListener{
           // navController!!.navigate(R.id.action_navigation_camera_to_navigation_mainFragment)
        }
        btn_decline.setOnClickListener{
            btn_accept.startAnimation(fabCloseAnim)
            btn_decline.startAnimation(fabCloseAnim)
            btn_accept.isVisible = false
            btn_decline.isVisible= false
        }
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val aspectRatio = Rational(layout_camera.width,layout_camera.height)
            val screenSize = Size(layout_camera.width,layout_camera.height)
            preview = Preview.Builder()
                .setTargetResolution(screenSize)
                .build()

            // Select back camera
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(this,cameraSelector,preview)
                preview?.setSurfaceProvider(layout_camera.createSurfaceProvider(camera?.cameraInfo))
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // TODO
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
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
