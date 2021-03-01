package com.learn.camera.encrypt

/**
 * Create by lxw on 2021/3/1
 * Note：解码器
 */
interface IDecoder: Runnable {

    /**
     * 暂停解码
     */
    fun pauseDecode()

    /**
     * 继续解码
     */
    fun resumeDecode()

    /**
     * 停止解码
     */
    fun stopDecode()

    /**
     * 是否正在解码
     */
    fun isDecoding(): Boolean

    /**
     * 是否正在快进
     */
    fun isSeeking(): Boolean

    /**
     * 是否停止解码
     */
    fun isStop(): Boolean

    /**
     * 设置状态监听器
     */
    fun setStateListener(listener: IDecoderStateListener?)

    /**
     * 获取视频宽
     */
    fun getVideoWidth()

    /**
     * 获取视频高
     */
    fun getVideoHeight()

    /**
     * 获取视频时长
     */
    fun getVideoDuration()

    /**
     * 获取视频旋转角度
     */
    fun getRotationAngle()

    /**
     * 获取音视频对应格式参数
     */
    fun getMediaFormat()

    /**
     * 获取音视频对应轨道
     */
    fun getTrack(): Int

    /**
     * 获取解码文件路径
     */
    fun getDecodeFilePath(): String
}