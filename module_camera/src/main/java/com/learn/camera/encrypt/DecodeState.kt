package com.learn.camera.encrypt

/**
 * Create by lxw on 2021/3/1
 * Note：Decoder State
 */
enum class DecodeState {
    //开始状态
    STARTED,
    //解码中
    DECODING,
    //解码暂停
    PAUSE,
    //正在快进
    SEEKING,
    //解码完成
    FINISH,
    //解码器释放
    RELEASE
}