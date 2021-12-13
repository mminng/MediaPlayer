package com.github.mminng.media

import android.media.MediaPlayer
import android.view.Surface
import com.github.mminng.media.player.BasePlayer

/**
 * Created by zh on 2021/10/2.
 */
class DefaultPlayer : BasePlayer(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private val player: MediaPlayer = MediaPlayer()

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
        prepared()
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        videoSizeChanged(width, height)
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        mp?.let {
            bufferingUpdate(mp.duration / 100 * percent)
        }
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                bufferingStart()
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                bufferingEnd()
            }
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                bufferingEnd()
            }
        }
        return true
    }

    override fun onCompletion(mp: MediaPlayer?) {
        completion()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (extra) {
            MediaPlayer.MEDIA_ERROR_IO -> {
                error("MEDIA_ERROR_IO")
            }
            MediaPlayer.MEDIA_ERROR_MALFORMED -> {
                error("MEDIA_ERROR_MALFORMED")
            }
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> {
                error("MEDIA_ERROR_UNSUPPORTED")
            }
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> {
                error("MEDIA_ERROR_TIMED_OUT")
            }
            else -> {
                error("MEDIA_ERROR_UNKNOWN")
            }
        }
        return true
    }
    /*MediaPlayer callback end*/

    override fun setDataSource(source: String) {
        player.setDataSource(source)
    }

    override fun prepareAsync() {
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

    override fun setSurface(surface: Surface) {
        player.setSurface(surface)
    }

    override fun reset() {
        player.reset()
    }

    override fun release() {
        player.release()
    }

    override fun isPlaying(): Boolean = player.isPlaying

    override fun getCurrentPosition(): Int = player.currentPosition

    override fun getDuration(): Int = player.duration

}