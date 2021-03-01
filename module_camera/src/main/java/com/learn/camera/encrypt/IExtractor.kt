package com.learn.camera.encrypt

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Create by lxw on 2021/3/1
 * Note：Exctractor interface
 */
interface IExtractor {
    /**
     * 获取音视频格式参数
     */
    fun getMediaFormat(): MediaFormat?

    /**
     * 读取音视频数据
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int

    /**
     * 获取当前帧时间
     */
    fun getCurrentTimestamp(): Long

    /**
     * Seek 到指定位置，并返回实际帧的时间戳
     */
    fun seek(position: Long): Long

    fun setStartPos(pos: Long)

    /**
     * 停止读取数据
     */
    fun stop()
}