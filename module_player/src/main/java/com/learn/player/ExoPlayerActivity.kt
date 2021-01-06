package com.learn.player

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.HttpDataSource.HttpDataSourceException
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException
import com.google.android.exoplayer2.util.MimeTypes
import com.learn.base.util.LogUtil


class ExoPlayerActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val video_url =
            "https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4"
    }

    private lateinit var mSimpleExoPlayer: SimpleExoPlayer
    private lateinit var mPlayerView: StyledPlayerView
    private lateinit var mBtnPlay: Button
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
        mSimpleExoPlayer = SimpleExoPlayer.Builder(this).build().apply {
            addListener(object : Player.EventListener {
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    //STATE_IDLE = 1 initial state, does not have any media to play
                    //STATE_BUFFERING = 2 player is not able to immediately play from current position
                    //this mostly happens because more data needs to be loaded;
                    //STATE_READY = 3 player is able to immediately play from current position;
                    //STATE_ENDED = 4 player finished playing all media.
                    LogUtil.debug("onPlaybackStateChanged --- $state")
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    super.onPlayerError(error)
                    LogUtil.debug("onPlayerError ---- ${error.message}")
                    if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                        val cause = error.sourceException
                        if (cause is HttpDataSourceException) {
                            // An HTTP error occurred.
                            // This is the request for which the error occurred.
                            val requestDataSpec = cause.dataSpec
                            // It's possible to find out more about the error both by casting and by
                            // querying the cause.
                            if (cause is InvalidResponseCodeException) {
                                // Cast to InvalidResponseCodeException and retrieve the response code,
                                // message and headers.
                                LogUtil.debug("PlayError InvalidResponseCodeException --- responseCode = ${cause.responseCode} responseMessage = ${cause.responseMessage}")
                            } else {
                                // Try calling httpError.getCause() to retrieve the underlying cause,
                                // although note that it may be null.
                                LogUtil.debug("error msg is ${cause.cause?.message}")

                            }
                        }
                    }
                    mSimpleExoPlayer.prepare()
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
                    LogUtil.debug("playWhenReady: $playWhenReady  & reason is $reason")
                    if (playWhenReady) {
                        mSimpleExoPlayer.play()
                    }
                }

                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    super.onTimelineChanged(timeline, reason)
                    LogUtil.debug("onTimelineChanged ${timeline.periodCount}  reason is $reason")
                }

            })

            addAnalyticsListener(object : AnalyticsListener {
                override fun onPositionDiscontinuity(
                    eventTime: AnalyticsListener.EventTime,
                    reason: Int
                ) {
                    super.onPositionDiscontinuity(eventTime, reason)
                    LogUtil.debug("onPositionDiscontinuity --- $reason eventTime = $eventTime")
                }

                override fun onSeekStarted(eventTime: AnalyticsListener.EventTime) {
                    super.onSeekStarted(eventTime)
                    LogUtil.debug("onSeekStarted")
                }

                override fun onPlaybackStateChanged(
                    eventTime: AnalyticsListener.EventTime,
                    state: Int
                ) {
                    super.onPlaybackStateChanged(eventTime, state)
                    LogUtil.debug("addAnalyticsListener onPlaybackStateChanged --- $state")
                }
            })
//            setVideoSurfaceHolder()
        }
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse("https://mf.zype.com/eyJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJodHRwczovL2d2c20uenlwZS5jb20vNWM4ODI4MDNjMWM4ZDcxMmNlMDAzOWVhLzVmY2RjMDEwMTVlNGIyMDAwMTA4NDAzZS81ZmNkYzExMDFiM2QzYzAwMDExZDFlYjUvNTQ1YmQ2Y2E2OTcwMmQwNWI5MDEwMDAwL2Y1NjVjNWFmLTE3OWItNDNiMi05NDg4LWRlYzMwNGU5YWIyNi5tM3U4IiwicGFyYW1zIjp7IjZwTEtNUTN5IjoiU3FpM05nOTAiLCJGMzhiMWRGNCI6IjVmZDk3ZGE1YTZkYTExMDAwMWZhODhlMyJ9LCJleHAiOjE2MDgwOTk3OTh9.-kalQiZeaZNHYPpj3jSCU9pG-PSzAkxgYXWAU4fxnuY"))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
        mSimpleExoPlayer.setMediaItem(mediaItem)
        mPlayerView.resizeMode =AspectRatioFrameLayout.RESIZE_MODE_FILL
        mPlayerView.player = mSimpleExoPlayer
        mSimpleExoPlayer.prepare()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSimpleExoPlayer.stop()
    }

    override fun onClick(v: View?) {

    }
}