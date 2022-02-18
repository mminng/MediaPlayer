package com.github.mminng.media.player

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi

/**
 * Created by zh on 2021/10/2.
 */
class DefaultPlayer : BasePlayer(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private val player: MediaPlayer = MediaPlayer()
    private var _bufferingPosition: Int = 0

    init {
        player.setOnPreparedListener(this)
        player.setOnVideoSizeChangedListener(this)
        player.setOnBufferingUpdateListener(this)
        player.setOnInfoListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }

    /*MediaPlayer callback*/
    override fun onPrepared(mp: MediaPlayer?) {
        statePrepared()
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        videoSizeChanged(width, height)
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        if (getPlayerState() == PlayerState.IDLE ||
            getPlayerState() == PlayerState.INITIALIZED ||
            getPlayerState() == PlayerState.PREPARING ||
            getPlayerState() == PlayerState.ERROR
        ) return
        mp?.let {
            _bufferingPosition = it.duration * percent / 100
        }
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                stateBufferingStart()
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                stateBufferingEnd()
            }
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                stateRenderingStart()
            }
        }
        return false
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (extra) {
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                stateError("MEDIA_ERROR_SERVER_DIED($what,$extra)")
            }
            MediaPlayer.MEDIA_ERROR_IO -> {
                stateError("MEDIA_ERROR_IO($what,$extra)")
            }
            MediaPlayer.MEDIA_ERROR_MALFORMED -> {
                stateError("MEDIA_ERROR_MALFORMED($what,$extra)")
            }
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> {
                stateError("MEDIA_ERROR_UNSUPPORTED($what,$extra)")
            }
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> {
                stateError("MEDIA_ERROR_TIMED_OUT($what,$extra)")
            }
            else -> {
                stateError("MEDIA_ERROR_UNKNOWN($what,$extra)")
            }
        }
        return true
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stateCompletion()
    }
    /*MediaPlayer callback end*/

    override fun setDataSource(source: String) {
        player.setDataSource(source)
    }

    override fun prepare() {
        player.prepareAsync()
    }

    override fun start() {
        player.start()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(position: Int) {
        player.seekTo(position)
    }

    override fun setSurface(surface: Surface?) {
        player.setSurface(surface)
    }

    override fun setSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            player.playbackParams = player.playbackParams.setSpeed(speed)
        }
    }

    override fun reset() {
        player.reset()
    }

    override fun release() {
        player.release()
    }

    override fun isPlaying(): Boolean = player.isPlaying

    override fun getCurrentPosition(): Int = player.currentPosition

    override fun getBufferPosition(): Int = _bufferingPosition

    override fun getDuration(): Int = player.duration

    override fun getVideoWidth(): Int = player.videoWidth

    override fun getVideoHeight(): Int = player.videoHeight

}