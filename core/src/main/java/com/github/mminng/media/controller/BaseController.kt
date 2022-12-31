package com.github.mminng.media.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.github.mminng.media.R
import com.github.mminng.media.controller.gesture.DefaultGesture
import com.github.mminng.media.controller.gesture.Gesture
import com.github.mminng.media.player.PlayerState
import com.github.mminng.media.renderer.RenderMode

/**
 * Created by zh on 2021/12/9.
 */
abstract class BaseController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Controller, Gesture.Listener {

    private var _updateInterval: Long = 1000
    private var _visibilityInterval: Long = 5 * 1000
    private var _controllerIsReady: Boolean = false
    private var _controllerIsVisible: Boolean = false
    private var _coverViewEnable: Boolean = false
    private var _shouldHideCover: Boolean = false
    private var _completionViewEnable: Boolean = false
    private var _errorViewEnable: Boolean = false
    private var _bindCoverListener: ((url: String, view: ImageView) -> Unit)? = null
    private var _isControllerReadyListener: (() -> Unit)? = null
    private var _listener: Controller.Listener? = null
    private val gestureController: Gesture = DefaultGesture(context, attrs)

    private val controllerView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setControllerLayout(), true)
    }
    private val seekView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setSwipeSeekLayout())
    }
    private val brightnessView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setSwipeBrightnessLayout())
    }
    private val volumeView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setSwipeVolumeLayout())
    }
    private val longTouchView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setTouchSpeedLayout())
    }
    private val stateCoverView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setCoverLayout())
    }
    private val stateBufferingView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setBufferingLayout())
    }
    private val stateCompletionView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setCompletionLayout())
    }
    private val stateErrorView: View by lazy(LazyThreadSafetyMode.NONE) {
        inflateLayout(setErrorLayout())
    }

    private val progressRunnable: Runnable = Runnable {
        updatePosition()
    }
    private val visibilityRunnable: Runnable = Runnable {
        hideController()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (_controllerIsReady) return
        onLayoutCreated(controllerView)
        addView(gestureController.getView(), 0)
        addView(stateBufferingView, 0)
        addView(seekView)
        addView(brightnessView)
        addView(volumeView)
        addView(longTouchView)
        addView(stateCompletionView)
        addView(stateErrorView)
        addView(stateCoverView)
        seekView.visibility = INVISIBLE
        brightnessView.visibility = INVISIBLE
        volumeView.visibility = INVISIBLE
        longTouchView.visibility = INVISIBLE
        stateBufferingView.visibility = INVISIBLE
        stateCompletionView.visibility = GONE
        stateErrorView.visibility = GONE
        stateCoverView.visibility = if (_coverViewEnable && !_shouldHideCover) VISIBLE else GONE
        gestureController.setListener(this)
        _controllerIsReady = true
        _isControllerReadyListener?.invoke()
    }

    @LayoutRes
    abstract fun setControllerLayout(): Int

    abstract fun onLayoutCreated(view: View)

    override fun onPlayerStateChanged(state: PlayerState, error: String) {
        when (state) {
            PlayerState.IDLE -> {
            }
            PlayerState.INITIALIZED -> {
            }
            PlayerState.PREPARING -> {
                stateBufferingView.visibility = VISIBLE
            }
            PlayerState.PREPARED -> {
                stateBufferingView.visibility = INVISIBLE
                showController()
            }
            PlayerState.BUFFERING -> {
                stateBufferingView.visibility = VISIBLE
            }
            PlayerState.BUFFERED -> {
                stateBufferingView.visibility = INVISIBLE
            }
            PlayerState.RENDERING -> {
            }
            PlayerState.STARTED -> {
            }
            PlayerState.PAUSED -> {
            }
            PlayerState.COMPLETION -> {
                if (_completionViewEnable) {
                    stateCompletionView.visibility = VISIBLE
                    hideController()
                } else {
                    showController(false)
                }
                onCompletion()
            }
            PlayerState.ERROR -> {
                if (_errorViewEnable) {
                    stateErrorView.visibility = VISIBLE
                    hideController()
                } else {
                    showController(false)
                }
                onPlayerError(error)
            }
        }
    }

    final override fun hideCover() {
        _bindCoverListener?.let {
            _shouldHideCover = true
            if (isReady() && getCoverView().visibility != GONE) {
                getCoverView().visibility = GONE
            }
        }
    }

    final override fun playerBack() {
        _listener?.onPlayerBack()
    }

    final override fun prepare(playWhenPrepared: Boolean) {
        _listener?.onPrepare(playWhenPrepared)
    }

    final override fun playOrPause(pauseFromUser: Boolean) {
        _listener?.onPlayOrPause(pauseFromUser)
    }

    final override fun seekTo(position: Long) {
        _listener?.onSeekTo(position)
    }

    final override fun changeSpeed(speed: Float) {
        _listener?.onChangeSpeed(speed)
    }

    final override fun screenChanged() {
        _listener?.onScreenChanged()
    }

    final override fun changeRenderMode(renderMode: RenderMode) {
        _listener?.onChangeRenderMode(renderMode)
    }

    final override fun replay() {
        _listener?.onReplay()
    }

    final override fun retry() {
        _listener?.onRetry()
    }

    final override fun setReadyListener(listener: () -> Unit) {
        if (_isControllerReadyListener == null && !isReady()) {
            _isControllerReadyListener = listener
        }
    }

    final override fun isReady(): Boolean = _controllerIsReady

    override fun canBack(): Boolean = true

    final override fun getPlayerState(): PlayerState {
        _listener?.let {
            return it.requirePlayerState()
        }
        return PlayerState.IDLE
    }

    final override fun getView(): View = this

    final override fun updatePosition() {
        _listener?.onPosition()
        postDelayed(progressRunnable, _updateInterval)
    }

    final override fun stopUpdatePosition() {
        removeCallbacks(progressRunnable)
    }

    override fun release() {
        removeCallbacks(progressRunnable)
        removeCallbacks(visibilityRunnable)
    }

    final override fun setListener(listener: Controller.Listener) {
        if (_listener === listener) return
        _listener = listener
    }

    /*Gesture Listener*/
    override fun onSingleTap() {
        if (_controllerIsVisible) {
            hideController()
        } else {
            showController()
        }
    }

    override fun onDoubleTap() {
        if (_controllerIsVisible) {
            removeCallbacks(visibilityRunnable)
            postDelayed(visibilityRunnable, _visibilityInterval)
        }
        _listener?.onPlayOrPause(true)
    }

    override fun onLongTap(touching: Boolean) {
        if (_controllerIsVisible && touching) {
            hideController()
        }
        _listener?.onTouchSpeed(touching)
    }

    final override fun getPosition(): Long {
        _listener?.let {
            return it.requirePosition()
        }
        return 0
    }

    final override fun getDuration(): Long {
        _listener?.let {
            return it.requireDuration()
        }
        return 0
    }
    /*Gesture Listener*/

    override fun setSwipeSeekLayout(): Int {
        return R.layout.default_swipe_seek_layout
    }

    override fun setSwipeBrightnessLayout(): Int {
        return R.layout.default_swipe_brightness_layout
    }

    override fun setSwipeVolumeLayout(): Int {
        return R.layout.default_swipe_volume_layout
    }

    override fun setTouchSpeedLayout(): Int {
        return R.layout.default_touch_speed_layout
    }

    override fun setCoverLayout(): Int {
        return R.layout.default_state_cover_layout
    }

    override fun setBufferingLayout(): Int {
        return R.layout.default_state_buffering_layout
    }

    override fun setCompletionLayout(): Int {
        return R.layout.default_state_completion_layout
    }

    override fun setErrorLayout(): Int {
        return R.layout.default_state_error_layout
    }

    private fun inflateLayout(@LayoutRes layout: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layout, this, attachToRoot)
    }

    fun getBufferingView(): View = stateBufferingView

    fun getCoverView(): View = stateCoverView

    fun getCompletionView(): View = stateCompletionView

    fun getErrorView(): View = stateErrorView

    fun getSwipeSeekView(): View = seekView

    fun getSwipeBrightnessView(): View = brightnessView

    fun getSwipeVolumeView(): View = volumeView

    fun getTouchSpeedView(): View = longTouchView

    fun onBindCover(url: String, listener: (url: String, view: ImageView) -> Unit) {
        if (_bindCoverListener === listener) return
        _bindCoverListener = listener
        _coverViewEnable = true
        if (isReady() && getCoverView().visibility != VISIBLE) {
            stateCoverView.visibility = VISIBLE
        }
        setCover(url)
    }

    private fun setCover(url: String) {
        _bindCoverListener?.invoke(url, requireCover())
    }

    fun showCover() {
        if (getPlayerState() != PlayerState.COMPLETION) return
        if (isReady() && getCoverView().visibility != VISIBLE) {
            getCoverView().visibility = VISIBLE
        }
    }

    fun setGestureSeekEnable(enable: Boolean) {
        gestureController.setGestureSeekEnable(enable)
    }

    fun setGestureEnable(enable: Boolean) {
        gestureController.setGestureEnable(enable)
    }

    fun getGestureEnable(): Boolean = gestureController.getGestureEnable()

    fun setCoverViewEnable(enable: Boolean) {
        _coverViewEnable = enable
        if (isReady()) {
            if (enable) {
                stateCoverView.visibility = VISIBLE
            } else {
                stateCoverView.visibility = GONE
            }
        }
    }

    fun setCompletionViewEnable(enable: Boolean) {
        _completionViewEnable = enable
        if (isReady()) {
            if (enable) {
                stateCompletionView.visibility = VISIBLE
            } else {
                stateCompletionView.visibility = GONE
            }
        }
    }

    fun setErrorViewEnable(enable: Boolean) {
        _errorViewEnable = enable
        if (isReady()) {
            if (enable) {
                stateErrorView.visibility = VISIBLE
            } else {
                stateErrorView.visibility = GONE
            }
        }
    }

    fun setUpdateInterval(millis: Long) {
        _updateInterval = millis
    }

    fun setVisibilityInterval(millis: Long) {
        _visibilityInterval = millis
    }

    fun showController(shouldHide: Boolean = true) {
        onShowController()
        _controllerIsVisible = true
        removeCallbacks(visibilityRunnable)
        if (shouldHide) {
            postDelayed(visibilityRunnable, _visibilityInterval)
        }
    }

    fun hideController() {
        onHideController()
        _controllerIsVisible = false
        removeCallbacks(visibilityRunnable)
    }
}