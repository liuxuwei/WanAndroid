package com.learn.camera.encrypt

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

/**
 * Create by lxw on 2021/3/1
 * Note：BaseDecoder
 */
abstract class BaseDecoder: IDecoder {
    private val LOG_TAG = "BaseDecoder"

    //解码器是否正在运行
    private var mIsRunning = true

    //线程等待锁
    private val mLock = Object()

    //是否可以进入解码
    private var mReadyForDecode = false

    /********* ----------- 解码相关 -------------- ************/

    //音视频解码器
    protected var mCodec: MediaCodec? = null

    //音视频数据读取器
    protected var mExtractor: IExtractor? = null

    //解码输入缓存区
    protected var mInputBuffers: Array<ByteBuffer>? = null

    //解码输出缓存区
    protected var mOutputBuffers: Array<ByteBuffer>? = null

    //解码数据信息
    private var mBufferInfo = MediaCodec.BufferInfo()

    private var mDecodeState = DecodeState.RELEASE

    private var mStateListener: IDecoderStateListener? = null

    private var mFilePath: String = ""

    private var mDuration: Long = 0

    private var mEndPos: Long = 0

    //流数据是否结束
    private var mIsEOS = false

    protected var mVideoWidth = 0

    protected var mVideoHeight = 0


    override fun pauseDecode() {
        
    }

    override fun resumeDecode() {
        
    }

    override fun stopDecode() {
        
    }

    override fun isDecoding(): Boolean {
        return false
    }

    override fun isSeeking(): Boolean {
        return false
    }

    override fun isStop(): Boolean {
        return false
    }

    override fun setStateListener(listener: IDecoderStateListener?) {
        
    }

    override fun getVideoWidth() {
        
    }

    override fun getVideoHeight() {
        
    }

    override fun getVideoDuration() {
        
    }

    override fun getRotationAngle() {
        
    }

    override fun getMediaFormat() {
        
    }

    override fun getTrack(): Int {
        return 0
    }

    override fun getDecodeFilePath(): String {
        return ""
    }

    override fun run() {
        mDecodeState = DecodeState.STARTED
        mStateListener?.decoderPrepare(this)

        if (!init()) return


    }

    private fun init(): Boolean {
        if (mFilePath.isEmpty() || File(mFilePath).exists()) {
            Log.d(LOG_TAG, "init: 文件路径为空")
            mStateListener?.decoderError(this, "文件路径为空")
            return false
        }

        //检查子类参数是否完整
        if (!check()) return false

        mExtractor = initExtractor(mFilePath)

        if (mExtractor == null || mExtractor!!.getMediaFormat() == null) return false

        //初始化参数
        if (!initParams()) return false

        //初始化渲染器
        if (!initRender()) return false

        if (!initCodec()) return false

        return true
    }

    /**
     * 初始化解码器
     */
    private fun initCodec(): Boolean {
        try {
            //1、根据音视频编码格式初始化编码器
            val type = mExtractor!!.getMediaFormat()!!.getString(MediaFormat.KEY_MIME)!!
            mCodec = MediaCodec.createDecoderByType(type)
            //2、配置解码器
            if (!configCodec(mCodec!!, mExtractor!!.getMediaFormat()!!)) {
                waitDecode()
            }
            //3、启动解码器
            mCodec!!.start()

            //4、获取解码器缓冲区
            mInputBuffers = mCodec!!.inputBuffers
            mOutputBuffers = mCodec!!.outputBuffers

        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun initParams(): Boolean {
        try {
            mExtractor!!.let {
                val format = it.getMediaFormat()!!
                mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000
                if (mEndPos == 0L) mEndPos = mDuration
                initSpecParams(it.getMediaFormat()!!)
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }


    /**
     * 初始化子类自己特有的参数
     */
    abstract fun initSpecParams(mediaFormat: MediaFormat)

    /**
     * 初始化渲染器
     */
    abstract fun initRender(): Boolean

    /**
     * 初始化数据提取器
     */
    abstract fun initExtractor(mFilePath: String): IExtractor

    /**
     * 检查子类参数
     */
    abstract fun check(): Boolean

    /**
     * 配置解码器
     */
    abstract fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean

    /**
     * 解码线程进入等待
     */
    private fun waitDecode() {
        try {
            if (mDecodeState == DecodeState.PAUSE) {
                mStateListener?.decoderPause(this)
            }
            synchronized(mLock) {
                mLock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续运行
     */
    protected fun notifyDecode() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
        if (mDecodeState == DecodeState.DECODING) {
            mStateListener?.decoderRunning(this)
        }
    }

    /**
     * 渲染
     */
    abstract fun render(outputBuffers: ByteBuffer,
                        bufferInfo: MediaCodec.BufferInfo)

    /**
     * 结束解码
     */
    abstract fun doneDecode()
}