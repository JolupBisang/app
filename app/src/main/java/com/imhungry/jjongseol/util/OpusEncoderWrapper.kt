package com.imhungry.jjongseol.util

import com.theeasiestway.opus.Opus
import com.theeasiestway.opus.Constants

class OpusEncoderWrapper {
    private val codec = Opus()
    private val sampleRate = Constants.SampleRate._48000()
    private val channels = Constants.Channels.mono()
    private val application = Constants.Application.audio()
    private val frameSize = Constants.FrameSize._120()

    fun init(): Boolean {
        codec.encoderInit(sampleRate, channels, application)
        codec.encoderSetComplexity(Constants.Complexity.instance(10))
        codec.encoderSetBitrate(Constants.Bitrate.max())
        return true
    }

    fun encode(pcm: ByteArray): ByteArray? {
        return codec.encode(pcm, frameSize)
    }

    fun release() {
        codec.encoderRelease()
    }
}
