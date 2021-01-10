package com.learn.camera

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.learn.camera.video.VideoCaptureActivity

class EntranceActivity : AppCompatActivity() {
    private val mToPhotoActivityBtn: Button by lazy {
        findViewById(R.id.btn_to_photo)
    }

    private val mToVideoActivityBtn: Button by lazy {
        findViewById(R.id.btn_to_video)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrance)

        mToPhotoActivityBtn.setOnClickListener {
            startActivity(Intent(this, MainCameraActivity::class.java))
        }

        mToVideoActivityBtn.setOnClickListener {
            startActivity(Intent(this, VideoCaptureActivity::class.java))
        }
    }

}