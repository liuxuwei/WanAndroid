package com.learn.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.media.MediaCodec
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.LiveData
import com.learn.base.util.LogUtil
import java.io.File
import java.lang.Exception
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class MainCameraActivity : AppCompatActivity() {
    private lateinit var mViewFinder: PreviewView
    private lateinit var mCameraCaptureBtn: Button
    private var mImageCapture: ImageCapture? = null

    private lateinit var mOutputDir: File
    private lateinit var mCameraExecutor: ExecutorService

    companion object {
        private const val TAG = "MainCameraActivity"
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUEST_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_camera)

        initView()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUEST_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        mCameraCaptureBtn.setOnClickListener { takePhoto() }

        mOutputDir = getOutputDir()

        mCameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCameraExecutor.shutdown()
    }

    @SuppressLint("NewApi")
    private fun getOutputDir(): File {
        val mediaDir =
            externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
            }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun initView() {
        mCameraCaptureBtn = findViewById(R.id.camera_capture_button)
        mViewFinder = findViewById(R.id.viewFinder)

    }

    private fun takePhoto() {
        LogUtil.debug("take a photo")
        val imageCapture = mImageCapture ?: return

        val photoFile = File(
            mOutputDir,
            SimpleDateFormat(FILENAME_FORMAT, Locale.CHINA).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "photo captured success: $savedUri"
                Toast.makeText(this@MainCameraActivity, msg, Toast.LENGTH_SHORT).show()
                LogUtil.debug(msg)
            }

            override fun onError(exception: ImageCaptureException) {
                LogUtil.debug("Photo capture failed: ${exception.message}")
            }
        })
    }

    private fun startCamera() {
        LogUtil.debug("start Camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetResolution(Size(20, 20))
                .build()
                .also {
                    it.setSurfaceProvider(mViewFinder.surfaceProvider)
                }

            mImageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(mCameraExecutor, LuminosityAnalyzer { luma ->
                        LogUtil.debug("Average luminosity: $luma")
                    })
                }

            //默认选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, mImageCapture, imageAnalyzer)
            } catch (e: Exception) {
                LogUtil.debug("Use case binding failed $e")
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private class LuminosityAnalyzer(private val listener: LumaListener) :ImageAnalysis.Analyzer {
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }


        override fun analyze(image: ImageProxy) {
            val imageFormat = image.format

            if (imageFormat == ImageFormat.YUV_420_888) {
                LogUtil.debug("image format is ImageFormat.YUV_420_888 and width is ${image.width} & height is ${image.height}")
            }

            val bufferY = image.planes[0].buffer
            val bufferU = image.planes[1].buffer
            val bufferV = image.planes[2].buffer

            val dataU = bufferU.toByteArray()
            val dataV = bufferV.toByteArray()

            LogUtil.debug("U Arr is ${dataU.contentToString()}  V Arr is ${dataV.contentToString()}")

            val pixelStrideY = image.planes[0].pixelStride
            val pixelStrideU = image.planes[1].pixelStride
            val pixelStrideV = image.planes[2].pixelStride

            val rowStrideY = image.planes[0].rowStride
            val rowStrideU = image.planes[1].rowStride
            val rowStrideV = image.planes[2].rowStride

            LogUtil.debug("Y pixelStride is $pixelStrideY  and U pixelStride is $pixelStrideU and V pixelStride is $pixelStrideV  ")
            LogUtil.debug("Y rowStride is $rowStrideY  and U rowStride is $rowStrideU and V rowStride is $rowStrideV  ")

            val data = bufferY.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

    private fun allPermissionsGranted() = REQUEST_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permission Not Granted.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}