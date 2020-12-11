package com.learn.player

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView

class ExoPlayerActivity : AppCompatActivity() {
    companion object {
        const val video_url =
            "https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4"
    }
    private lateinit var mPlayerView: StyledPlayerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_player)

        initView()
        initPlayer()
    }

    private fun initView() {
        mPlayerView = findViewById(R.id.play_view)
    }

    private fun initPlayer() {
        val simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
        mPlayerView.player = simpleExoPlayer
    }
}