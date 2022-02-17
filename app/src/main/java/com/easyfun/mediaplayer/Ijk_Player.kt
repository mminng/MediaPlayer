package com.easyfun.mediaplayer

import android.view.Surface
import com.github.mminng.media.player.BasePlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * Created by zh on 2022/2/15.
 */
class Ijk_Player : BasePlayer() {

    private val player: IjkMediaPlayer = IjkMediaPlayer()
    private var _bufferingPosition: Int = 0

    init {
        player.setOnPreparedListener {
            statePrepared()
        }
        player.setOnVideoSizeChangedListener { iMediaPlayer, i, i2, i3, i4 ->
            videoSizeChanged(i, i2)
        }
        player.setOnBufferingUpdateListener { iMediaPlayer, i ->
            _bufferingPosition = (iMediaPlayer.duration * i / 100).toInt()
        }
        player.setOnInfoListener(object : IMediaPlayer.OnInfoListener {
            override fun onInfo(p0: IMediaPlayer?, p1: Int, p2: Int): Boolean {
                when (p1) {
                    IjkMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        stateBufferingStart()
                    }
                    IjkMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        stateBufferingEnd()
                    }
                    IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                        stateRenderingStart()
                    }
                }
                return false
            }
        })
        player.setOnCompletionListener {
            stateCompletion()
        }
        player.setOnErrorListener(object : IMediaPlayer.OnErrorListener {
            override fun onError(p0: IMediaPlayer?, what: Int, extra: Int): Boolean {
                when (what) {
                    IjkMediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                        stateError("MEDIA_ERROR_SERVER_DIED($what,$extra)")
                    }
                    IjkMediaPlayer.MEDIA_ERROR_IO -> {
                        stateError("MEDIA_ERROR_IO($what,$extra)")
                    }
                    IjkMediaPlayer.MEDIA_ERROR_MALFORMED -> {
                        stateError("MEDIA_ERROR_MALFORMED($what,$extra)")
                    }
                    IjkMediaPlayer.MEDIA_ERROR_UNSUPPORTED -> {
                        stateError("MEDIA_ERROR_UNSUPPORTED($what,$extra)")
                    }
                    IjkMediaPlayer.MEDIA_ERROR_TIMED_OUT -> {
                        stateError("MEDIA_ERROR_TIMED_OUT($what,$extra)")
                    }
                    else -> {
                        stateError("MEDIA_ERROR_UNKNOWN($what,$extra)")
                    }
                }
                return true
            }
        })
    }

    override fun setDataSource(source: String) {
        player.dataSource = source
    }

    override fun prepare() {
        player.prepareAsync()
    }

    override fun pause() {
        player.pause()
    }

    override fun start() {
        player.start()
    }

    override fun seekTo(position: Int) {
        player.seekTo(position.toLong())
    }

    override fun setSurface(surface: Surface?) {
        player.setSurface(surface)
    }

    override fun reset() {
        player.reset()
    }

    override fun release() {
        player.release()
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }

    override fun getCurrentPosition(): Int {
        return player.currentPosition.toInt()
    }

    override fun getBufferPosition(): Int {
        return _bufferingPosition
    }

    override fun getDuration(): Int {
        return player.duration.toInt()
    }

    override fun getVideoWidth(): Int {
        return player.videoWidth
    }

    override fun getVideoHeight(): Int {
        return player.videoHeight
    }
}