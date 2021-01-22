package com.learn.camera.encrypt

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat

/**
 * Create by lxw on 2021/1/11
 * Note：H264 Encoder
 */
object AVCEncoder {

    private const val INPUT_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
    private const val OUTPUT_MEDIA_FORMAT = MediaFormat.MIMETYPE_VIDEO_AVC

    private lateinit var mMediaCodec: MediaCodec
    private val info: MediaCodec.BufferInfo = MediaCodec.BufferInfo()

    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mFrameRate = 0
    private var mBitRate = 0

    fun create(width: Int, height: Int, frameRate: Int, bitRate: Int): AVCEncoder {
        mVideoWidth = width
        mVideoHeight = height
        mFrameRate = frameRate
        mBitRate = bitRate

        return this
    }

    fun configure(): AVCEncoder {
        mMediaCodec = MediaCodec.createEncoderByType(OUTPUT_MEDIA_FORMAT)
        val mediaFormat = MediaFormat.createVideoFormat(OUTPUT_MEDIA_FORMAT, mVideoHeight, mVideoWidth)
        //比特率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate)
        //帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate)
        //color format
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, INPUT_COLOR_FORMAT)
        //IDR帧刷新时间       ??? why is 5
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return this
    }

    fun start(): AVCEncoder {
        mMediaCodec.start()
        return this
    }

    fun pause(): AVCEncoder {
        mMediaCodec.stop()
        return this
    }

    fun close(): AVCEncoder {
        mMediaCodec.stop()
        mMediaCodec.release()
        return this
    }

    fun encodeStream(input: ByteArray, output: ByteArray) {

    }

}