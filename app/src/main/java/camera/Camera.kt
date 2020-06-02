package camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.gettext.R
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.camera.*
import java.io.File
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toFile
import com.example.gettext.MainActivity
import kotlinx.android.synthetic.main.main_activity.*

import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.max
import java.util.Collections.min
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Camera: AppCompatActivity() {
    private lateinit var container: ConstraintLayout
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private var photoFile : File? = null
    private var displayId: Int = -1

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera)
        if (allPermissionsGranted()) {
            layout_camera.post{
                startCamera()
            }

        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        outputDirectory = getOutputDirectory()
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
           val path = photoFile!!.absolutePath
            val intent= Intent(this, MainActivity::class.java)
            intent.putExtra("imagePath",path)
            startActivity(intent)
            overridePendingTransition(0,R.anim.slide_out_top)

        }
        btn_decline.setOnClickListener{
            btn_accept.startAnimation(fabCloseAnim)
            btn_decline.startAnimation(fabCloseAnim)
            btn_accept.isVisible = false
            btn_decline.isVisible= false
            photoFile!!.delete()
            startCamera()

        }
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
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()

            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val metrics = DisplayMetrics().also { layout_camera.display.getRealMetrics(it) }
            Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")
            val rotation = layout_camera.display.rotation

            // Preview
            preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                //.setTargetRotation(rotation)
                .build()
            imageCapture = ImageCapture.Builder()
                //.setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)

                .build()

            // Select back camera
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,imageCapture)
                preview?.setSurfaceProvider(layout_camera.createSurfaceProvider())
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create timestamped output file to hold the image
         photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.ROOT
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile!!).build()

        // Setup image capture listener which is triggered after photo has
        // been taken

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                        val savedUri = Uri.fromFile(photoFile)
                        val msg = "Photo capture succeeded: $savedUri"
                        Log.d(TAG, msg)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            this@Camera.sendBroadcast(
                                Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                            )
                        }
                        val mimeType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(savedUri.toFile().extension)
                        MediaScannerConnection.scanFile(
                            this@Camera,
                            arrayOf(savedUri.toFile().absolutePath),
                            arrayOf(mimeType)
                        ) { _, uri ->
                            Log.d(TAG, "Image capture scanned into media store: $uri")
                        }

                    }
                })

    }
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = layout_camera?.let { view ->
            if (displayId == this@Camera.displayId) {
                Log.d(TAG, "Rotation changed: ${layout_camera.display.rotation}")
                imageCapture?.targetRotation = layout_camera.display.rotation

            }
        } ?: Unit
    }
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onStop() {
        overridePendingTransition(R.anim.slide_in_top,R.anim.slide_out_top)
        super.onStop()

    }
}