package com.learn.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.learn.base.util.LogUtil
import com.learn.base.util.ThreadManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double, yuvDataArr: ByteArray) -> Unit

class MainCameraActivity : AppCompatActivity() {
    private lateinit var yuvFile: File
    private lateinit var mViewFinder: PreviewView
    private lateinit var mCameraCaptureBtn: Button
    private var mImageCapture: ImageCapture? = null
    private var mHasYUVData = false

    private var mFrameCount = 0
    private var mTempYUVArr = ByteArray(0)
    private lateinit var mGetYuvDataRunnable: Runnable

    private lateinit var mOutputDir: File
    private lateinit var mCameraExecutor: ExecutorService

    private val mHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "MainCameraActivity"
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUEST_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

        yuvFile = File(mOutputDir, "tempI420.yuv")

        LogUtil.debug("file name is ${yuvFile.absolutePath}")

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
            SimpleDateFormat(
                FILENAME_FORMAT,
                Locale.CHINA
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
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

        mGetYuvDataRunnable = createRunnable()

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
                    it.setAnalyzer(mCameraExecutor, LuminosityAnalyzer { luma, yuvData ->
                        LogUtil.debug("回调 + ${yuvData.contentToString()}")
                        mHasYUVData = true
                        mTempYUVArr = yuvData
                        mHandler.post(mGetYuvDataRunnable)
                    })
                }

            //默认选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    mImageCapture,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                LogUtil.debug("Use case binding failed $e")
            }
        }, ContextCompat.getMainExecutor(this))


    }

    private fun createRunnable(): Runnable {
        return Runnable {
            //dataY size = image.width * image.height;
            //dataU size = image.width * image.height / 2 - 1; ???? why minus 1
            //dataV size = image.width * image.height / 2 - 1; ????  -1
            LogUtil.debug("write data")
            while (mHasYUVData && mFrameCount <= 60) {
                LogUtil.debug("mFrameCount = $mFrameCount")
                dumpYUVFile(yuvFile.absolutePath, mTempYUVArr)
                ++mFrameCount
                mHasYUVData = false
            }
        }
    }


    @Synchronized
    private fun dumpYUVFile(fileName: String, data: ByteArray) {
        LogUtil.debug("filename is $fileName")
        val outStream: FileOutputStream

        try {
            outStream = FileOutputStream(fileName, true)
            outStream.write(data)
            outStream.close()
        } catch (ioe: IOException) {
            throw RuntimeException("failed writing data to file $fileName", ioe)
        }

    }


    private inner class LuminosityAnalyzer(private val listener: LumaListener) :
        ImageAnalysis.Analyzer {
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }


        override fun analyze(image: ImageProxy) {
            val imageFormat = image.format


//            if (imageFormat == ImageFormat.YUV_420_888) {
//                LogUtil.debug("image format is ImageFormat.YUV_420_888 and width is ${image.width} & height is ${image.height}")
//            }

            val bufferY = image.planes[0].buffer
            val bufferU = image.planes[1].buffer
            val bufferV = image.planes[2].buffer

            val dataY = bufferY.toByteArray()
            val dataU = bufferU.toByteArray()
            val dataV = bufferV.toByteArray()
            val pixels = dataY.map { it.toInt() and 0xFF }

            val dataUV = ByteArray(dataU.size + 1)
            val uSize = dataY.size / 4

            LogUtil.debug("uSize is $uSize   dataUSize is + ${dataU.size}")


            //生成  YYYYYYYYYYYYYYYYUUUUVVVV      ---- I420
            for (i in 0 until uSize) {
                dataUV[i] = dataU[i * image.planes[1].pixelStride]
            }

            for (i in 0 until uSize -1) {
                dataUV[uSize + 1 + i] = dataV[i * image.planes[2].pixelStride]
            }


            //生成 YYYYYYYYYYYYYYYYUVUVUVUV       ---- NV12
//            for (i in 0 until (dataU.size - 1) / 2) {
//                dataUV[i * 2] = dataU[i * image.planes[1].pixelStride]
//            }
//
//            for (i in 0 until (dataV.size - 1) / 2) {
//                dataUV[2 * i + 1] = dataV[i * image.planes[2].pixelStride]
//            }


            //生成 YYYYYYYYYYYYYYYYVUVUVUVU     ---- NV21
//            for (i in 0 .. (dataV.size - 1) / 2) {
//                dataUV[i * 2] = dataV[i * image.planes[2].pixelStride]
//            }
//
//            for (i in 0 .. (dataU.size - 1) / 2) {
//                dataUV[2 * i + 1] = dataU[i * image.planes[1].pixelStride]
//            }

            val yuvArr = ByteArray(dataY.size + dataUV.size)

            System.arraycopy(dataY, 0, yuvArr, 0, dataY.size)
            System.arraycopy(dataUV, 0, yuvArr, dataY.size, dataUV.size)

            val luma = pixels.average()

            listener(luma, yuvArr)

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