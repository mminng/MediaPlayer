package com.github.mminng.media

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.Surface
import android.widget.FrameLayout
import com.github.mminng.media.controller.Controller
import com.github.mminng.media.controller.ControllerView
import com.github.mminng.media.player.Player
import com.github.mminng.media.renderer.RenderMode
import com.github.mminng.media.renderer.Renderer
import com.github.mminng.media.renderer.SurfaceRenderView
import com.github.mminng.media.renderer.TextureRenderView
import com.github.mminng.media.utils.d

/**
 * Created by zh on 2021/10/1.
 */
class PlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs),
    Renderer.OnRenderCallback, Player.OnPlayerListener, Controller.OnControllerListener {

    private val interval: Long = 200

    private var _renderView: Renderer
    private var player: Player? = null
    private val controller: ControllerView = ControllerView(context)
    private var onFullScreenModeChangedListener: (() -> Unit)? = null
    private val _progressRunnable: Runnable = Runnable {
        updateProgress()
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.PlayerView, 0, 0).apply {
            try {
                val renderType = getInt(R.styleable.PlayerView_renderType, 0)
                _renderView = if (renderType == 0) {
                    SurfaceRenderView(context)
                } else {
                    TextureRenderView(context)
                }
            } finally {
                recycle()
            }
        }
        addView(
            _renderView.getView(),
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        addView(controller)
        _renderView.setCallback(this)
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
        _renderView.setAspectRatio(width.toFloat() / height.toFloat())
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
        removeCallbacks(_progressRunnable)
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

    fun setRenderMode(mode: RenderMode) {
        _renderView.setRenderMode(mode)
    }

    override fun onRenderCreated(surface: Surface) {
        player?.setSurface(surface)
    }

    override fun onRenderChanged(width: Int, height: Int) {
    }

    override fun onRenderDestroyed() {
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _renderView.release()
    }

}