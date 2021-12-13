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

    private var _currentDataSource: String = ""

    private var onFullScreenModeChangedListener: (() -> Unit)? = null

    init {
        onPlayerStateChanged(PlayerState.IDLE)
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

    fun setDataSource(source: String) {
        _currentDataSource = source
        _player?.setDataSource(source)
        onPlayerStateChanged(PlayerState.INITIALIZED)
    }

    fun prepareAsync() {
        _player?.prepareAsync()
        onPlayerStateChanged(PlayerState.PREPARING)
    }

    fun start() {
        _player?.start()
        _controller?.updateProgress()
        _controller?.onPlayPause(true)
        onPlayerStateChanged(PlayerState.STARTED)
    }

    fun pause() {
        _player?.pause()
        _controller?.stopProgress()
        _controller?.onPlayPause(false)
        onPlayerStateChanged(PlayerState.PAUSED)
    }

    override fun onPrepareAsync() {
        prepareAsync()
    }

    override fun onPlayPause() {
        _player?.let {
            if (it.isPlaying()) {
                pause()
            } else {
                start()
            }
        }
    }

    override fun onReplay() {
        start()
    }

    override fun onRetry() {
        reset()
        setDataSource(_currentDataSource)
        prepareAsync()
    }

    fun release() {
        _controller?.stopProgress()
        _player?.release()
        d("player release")
    }

    override fun onRenderCreated(surface: Surface) {
        _player?.setSurface(surface)
    }

    override fun onRenderChanged(width: Int, height: Int) {
    }

    override fun onRenderDestroyed() {
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        d("video_width:$width")
        d("video_height:$height")
        _renderer.setAspectRatio(width.toFloat() / height.toFloat())
    }

    override fun onBufferingUpdate(bufferingProgress: Int) {
        d("bufferingProgress:$bufferingProgress")
        _controller?.onBufferingProgressUpdate(bufferingProgress)
    }

    override fun onPlayerStateChanged(state: PlayerState, errorMessage: String) {
        _controller?.setControllerState(state, errorMessage)
        _player?.let {
            keepScreenOn = it.isPlaying()
        }
        when (state) {
            PlayerState.IDLE -> {
                d("STATE IDLE")
            }
            PlayerState.INITIALIZED -> {
                d("STATE INITIALIZED")
            }
            PlayerState.PREPARING -> {
                d("STATE PREPARING")
            }
            PlayerState.PREPARED -> {
                d("STATE PREPARED")
                start()
            }
            PlayerState.BUFFERING -> {
                d("STATE BUFFERING")
            }
            PlayerState.BUFFERED -> {
                d("STATE BUFFERED")
            }
            PlayerState.STARTED -> {
                d("STATE STARTED")
            }
            PlayerState.PAUSED -> {
                d("STATE PAUSED")
            }
            PlayerState.COMPLETION -> {
                d("STATE COMPLETED")
                _controller?.stopProgress()
                _controller?.onPlayPause(false)
            }
            PlayerState.ERROR -> {
                d("STATE ERROR:$errorMessage")
                _controller?.stopProgress()
                _controller?.onPlayPause(false)
            }
        }
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

    fun setRenderMode(mode: RenderMode) {
        _renderer.setRenderMode(mode)
    }

    fun setOnFullScreenModeChangedListener(listener: () -> Unit) {
        this.onFullScreenModeChangedListener = listener
    }

    fun reset() {
        _player?.reset()
        onPlayerStateChanged(PlayerState.IDLE)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _renderer.release()
    }

}