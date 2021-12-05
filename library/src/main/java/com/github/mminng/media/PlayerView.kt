package com.github.mminng.media

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Surface
import android.widget.FrameLayout
import com.github.mminng.media.controller.Controller
import com.github.mminng.media.controller.ControllerView
import com.github.mminng.media.player.Player
import com.github.mminng.media.render.Render
import com.github.mminng.media.utils.d

/**
 * Created by zh on 2021/10/1.
 */
class PlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs),
    Controller.OnControllerListener,
    Player.OnPlayerListener,
    Render.RenderCallback {

    private val interval: Long = 200

    private var renderView: Render
    private val controller: ControllerView = ControllerView(context)
    private var player: Player? = null
    private var onFullScreenModeChangedListener: (() -> Unit)? = null
    private val _progressRunnable: Runnable = Runnable {
        updateProgress()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.player_view, this)
        addView(controller)
        renderView = findViewById(R.id.media_render_view)
        renderView.setCallback(this)
        controller.setOnControllerListener(this)
    }

    override fun onPlayPause() {
        player?.let {
            if (it.isPlaying()) {
                it.pause()
            } else {
                it.start()
            }
        }
    }

    override fun onFullScreen() {
        onFullScreenModeChangedListener?.invoke()
    }

    override fun onProgressChanged(position: Int) {
        player?.seekTo(position)
    }

    override fun onPlayerState(isPlaying: Boolean) {
        controller.onPlayPause(isPlaying)
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        d("width=$width")
        d("height=$height")
        renderView.setAspectRatio(width.toFloat() / height.toFloat())
        updateProgress()
        player?.let { controller.onDuration(it.getDuration()) }

    }

    fun setPlayer(player: Player) {
        player.setOnVideoSizeChangedListener(this)
        this.player = player
    }

    fun setDataSource(source: String) {
        player?.let {
            it.setDataSource(source)
            it.prepareAsync()
        }
    }

    fun start() {
        player?.start()
    }

    fun pause() {
        player?.pause()
    }

    fun isPlaying(): Boolean {
        player?.let {
            return it.isPlaying()
        }
        return false
    }

    fun release() {
        player?.release()
    }

    fun setOnFullScreenModeChangedListener(listener: () -> Unit) {
        this.onFullScreenModeChangedListener = listener
    }

    fun updateProgress() {
        player?.let {
            controller.onProgress(it.getCurrentPosition())
        }
        postDelayed(_progressRunnable, interval)
    }


    override fun onRenderCreated(surface: Surface) {
        player?.setSurface(surface)
    }

    override fun onRenderChanged(surface: Surface, width: Int, height: Int) {
    }

    override fun onRenderDestroyed() {
    }
}