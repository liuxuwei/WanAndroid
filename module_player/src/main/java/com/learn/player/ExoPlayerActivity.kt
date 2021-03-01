package com.learn.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource.HttpDataSourceException
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.video.VideoListener
import com.learn.base.util.LogUtil


class ExoPlayerActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val video_url =
            "https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4"

        const val hls_url =
            "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"
    }

    private lateinit var mSimpleExoPlayer: SimpleExoPlayer
    private lateinit var mPlayerView: StyledPlayerView
    private lateinit var mTrackSelector: DefaultTrackSelector
    private lateinit var m1080Btn: Button
    private lateinit var m720Btn: Button
    private lateinit var m480Btn: Button

    private val maxWidthArr = intArrayOf(1920, 1280, 640)
    private val maxHeightArr = intArrayOf(1080, 720, 480)
    private val bitrateArr = intArrayOf(737777, 484444, 200000)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_player)

        initView()
        initPlayer()
    }

    private fun initView() {
        mPlayerView = findViewById(R.id.play_view)
        m1080Btn = findViewById(R.id.video_1080)
        m720Btn = findViewById(R.id.video_720)
        m480Btn = findViewById(R.id.video_480)
        m1080Btn.setOnClickListener {
            changeVideoSize(0)
        }

        m720Btn.setOnClickListener {
            changeVideoSize(1)
        }

        m480Btn.setOnClickListener {
            changeVideoSize(2)
        }
    }

    private fun initPlayer() {

        mTrackSelector = DefaultTrackSelector(this)

        mSimpleExoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(mTrackSelector)
            .build()
            .apply {

                addListener(object : Player.EventListener {

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        super.onMediaItemTransition(mediaItem, reason)
                    }


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


                    override fun onMetadata(
                        eventTime: AnalyticsListener.EventTime,
                        metadata: Metadata
                    ) {
                        super.onMetadata(eventTime, metadata)
                        LogUtil.debug("onMetadata  ---- $metadata ")
                    }

                    override fun onBandwidthEstimate(
                        eventTime: AnalyticsListener.EventTime,
                        totalLoadTimeMs: Int,
                        totalBytesLoaded: Long,
                        bitrateEstimate: Long
                    ) {
                        super.onBandwidthEstimate(
                            eventTime,
                            totalLoadTimeMs,
                            totalBytesLoaded,
                            bitrateEstimate
                        )
                        LogUtil.debug("onBandwidthEstimate --- $bitrateEstimate")
                    }

                    override fun onVideoSizeChanged(
                        eventTime: AnalyticsListener.EventTime,
                        width: Int,
                        height: Int,
                        unappliedRotationDegrees: Int,
                        pixelWidthHeightRatio: Float
                    ) {
                        super.onVideoSizeChanged(
                            eventTime,
                            width,
                            height,
                            unappliedRotationDegrees,
                            pixelWidthHeightRatio
                        )
                        LogUtil.debug("onVideoSize changed -----")
                    }

                    override fun onSeekStarted(eventTime: AnalyticsListener.EventTime) {
                        super.onSeekStarted(eventTime)
                        LogUtil.debug("onSeekStarted ---  ${eventTime.currentPlaybackPositionMs}")
                    }


                    override fun onTracksChanged(
                        eventTime: AnalyticsListener.EventTime,
                        trackGroups: TrackGroupArray,
                        trackSelections: TrackSelectionArray
                    ) {
                        super.onTracksChanged(eventTime, trackGroups, trackSelections)
                        for (i in 0 until trackGroups.length) {
                            for (j in 0 until trackGroups[i].length) {
                                LogUtil.debug(
                                    "onTracksChanged ----- ${
                                        Format.toLogString(
                                            trackGroups[i].getFormat(j)
                                        )
                                    }"
                                )
                            }
                        }
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

                videoComponent?.addVideoListener(object : VideoListener {
                    override fun onVideoSizeChanged(
                        width: Int,
                        height: Int,
                        unappliedRotationDegrees: Int,
                        pixelWidthHeightRatio: Float
                    ) {
                        super.onVideoSizeChanged(
                            width,
                            height,
                            unappliedRotationDegrees,
                            pixelWidthHeightRatio
                        )
                        LogUtil.debug("onVideoSizeChanged --- $width x $height")
                    }
                })
            }

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(hls_url))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
        val mediaSource = HlsMediaSource.Factory(DefaultDataSourceFactory(this))
            .createMediaSource(mediaItem)

        val loopingMediaSource = LoopingMediaSource(mediaSource)
        mSimpleExoPlayer.setMediaSource(loopingMediaSource)
        mPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        mPlayerView.player = mSimpleExoPlayer
        mSimpleExoPlayer.prepare()
    }

    private fun changeVideoSize(index: Int) {
        LogUtil.debug("changeVideoSize ---- ${bitrateArr[index]}")
        val trackParameter = mTrackSelector.parameters.buildUpon()
            .setMaxVideoBitrate(bitrateArr[index])
            .build()

        mTrackSelector.parameters = trackParameter
    }

    override fun onDestroy() {
        super.onDestroy()
        mSimpleExoPlayer.stop()
    }

    override fun onClick(v: View?) {

    }
}