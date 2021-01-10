package com.learn.camera.video

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.core.VideoCapture.OutputFileOptions
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.learn.base.util.LogUtil
import com.learn.camera.MainCameraActivity
import com.learn.camera.R
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class VideoCaptureActivity : AppCompatActivity() {
    private lateinit var mVideoCapture: VideoCapture
    private lateinit var mOutputDir: File

    //if current state is recording.
    private var mIsRecording = false

    companion object {
        val REQUEST_PERMISSION = arrayOf(Manifest.permission.RECORD_AUDIO)
        val REQUEST_CODE = 1
    }

    private val mVideoCaptureBtn: Button by lazy {
        findViewById(R.id.video_capture_btn)
    }

    private val mVideoFinder: PreviewView by lazy {
        findViewById(R.id.viewFinder)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_capture)

        mOutputDir = getOutputDir()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUEST_PERMISSION,
                REQUEST_CODE
            )
        }

        mVideoCaptureBtn.setOnClickListener {
            if (mIsRecording) {
                stopRecording()
                mVideoCaptureBtn.text = "start Recording"
            } else {
                startRecording()
                mVideoCaptureBtn.text = "stop Recording"
            }
            mIsRecording = !mIsRecording
        }
    }

    private fun getOutputDir(): File {
        val mediaFile = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.camera_app)).apply { mkdirs() }
        }
        return if (mediaFile != null && mediaFile.exists()) mediaFile else filesDir
    }

    private fun allPermissionsGranted() = REQUEST_PERMISSION.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * stop recording
     */
    @SuppressLint("RestrictedApi")
    private fun stopRecording() {
        mVideoCapture.stopRecording()
    }

    /**
     * start recording
     */
    @SuppressLint("RestrictedApi")
    private fun startRecording() {
        val videoFile = File(
            mOutputDir,
            SimpleDateFormat(
                MainCameraActivity.FILENAME_FORMAT,
                Locale.CHINA
            ).format(System.currentTimeMillis()) + ".mp4"
        )

        val outputFileOptions = OutputFileOptions.Builder(videoFile).build()

        mVideoCapture.startRecording(
            outputFileOptions,
            ContextCompat.getMainExecutor(this),
            object :
                VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(videoFile)
                    val msg = "video captured success: $savedUri"
                    Toast.makeText(this@VideoCaptureActivity, msg, Toast.LENGTH_SHORT).show()
                    LogUtil.debug(msg)
                }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    LogUtil.debug("Video capture failed: $message")
                }

            })
    }


    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(mVideoFinder.surfaceProvider)
                }


            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            mVideoCapture = VideoCapture.Builder()
//                .setAudioBitRate()
//                .setAudioChannelCount()
//                .setAudioMinBufferSize()
//                .setAudioRecordSource()
//                .setAudioSampleRate()
//                .setIFrameInterval()
//                .setVideoFrameRate()
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, mVideoCapture)
            } catch (e: Exception) {
                LogUtil.debug("bind useCase exception ${e.message}")
            }


        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permission Not Granted.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}