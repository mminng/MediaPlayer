package com.github.mminng.media

import android.media.MediaPlayer
import android.view.Surface
import android.view.SurfaceHolder
import com.github.mminng.media.player.BasePlayer
import com.github.mminng.media.utils.d

/**
 * Created by zh on 2021/10/2.
 */
class DefaultPlayer : BasePlayer() {

    private val player: MediaPlayer = MediaPlayer()

    init {
        player.setOnPreparedListener {
            prepared()
        }
        player.setOnVideoSizeChangedListener { _, width, height ->
            videoSizeChanged(width, height)
        }
        player.setOnCompletionListener {
            completion()
        }
        player.setOnBufferingUpdateListener { mp, percent ->
            bufferingUpdate(mp.duration / 100 * percent)
//            d("duration:${mp.duration}")
//            d("bufferingProgress:${mp.duration / 100 * percent}")
        }
        player.setOnSeekCompleteListener {
            d("SeekComplete")
        }
        player.setOnInfoListener { mp, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    //暂停播放开始缓冲更多数据
                    d("Info:暂停播放开始缓冲更多数据")
                    bufferingStart()
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    //缓冲了足够的数据重新开始播放
                    d("Info:缓冲了足够的数据重新开始播放")
                    bufferingEnd()
                }
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    d("Info:第一帧开始渲染")
                    bufferingEnd()
                }
            }
            false
        }
        player.setOnErrorListener { mp, what, extra ->
            error("what=$what/extra=$extra")
            true
        }
    }

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

    override fun getCurrentPosition(): Int = player.currentPosition

    override fun getDuration(): Int = player.duration

    override fun setDisplay(display: SurfaceHolder) {
        player.setDisplay(display)
    }

    override fun setSurface(display: Surface) {
        player.setSurface(display)
    }

    override fun isPlaying(): Boolean = player.isPlaying

    override fun release() {
        player.release()
    }
}