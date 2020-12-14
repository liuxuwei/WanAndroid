package com.learn.player

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes

class ExoPlayerActivity : AppCompatActivity() {
    companion object {
        const val video_url =
            "https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4"
    }

    private lateinit var mSimpleExoPlayer: SimpleExoPlayer
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
        mSimpleExoPlayer = SimpleExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse("http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8"))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
        mSimpleExoPlayer.setMediaItem(mediaItem)
        mPlayerView.resizeMode =AspectRatioFrameLayout.RESIZE_MODE_FILL
        mPlayerView.player = mSimpleExoPlayer
        mSimpleExoPlayer.prepare()
        mSimpleExoPlayer.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSimpleExoPlayer.stop()
    }
}