package com.github.mminng.media

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.Surface
import android.widget.FrameLayout
import com.github.mminng.media.controller.Controller
import com.github.mminng.media.player.Player
import com.github.mminng.media.player.state.PlayerState
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
    Renderer.OnRenderCallback, Player.OnPlayerListener,
    Player.OnPlayerStateListener, Controller.OnControllerListener {

    private var _renderer: Renderer
    private var _player: Player? = null
    private var _controller: Controller? = null

    private var onFullScreenModeChangedListener: (() -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.PlayerView, 0, 0).apply {
            try {
                val renderType = getInt(R.styleable.PlayerView_renderType, 0)
                _renderer =
                    if (renderType == 0) SurfaceRenderView(context) else TextureRenderView(context)
            } finally {
                recycle()
            }
        }
        addView(
            _renderer.getView(),
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        _renderer.setCallback(this)
    }

    override fun onPlayPause() {
        _player?.let {
            if (it.isPlaying()) {
                _controller?.stopProgress()
                it.pause()
            } else {
                _controller?.updateProgress()
                it.start()
            }
        }
    }

    override fun onFullScreen() {
        onFullScreenModeChangedListener?.invoke()
    }

    override fun onSeekTo(position: Int) {
        _player?.seekTo(position)
    }

    override fun onProgressUpdate() {
        _player?.let {
            _controller?.onProgressUpdate(it.getCurrentPosition())
        }
    }

    override fun onPlayerStateChanged(state: PlayerState) {
        when (state) {
            PlayerState.IDLE -> {

            }
            PlayerState.BUFFERING -> {

            }
            PlayerState.READY -> {

            }
            PlayerState.COMPLETED -> {

            }
            PlayerState.ERROR -> {

            }
        }
    }

    override fun onPlayingChanged(isPlaying: Boolean) {
        _controller?.onPlayPause(isPlaying)
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        d("width=$width")
        d("height=$height")
        _controller?.updateProgress()
        _renderer.setAspectRatio(width.toFloat() / height.toFloat())
        _player?.let { _controller?.onDuration(it.getDuration()) }
    }

    override fun onBufferingUpdate(bufferingProgress: Int) {
        _controller?.onBufferingProgressUpdate(bufferingProgress)
    }

    fun setDataSource(source: String) {
        _player?.let {
            it.setDataSource(source)
            it.prepareAsync()
        }
    }

    fun start() {
        _player?.start()
    }

    fun pause() {
        _player?.pause()
    }

    fun isPlaying(): Boolean {
        _player?.let {
            return it.isPlaying()
        }
        return false
    }

    fun release() {
        _controller?.stopProgress()
        _player?.release()
        d("player release")
    }

    fun setOnFullScreenModeChangedListener(listener: () -> Unit) {
        this.onFullScreenModeChangedListener = listener
    }

    fun setRenderMode(mode: RenderMode) {
        _renderer.setRenderMode(mode)
    }

    fun setPlayer(player: Player) {
        if (_player == null) {
            _player = player
            player.setOnPlayerListener(this)
            player.setOnPlayerStateListener(this)
        }
    }

    fun setController(controller: Controller) {
        if (_controller == null) {
            _controller = controller
            addView(controller.getView())
            controller.setOnControllerListener(this)
        }
    }

    override fun onRenderCreated(surface: Surface) {
        _player?.setSurface(surface)
    }

    override fun onRenderChanged(width: Int, height: Int) {
    }

    override fun onRenderDestroyed() {
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _renderer.release()
    }

}