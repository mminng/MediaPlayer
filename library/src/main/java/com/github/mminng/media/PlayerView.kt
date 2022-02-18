package com.github.mminng.media

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.github.mminng.media.controller.Controller
import com.github.mminng.media.player.OnPlayerListener
import com.github.mminng.media.player.Player
import com.github.mminng.media.player.PlayerState
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
) : FrameLayout(context, attrs), Player.Listener, Renderer.Listener, Controller.Listener {

    private val playerView: FrameLayout = FrameLayout(context, attrs)
    private val playerState: MutableList<PlayerState> = mutableListOf(PlayerState.IDLE)
    private var _player: Player? = null
    private var _renderer: Renderer
    private var _controller: Controller? = null
    private var _isFullScreen: Boolean = false
    private var _playWhenPrepared: Boolean = false
    private var _pauseFromUser: Boolean = false
    private var _currentDataSource: String = ""
    private var _currentRetryPosition: Int = 0
    private var _onPlayerListener: OnPlayerListener? = null
    private val activity: AppCompatActivity = context as AppCompatActivity
    private val activityContentView: ViewGroup by lazy {
        activity.findViewById(Window.ID_ANDROID_CONTENT)
    }

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
        _renderer.setListener(this)
        addView(playerView)
        playerView.addView(
            _renderer.getView(),
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        d("video_width:$width")
        d("video_height:$height")
        _renderer.setVideoSize(width, height)
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
                d("Player idle")
                playerState[0] = state
                _controller?.stopUpdatePosition()
            }
            PlayerState.INITIALIZED -> {
                d("Player initialized")
                playerState[0] = state
                _controller?.let {
                    if (_currentRetryPosition == 0 && it.isControllerReady()) {
                        it.onCurrentPosition(0)
                        it.onBufferPosition(0)
                        it.onDuration(0)
                    }
                }
            }
            PlayerState.PREPARING -> {
                d("Player preparing")
                playerState[0] = state
                keepScreenOn = true
            }
            PlayerState.PREPARED -> {
                d("Player prepared")
                playerState[0] = state
                _player?.let {
                    _controller?.onDuration(it.getDuration())
                }
                _onPlayerListener?.onPrepared()
                if (_currentRetryPosition > 0) {
                    onSeekTo(_currentRetryPosition)
                    _currentRetryPosition = 0
                }
                if (_playWhenPrepared) {
                    start()
                }
            }
            PlayerState.BUFFERING -> {
                d("Player buffering Start")
                keepScreenOn = true
            }
            PlayerState.BUFFERED -> {
                d("Player buffering End")
            }
            PlayerState.RENDERING -> {
                d("Player rendering")
            }
            PlayerState.STARTED -> {
                d("Player started")
                playerState[0] = state
                _controller?.updatePosition()
                _controller?.onPlayOrPause(true)
                _onPlayerListener?.onStarted()
            }
            PlayerState.PAUSED -> {
                d("Player paused")
                playerState[0] = state
                _controller?.stopUpdatePosition()
                _controller?.onPlayOrPause(false)
                _onPlayerListener?.onPaused()
            }
            PlayerState.COMPLETION -> {
                d("Player completed")
                playerState[0] = state
                _pauseFromUser = true
                _controller?.stopUpdatePosition()
                _controller?.onPlayOrPause(false)
                _onPlayerListener?.onCompletion()
            }
            PlayerState.ERROR -> {
                d("Player error:$errorMessage")
                playerState[0] = state
                keepScreenOn = false
                _player?.let {
                    _currentRetryPosition = it.getCurrentPosition()
                }
                _controller?.stopUpdatePosition()
                _controller?.onPlayOrPause(false)
                _onPlayerListener?.onError(errorMessage)
            }
        }
    }

    override fun onPlayerState(): PlayerState = playerState[0]

    override fun onRenderCreated(surface: Surface) {
        _player?.setSurface(surface)
    }

    override fun onRenderChanged(width: Int, height: Int) {
        //NO OP
    }

    override fun onRenderDestroyed() {
        if (_renderer is SurfaceView) {
            _player?.setSurface(null)
        }
    }

    override fun onPrepare(playWhenPrepared: Boolean) {
        _playWhenPrepared = playWhenPrepared
        _player?.prepare()
        _player?.statePreparing()
    }

    override fun onPlayOrPause(pauseFromUser: Boolean) {
        if (getPlayerState() == PlayerState.ERROR) return
        if (getPlayerState() == PlayerState.INITIALIZED) {
            onPrepare(true)
        } else {
            _player?.let {
                if (it.isPlaying()) {
                    _pauseFromUser = pauseFromUser
                    pause()
                } else {
                    _pauseFromUser = false
                    if (getPlayerState() == PlayerState.COMPLETION) {
                        onSeekTo(Long.MIN_VALUE.toInt() + 1)
                    }
                    start()
                }
            }
        }
    }

    override fun onTouchSpeed(speed: Float, isTouch: Boolean) {
        setSpeed(speed)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onFullScreenChanged() {
        if (_isFullScreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            activityContentView.removeView(playerView)
            addView(playerView)
            showSystemBar()
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            removeView(playerView)
            activityContentView.addView(
                playerView, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )
            hideSystemBar()
        }
        _onPlayerListener?.onScreenChanged(_isFullScreen)
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
            _controller?.onBufferPosition(it.getBufferPosition())
        }
    }

    override fun onReplay() {
        onPlayOrPause()
    }

    override fun onRetry() {
        reset()
        setDataSource(_currentDataSource)
        onPrepare(true)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE && _isFullScreen) {
            hideSystemBar()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showSystemBar() {
        _isFullScreen = false
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        activity.window.decorView.systemUiVisibility = uiOptions
        activity.supportActionBar?.let {
            it.setShowHideAnimationEnabled(false)
            it.show()
        }
    }

    private fun hideSystemBar() {
        _isFullScreen = true
        hideStatusBar()
        hideNavigationBar()
    }

    @SuppressLint("RestrictedApi")
    private fun hideStatusBar() {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val uiOptions = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        activity.window.decorView.systemUiVisibility = uiOptions
        activity.supportActionBar?.let {
            it.setShowHideAnimationEnabled(false)
            it.hide()
        }
    }

    private fun hideNavigationBar() {
        val uiOptions = activity.window.decorView.systemUiVisibility
        activity.window.decorView.systemUiVisibility =
            uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    /*public function*/
    fun setPlayer(player: Player) {
        if (_player == null) {
            _player = player
            player.setListener(this)
            player.stateIdle()
        }
    }

    fun setController(controller: Controller) {
        if (_controller == null) {
            _controller = controller
            controller.setListener(this)
            playerView.addView(controller.getView())
        }
    }

    fun setOnPlayerListener(listener: OnPlayerListener.() -> Unit) {
        val playerListener = OnPlayerListener()
        playerListener.listener()
        _onPlayerListener = playerListener
    }

    fun setRenderMode(mode: RenderMode) {
        _renderer.setRenderMode(mode)
    }

    fun setDataSource(source: String) {
        _currentDataSource = source
        _player?.setDataSource(source)
        _player?.stateInitialized()
    }

    fun prepare(playWhenPrepared: Boolean = false) {
        onPrepare(playWhenPrepared)
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

    fun seekTo(position: Int) {
        onSeekTo(position)
    }

    fun setSpeed(speed: Float) {
        _player?.setSpeed(speed)
    }

    fun replay() {
        onReplay()
    }

    fun retry() {
        onRetry()
    }

    fun reset() {
        _player?.reset()
        _player?.stateIdle()
    }

    fun isPlaying(): Boolean {
        if (getPlayerState() == PlayerState.ERROR) return false
        _player?.let {
            return it.isPlaying()
        }
        return false
    }

    fun getCurrentPosition(): Int {
        if (getPlayerState() == PlayerState.ERROR) return 0
        _player?.let {
            return it.getCurrentPosition()
        }
        return 0
    }

    fun getDuration(): Int {
        if (getPlayerState() == PlayerState.IDLE ||
            getPlayerState() == PlayerState.INITIALIZED ||
            getPlayerState() == PlayerState.PREPARING ||
            getPlayerState() == PlayerState.ERROR
        ) return 0
        _player?.let {
            return it.getDuration()
        }
        return 0
    }

    fun getVideoWidth(): Int {
        if (getPlayerState() == PlayerState.ERROR) return 0
        _player?.let {
            return it.getVideoWidth()
        }
        return 0
    }

    fun getVideoHeight(): Int {
        if (getPlayerState() == PlayerState.ERROR) return 0
        _player?.let {
            return it.getVideoHeight()
        }
        return 0
    }

    fun getPlayerState(): PlayerState = onPlayerState()

    fun release() {
        _player?.setSurface(null)
        _player?.release()
        _controller?.release()
        _renderer.release()
        d("Player released")
    }
    /*public function end*/

}