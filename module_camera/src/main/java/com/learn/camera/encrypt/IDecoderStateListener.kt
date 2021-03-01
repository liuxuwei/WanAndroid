package com.learn.camera.encrypt

interface IDecoderStateListener {

    fun decoderPrepare(decoder: IDecoder)

    fun decoderRunning(decoder: IDecoder)

    fun decoderPause(decoder: IDecoder)

    fun decoderError(decoder: IDecoder, errorInfo: String)
}
