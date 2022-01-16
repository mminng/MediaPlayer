package com.github.mminng.media

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.mminng.media.controller.Controller
import com.github.mminng.media.player.Player
import com.github.mminng.media.player.PlayerState
import com.github.mminng.media.renderer.RenderMode
import com.github.mminng.media.renderer.Renderer
import com.github.mminng.media.renderer.SurfaceRenderView
import com.github.mminng.media.renderer.TextureRenderView
import com.github.mminng.media.utils.d
import com.github.mminng.media.utils.e

/**
 * Created by zh on 2021/10/1.
 */
class PlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Renderer.OnRenderCallback,
    Player.OnPlayerListener, Player.OnPlayerStateListener,
    Controller.OnControllerListener {

    companion object {
        private const val STATE_KEY: String = "STATE_KEY"
    }

    private val stateMap: MutableMap<String, PlayerState> = mutableMapOf()

    private var _renderer: Renderer
    private var _player: Player? = null
    private var _controller: Controller? = null
    private var _playWhenPrepared: Boolean = false
    private var _pauseFromUser: Boolean = false
    private var _coverViewEnable: Boolean
    private var _completionViewEnable: Boolean
    private var _errorViewEnable: Boolean
    private var _onCoverBindListener: ((view: ImageView) -> Unit)? = null
    private var _onFullScreenModeChangedListener: (() -> Unit)? = null

    private var _currentDataSource: String = ""
    private var _currentRetryPosition: Int = 0

    val activity: AppCompatActivity = context as AppCompatActivity

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.PlayerView, 0, 0).apply {
            try {
                val renderType = getInt(R.styleable.PlayerView_renderType, 0)
                _renderer =
                    if (renderType == 0) SurfaceRenderView(context) else TextureRenderView(context)
                _coverViewEnable =
                    getBoolean(R.styleable.PlayerView_coverViewEnable, true)
                _completionViewEnable =
                    getBoolean(R.styleable.PlayerView_completionViewEnable, true)
                _errorViewEnable =
                    getBoolean(R.styleable.PlayerView_errorViewEnable, true)
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
        setBackgroundColor(Color.BLACK)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            e("onVisibilityChanged=$visibility")
        }
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
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
        _renderer.setVideoSize(width.toFloat(), height.toFloat())
    }

    override fun onBufferingUpdate(bufferingPosition: Int) {
        _controller?.onCurrentBufferingPosition(bufferingPosition)
    }

    override fun onPlayerStateChanged(state: PlayerState, errorMessage: String) {
        _controller?.onPlayerStateChanged(state, errorMessage)
        _player?.let {
            if (state != PlayerState.ERROR) {
                keepScreenOn = it.isPlaying()
            }
        }
        when (state) {
            PlayerState.IDLE -> {
                d("STATE IDLE")
                stateMap[STATE_KEY] = state
                _controller?.stopUpdatePosition()
            }
            PlayerState.INITIALIZED -> {
                d("STATE INITIALIZED")
                stateMap[STATE_KEY] = state
                _controller?.let {
                    if (_currentRetryPosition == 0 && it.isControllerReady()) {
                        it.onCurrentPosition(0)
                        it.onCurrentBufferingPosition(0)
                        it.onDuration(0)
                    }
                }
            }
            PlayerState.PREPARING -> {
                d("STATE PREPARING")
                stateMap[STATE_KEY] = state
                keepScreenOn = true
            }
            PlayerState.PREPARED -> {
                d("STATE PREPARED")
                stateMap[STATE_KEY] = state
                _player?.let {
                    _controller?.onDuration(it.getDuration())
                }
                if (_currentRetryPosition > 0) {
                    onSeekTo(_currentRetryPosition)
                    _currentRetryPosition = 0
                }
                if (_playWhenPrepared) {
                    start()
                }
            }
            PlayerState.BUFFERING -> {
                d("STATE BUFFERING")
                keepScreenOn = true
            }
            PlayerState.BUFFERED -> {
                d("STATE BUFFERED")
            }
            PlayerState.RENDERING -> {
                d("STATE RENDERING")
            }
            PlayerState.STARTED -> {
                d("STATE STARTED")
                stateMap[STATE_KEY] = state
                _controller?.updatePosition()
                _controller?.onPlayPause(true)
            }
            PlayerState.PAUSED -> {
                d("STATE PAUSED")
                stateMap[STATE_KEY] = state
                _controller?.stopUpdatePosition()
                _controller?.onPlayPause(false)
            }
            PlayerState.COMPLETION -> {
                d("STATE COMPLETED")
                stateMap[STATE_KEY] = state
                _pauseFromUser = true
                _controller?.stopUpdatePosition()
                _controller?.onPlayPause(false)
            }
            PlayerState.ERROR -> {
                d("STATE ERROR:$errorMessage")
                stateMap[STATE_KEY] = state
                keepScreenOn = false
                _player?.let {
                    _currentRetryPosition = it.getCurrentPosition()
                }
                _controller?.stopUpdatePosition()
                _controller?.onPlayPause(false)
            }
        }
    }

    override fun onBindCoverImage(view: ImageView) {
        _onCoverBindListener?.invoke(view)
    }

    override fun onPlayPause(pauseFromUser: Boolean) {
        if (getPlayerState() == PlayerState.ERROR) return
        if (getPlayerState() == PlayerState.INITIALIZED) {
            prepare(true)
        } else {
            _player?.let {
                if (it.isPlaying()) {
                    _pauseFromUser = pauseFromUser
                    pause()
                } else {
                    _pauseFromUser = false
                    start()
                }
            }
        }
    }

    override fun onFullScreen() {
        _onFullScreenModeChangedListener?.invoke()
    }

    override fun onSeekTo(position: Int) {
        if (getPlayerState() == PlayerState.IDLE ||
            getPlayerState() == PlayerState.INITIALIZED ||
            getPlayerState() == PlayerState.PREPARING ||
            getPlayerState() == PlayerState.ERROR
        ) return
        _player?.seekTo(position)
    }

    override fun onPositionUpdated() {
        _player?.let {
            _controller?.onCurrentPosition(it.getCurrentPosition())
        }
    }

    override fun onReplay() {
        _pauseFromUser = false
        start()
    }

    override fun onRetry() {
        reset()
        setDataSource(_currentDataSource)
        prepare(true)
    }

    /*public function*/
    override fun getPlayerState(): PlayerState {
        stateMap[STATE_KEY]?.let {
            return it
        }
        return PlayerState.IDLE
    }

    override fun prepare(playWhenPrepared: Boolean) {
        _playWhenPrepared = playWhenPrepared
        _player?.prepare()
        _player?.statePreparing()
    }

    fun setPlayer(player: Player) {
        if (_player == null) {
            _player = player
            player.setOnPlayerListener(this)
            player.setOnPlayerStateListener(this)
            player.stateIdle()
        }
    }

    fun setController(controller: Controller) {
        if (_controller == null) {
            _controller = controller
            controller.setCoverViewEnable(_coverViewEnable)
            controller.setCompletionViewEnable(_completionViewEnable)
            controller.setErrorViewEnable(_errorViewEnable)
            controller.setOnControllerListener(this)
            addView(controller.getView())
        }
    }

    fun setRenderMode(mode: RenderMode) {
        _renderer.setRenderMode(mode)
    }

    fun setCover(listener: (view: ImageView) -> Unit) {
        this._onCoverBindListener = listener
    }

    fun setDataSource(source: String) {
        _currentDataSource = source
        _player?.setDataSource(source)
        _player?.stateInitialized()
    }

    fun start() {
        if (_pauseFromUser) return
        if (getPlayerState() == PlayerState.IDLE ||
            getPlayerState() == PlayerState.INITIALIZED ||
            getPlayerState() == PlayerState.PREPARING ||
            getPlayerState() == PlayerState.ERROR
        ) return
        _player?.start()
        _player?.stateStarted()
    }

    fun pause() {
        if (getPlayerState() == PlayerState.ERROR) return
        _player?.let {
            if (it.isPlaying()) {
                it.pause()
                it.statePaused()
            }
        }
    }

    fun setOnFullScreenModeChangedListener(listener: () -> Unit) {
        this._onFullScreenModeChangedListener = listener
    }

    fun reset() {
        _player?.reset()
        _player?.stateIdle()
    }

    fun release() {
        _controller?.stopUpdatePosition()
        _player?.release()
        d("player release")
    }
    /*public function end*/

}