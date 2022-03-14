package com.github.mminng.media

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.provider.Settings
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.github.mminng.media.controller.Controller
import com.github.mminng.media.player.OnPlayerListener
import com.github.mminng.media.player.Player
import com.github.mminng.media.player.PlayerOrientation
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
@Suppress("DEPRECATION")
class PlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Player.Listener, Renderer.Listener, Controller.Listener {

    private val playerView: FrameLayout = FrameLayout(context, attrs)
    private val playerState: MutableList<PlayerState> = mutableListOf(PlayerState.IDLE)
    private var _player: Player? = null
    private var _renderer: Renderer
    private var _controller: Controller? = null
    private var _isFullscreen: Boolean = false
    private var _isLandscape: Boolean = false
    private var _isPortrait: Boolean = true
    private var _needFinish: Boolean = false
    private val orientationFromInit: Int
    private var _fullscreenOrientation: PlayerOrientation = PlayerOrientation.LANDSCAPE
    private var _fullscreenOrientationFromUser: PlayerOrientation = PlayerOrientation.NONE
    private val orientationListener: OrientationEventListener
    private var _playWhenPrepared: Boolean = false
    private var _pauseFromUser: Boolean = false
    private var _currentDataSource: String = ""
    private var _currentRetryPosition: Long = 0
    private var _isSpeedAlreadySet: Boolean = true
    private var _saveSpeed: Float = 1.0F
    private var _playerListener: OnPlayerListener? = null
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioFocusListener: AudioManager.OnAudioFocusChangeListener
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
        activity.window.decorView.setOnSystemUiVisibilityChangeListener {
            if (_isFullscreen && it == View.SYSTEM_UI_FLAG_VISIBLE) {
                hideSystemBar()
            }
        }
        audioFocusListener = AudioManager.OnAudioFocusChangeListener {
            when (it) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    d("Audio focus loss")
                    pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    d("Audio focus loss")
                    pause()
                }
            }
        }
        orientationFromInit = activity.requestedOrientation
        orientationListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientationFromInit == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                    _fullscreenOrientation == PlayerOrientation.PORTRAIT
                ) return
                val autoRotateOn: Boolean = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    0
                ) == 1
                if (!autoRotateOn) return
                if ((orientation in 0..20) || orientation >= 340) {
                    //portrait screen
                    _isLandscape = false
                    if (_isPortrait) return
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) return
                    requireFullscreen(
                        fullscreen = false,
                        fromUser = false,
                        shouldChangePlayer = _isFullscreen
                    )
                } else if (orientation in 240..300) {
                    //landscape screen
                    _isPortrait = false
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                    if (_isLandscape) return
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) return
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        requireFullscreen(
                            fullscreen = true,
                            fromUser = false,
                            shouldChangePlayer = !_isFullscreen
                        )
                    }
                } else if (orientation in 60..120) {
                    //reverse landscape screen
                    _isPortrait = false
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    }
                    if (_isLandscape) return
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) return
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    } else {
                        requireFullscreen(
                            fullscreen = true,
                            fromUser = false,
                            reverseLandscape = true,
                            shouldChangePlayer = !_isFullscreen
                        )
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            e("newConfig=横屏")
        } else {
            e("newConfig=竖屏")
        }
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        d("video_width:$width")
        d("video_height:$height")
        _renderer.setVideoSize(width, height)
    }

    override fun onPlayerStateChanged(state: PlayerState, error: String) {
        _controller?.onPlayerStateChanged(state, error)
        when (state) {
            PlayerState.IDLE -> {
                d("Player idle")
                playerState[0] = state
                _controller?.stopUpdatePosition()
                _controller?.onPlayingChanged(false)
            }
            PlayerState.INITIALIZED -> {
                d("Player initialized")
                playerState[0] = state
                _controller?.let {
                    if (_currentRetryPosition == 0L && it.isReady()) {
                        it.onPosition(0)
                        it.onBufferedPosition(0)
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
                _playerListener?.onPrepared()
                _player?.let {
                    _controller?.onDuration(it.getDuration())
                    _fullscreenOrientation = if (it.getVideoHeight() >= it.getVideoWidth())
                        PlayerOrientation.PORTRAIT else PlayerOrientation.LANDSCAPE
                }
                if (_fullscreenOrientationFromUser == PlayerOrientation.UNSPECIFIED) {
                    requireFullscreen(
                        fullscreen = true,
                        fullscreenOrientation = _fullscreenOrientation,
                        fromUser = false,
                        shouldChangePlayer = !_isFullscreen
                    )
                }
                if (_currentRetryPosition > 0) {
                    onSeekTo(_currentRetryPosition)
                    _currentRetryPosition = 0
                }
                if (_playWhenPrepared) {
                    start()
                } else {
                    _pauseFromUser = true
                }
            }
            PlayerState.BUFFERING -> {
                d("Player buffering start")
                keepScreenOn = true
            }
            PlayerState.BUFFERED -> {
                d("Player buffering end")
            }
            PlayerState.RENDERING -> {
                d("Player rendering")
            }
            PlayerState.STARTED -> {
                d("Player started")
                playerState[0] = state
                _controller?.updatePosition()
                _controller?.onPlayingChanged(true)
                _playerListener?.onStarted()
            }
            PlayerState.PAUSED -> {
                d("Player paused")
                playerState[0] = state
                _controller?.stopUpdatePosition()
                _controller?.onPlayingChanged(false)
                _playerListener?.onPaused()
            }
            PlayerState.COMPLETION -> {
                d("Player completed")
                playerState[0] = state
                _pauseFromUser = true
                _controller?.stopUpdatePosition()
                _controller?.onPosition(getDuration())
                _controller?.onPlayingChanged(false)
                _playerListener?.onCompletion()
            }
            PlayerState.ERROR -> {
                e("Player error:$error")
                playerState[0] = state
                keepScreenOn = false
                _player?.let { _currentRetryPosition = it.getPosition() }
                _controller?.stopUpdatePosition()
                _controller?.onPlayingChanged(false)
                _playerListener?.onError(error)
            }
        }
        _player?.let {
            if (state != PlayerState.ERROR &&
                state != PlayerState.BUFFERING &&
                state != PlayerState.PREPARING
            ) keepScreenOn = it.isPlaying()
        }
    }

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

    override fun onPlayerBack() {
        if (_isFullscreen && !_needFinish) {
            requireFullscreen(fullscreen = false)
        } else {
            activity.finish()
        }
    }

    override fun onPrepare(playWhenPrepared: Boolean) {
        _playWhenPrepared = playWhenPrepared
        _player?.prepare()
        _player?.statePreparing()
    }

    override fun onPlayOrPause(pauseFromUser: Boolean) {
        if (getPlayerState() == PlayerState.ERROR) return
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

    override fun onSeekTo(position: Long) {
        if (getPlayerState() == PlayerState.IDLE ||
            getPlayerState() == PlayerState.INITIALIZED ||
            getPlayerState() == PlayerState.PREPARING ||
            getPlayerState() == PlayerState.ERROR
        ) return
        _player?.seekTo(position)
    }

    override fun onChangeSpeed(speed: Float) {
        setSpeed(speed)
    }

    override fun onTouchSpeed(isTouch: Boolean) {
        if (isTouch) {
            _saveSpeed = getSpeed()
            setSpeed(2.0F)
        } else {
            setSpeed(_saveSpeed)
        }
    }

    override fun onScreenChanged() {
        if (_isFullscreen) {
            requireFullscreen(fullscreen = false)
        } else {
            if (_fullscreenOrientationFromUser == PlayerOrientation.NONE ||
                _fullscreenOrientationFromUser == PlayerOrientation.UNSPECIFIED
            ) {
                requireFullscreen(
                    fullscreen = true,
                    fullscreenOrientation = _fullscreenOrientation
                )
            } else {
                requireFullscreen(
                    fullscreen = true,
                    fullscreenOrientation = _fullscreenOrientationFromUser
                )
            }
        }
    }

    override fun onChangeRenderMode(renderMode: RenderMode) {
        setRenderMode(renderMode)
    }

    override fun onReplay() {
        onPlayOrPause()
    }

    override fun onRetry() {
        reset()
        setDataSource(_currentDataSource)
        onPrepare(true)
    }

    override fun onPosition() {
        _player?.let {
            _controller?.onPosition(it.getPosition())
            _controller?.onBufferedPosition(it.getBufferedPosition())
        }
    }

    override fun requirePlayerState(): PlayerState = getPlayerState()

    override fun requirePosition(): Long = getPosition()

    override fun requireDuration(): Long = getDuration()

    @SuppressLint("SourceLockedOrientationActivity")
    private fun requireFullscreen(
        fullscreen: Boolean,
        fullscreenOrientation: PlayerOrientation = PlayerOrientation.LANDSCAPE,
        fromUser: Boolean = true,
        reverseLandscape: Boolean = false,
        shouldChangePlayer: Boolean = true
    ) {
        if (fromUser) {
            _isLandscape = true
            _isPortrait = true
        }
        if (fullscreen) {
            when (fullscreenOrientation) {
                PlayerOrientation.LANDSCAPE -> {
                    activity.requestedOrientation =
                        if (reverseLandscape) ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                PlayerOrientation.PORTRAIT -> {
                    if (orientationFromInit != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
                PlayerOrientation.NONE,
                PlayerOrientation.UNSPECIFIED -> {
                    //NO OP
                }
            }
            if (!shouldChangePlayer) return
            removeView(playerView)
            activityContentView.addView(
                playerView, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )
            hideSystemBar()
        } else {
            if (orientationFromInit != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            if (!shouldChangePlayer) return
            activityContentView.removeView(playerView)
            addView(playerView)
            showSystemBar()
        }
        _controller?.onScreenChanged(_isFullscreen)
        _playerListener?.onScreenChanged(_isFullscreen)
        d("fullscreen=$_isFullscreen")
    }

    @SuppressLint("RestrictedApi")
    private fun showSystemBar() {
        _isFullscreen = false
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        activity.window.decorView.systemUiVisibility = uiOptions
        activity.supportActionBar?.let {
            it.setShowHideAnimationEnabled(false)
            it.show()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hideSystemBar() {
        _isFullscreen = true
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        activity.window.decorView.systemUiVisibility = uiOptions
        activity.supportActionBar?.let {
            it.setShowHideAnimationEnabled(false)
            it.hide()
        }
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
        _playerListener = playerListener
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
        if (!_isSpeedAlreadySet) {
            _player?.setSpeed(_saveSpeed)
            _isSpeedAlreadySet = true
        }
        audioManager.requestAudioFocus(
            audioFocusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
        _player?.start()
        _player?.stateStarted()
    }

    fun pause() {
        if (getPlayerState() == PlayerState.ERROR) return
        _player?.let {
            if (it.isPlaying()) {
                audioManager.abandonAudioFocus(audioFocusListener)
                it.pause()
                it.statePaused()
            }
        }
    }

    fun seekTo(position: Long) {
        onSeekTo(position)
    }

    fun setSpeed(speed: Float) {
        if (getPlayerState() != PlayerState.STARTED) {
            _saveSpeed = speed
            _isSpeedAlreadySet = false
        } else {
            _player?.setSpeed(speed)
            _isSpeedAlreadySet = true
        }
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

    fun canBack(): Boolean {
        _controller?.let {
            return if (!it.canBack()) {
                false
            } else if (_isFullscreen && !_needFinish) {
                requireFullscreen(false)
                false
            } else {
                true
            }
        }
        return true
    }

    fun setOrientationApplySystem(applySystem: Boolean) {
        if (applySystem) {
            orientationListener.enable()
        } else {
            orientationListener.disable()
        }
    }

    fun setBackNeedFinish(needFinish: Boolean) {
        _needFinish = needFinish
    }

    fun setFullscreen(
        fullscreen: Boolean,
        fullscreenOrientation: PlayerOrientation = PlayerOrientation.UNSPECIFIED,
    ) {
        _fullscreenOrientationFromUser = fullscreenOrientation
        _controller?.let {
            if (it.isReady()) {
                if (fullscreen == _isFullscreen) return
                requireFullscreen(
                    fullscreen = fullscreen,
                    fullscreenOrientation = fullscreenOrientation,
                )
            } else {
                it.setReadyListener {
                    if (fullscreen == _isFullscreen) return@setReadyListener
                    requireFullscreen(
                        fullscreen = fullscreen,
                        fullscreenOrientation = fullscreenOrientation,
                    )
                }
            }
        }
        if (_controller == null) {
            if (fullscreen == _isFullscreen) return
            requireFullscreen(
                fullscreen = fullscreen,
                fullscreenOrientation = fullscreenOrientation,
            )
        }
    }

    fun isPlaying(): Boolean {
        if (getPlayerState() == PlayerState.ERROR) return false
        _player?.let {
            return it.isPlaying()
        }
        return false
    }

    fun getPosition(): Long {
        if (getPlayerState() == PlayerState.ERROR) return 0
        _player?.let {
            return it.getPosition()
        }
        return 0
    }

    fun getDuration(): Long {
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

    fun getSpeed(): Float {
        _player?.let {
            return it.getSpeed()
        }
        return 1.0F
    }

    fun getPlayerState(): PlayerState = playerState[0]

    fun release() {
        _player?.setSurface(null)
        _player?.release()
        _controller?.release()
        _renderer.release()
        orientationListener.disable()
        audioManager.abandonAudioFocus(audioFocusListener)
        d("Player released")
    }
    /*public function end*/
}