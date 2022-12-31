package com.github.mminng.media.controller.gesture

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Vibrator
import android.provider.Settings
import android.util.AttributeSet
import android.view.*
import com.github.mminng.media.player.PlayerState
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Created by zh on 2022/1/31.
 */
class DefaultGesture @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Gesture, View.OnTouchListener {

    companion object {
        private const val MOTION_NONE: Int = -1
        private const val MOTION_SEEK: Int = 0
        private const val MOTION_VOLUME: Int = 1
        private const val MOTION_BRIGHTNESS: Int = 2
    }

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val window: Window = (context as Activity).window
    private val windowParams: WindowManager.LayoutParams = window.attributes
    private val gestureDetector: GestureDetector
    private var _listener: Gesture.Listener? = null
    private var _gestureEnable: Boolean = true
    private var _gestureSeekEnable: Boolean = true
    private var _hasLongTap: Boolean = false
    private var _canMove = false
    private var _motionType = MOTION_NONE
    private var _viewWidth: Int = 0
    private var _startX: Float = 0F
    private var _startY: Float = 0F
    private var _moveX: Float = 0F
    private var _moveY: Float = 0F
    private var _seekOffset: Int = 0
    private var _duration: Long = 0
    private var _currentPosition: Long = 0
    private var _currentBrightness: Float = 0.0F
    private var _currentVolume: Int = 0
    private val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    init {
        isClickable = true
        isFocusable = true
        gestureDetector = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    _listener?.onSingleTap()
                    return super.onSingleTapConfirmed(e)
                }

                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    _listener?.onDoubleTap()
                    return super.onDoubleTap(e)
                }

                @Suppress("DEPRECATION")
                override fun onLongPress(e: MotionEvent?) {
                    if (_gestureSeekEnable &&
                        _gestureEnable &&
                        _listener?.getPlayerState() == PlayerState.STARTED
                    ) {
                        val vibrator: Vibrator =
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (vibrator.hasVibrator()) {
                            vibrator.cancel()
                            vibrator.vibrate(30)
                        }
                        _hasLongTap = true
                        _listener?.onLongTap(true)
                    }
                    super.onLongPress(e)
                }
            })
        setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (!_gestureEnable) {
            return gestureDetector.onTouchEvent(event)
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                _viewWidth = width
                _startX = event.x
                _startY = event.y
                _seekOffset = 0
                _listener?.let {
                    _currentPosition = it.getPosition()
                    _duration = it.getDuration()
                }
                _currentVolume = getCurrentVolume()
                _currentBrightness = getCurrentBrightness()
                _canMove = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (_hasLongTap) {
                    return gestureDetector.onTouchEvent(event)
                }
                _moveX = event.x - _startX
                _moveY = event.y - _startY
                if (_moveX.absoluteValue < 50 && _moveY.absoluteValue < 50 && !_canMove) {
                    return gestureDetector.onTouchEvent(event)
                } else {
                    _canMove = true
                }
                if (_motionType == MOTION_NONE) {
                    _motionType = if (_moveX.absoluteValue > _moveY.absoluteValue) {
                        MOTION_SEEK
                    } else {
                        if (_startX < _viewWidth / 2) {
                            MOTION_BRIGHTNESS
                        } else {
                            MOTION_VOLUME
                        }
                    }
                }
                when (_motionType) {
                    MOTION_SEEK -> {
                        if (!_gestureSeekEnable || _moveX.absoluteValue < 15) {
                            return gestureDetector.onTouchEvent(event)
                        }
                        _startX = event.x
                        if (_moveX > 0) {
                            //forward
                            _seekOffset += 1000
                        } else {
                            //backward
                            _seekOffset -= 1000
                        }
                        if (_seekOffset + _currentPosition < 0) {
                            _seekOffset = 0
                            _currentPosition = 0
                        }
                        if (_seekOffset + _currentPosition > _duration) {
                            _seekOffset = 0
                            _currentPosition = _duration
                        }
                        _listener?.onSwipeSeekView(
                            showing = true,
                            position = _seekOffset + _currentPosition,
                            duration = _duration
                        )
                    }
                    MOTION_BRIGHTNESS -> {
                        if (_moveY.absoluteValue < 10) {
                            return gestureDetector.onTouchEvent(event)
                        }
                        _startY = event.y
                        if (_moveY > 0) {
                            //down
                            _currentBrightness -= 0.01F
                        } else {
                            //up
                            _currentBrightness += 0.01F
                        }
                        if (_currentBrightness > 1.0) {
                            _currentBrightness = 1.0F
                        }
                        if (_currentBrightness < 0.0) {
                            _currentBrightness = 0.0F
                        }
                        setBrightness(_currentBrightness)
                        _listener?.onSwipeBrightnessView(
                            showing = true,
                            position = (_currentBrightness * 100).roundToInt()
                        )
                    }
                    MOTION_VOLUME -> {
                        if (_moveY.absoluteValue < 20) {
                            return gestureDetector.onTouchEvent(event)
                        }
                        _startY = event.y
                        if (_moveY > 0) {
                            //down
                            _currentVolume -= 1
                        } else {
                            //up
                            _currentVolume += 1
                        }
                        if (_currentVolume < 0) {
                            _currentVolume = 0
                        }
                        if (_currentVolume > maxVolume) {
                            _currentVolume = maxVolume
                        }
                        setVolume(_currentVolume)
                        _listener?.onSwipeVolumeView(
                            showing = true,
                            position = _currentVolume,
                            max = maxVolume
                        )
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (_hasLongTap) {
                    _hasLongTap = false
                    _listener?.onLongTap(false)
                }
                when (_motionType) {
                    MOTION_SEEK -> {
                        _listener?.onSwipeSeekView(
                            showing = false,
                            position = _seekOffset + _currentPosition,
                            duration = _duration,
                            allowSeek = true
                        )
                    }
                    MOTION_BRIGHTNESS -> {
                        _listener?.onSwipeBrightnessView(
                            showing = false,
                            position = (_currentBrightness * 100).roundToInt()
                        )
                    }
                    MOTION_VOLUME -> {
                        _listener?.onSwipeVolumeView(
                            showing = false,
                            position = _currentVolume,
                            max = maxVolume
                        )
                    }
                }
                _motionType = MOTION_NONE
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    override fun setGestureEnable(enable: Boolean) {
        _gestureEnable = enable
    }

    override fun setGestureSeekEnable(enable: Boolean) {
        _gestureSeekEnable = enable
    }

    override fun getGestureEnable(): Boolean = _gestureEnable

    override fun getGestureSeekEnable(): Boolean = _gestureSeekEnable

    override fun getView(): View = this

    override fun setListener(listener: Gesture.Listener) {
        if (_listener === listener) return
        _listener = listener
    }

    private fun getCurrentBrightness(): Float {
        val sysBrightness =
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        val currentBrightness: Float = windowParams.screenBrightness
        if (currentBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            return sysBrightness / 255F
        }
        return currentBrightness
    }

    private fun setBrightness(brightness: Float) {
        windowParams.screenBrightness = brightness
        window.attributes = windowParams
    }

    private fun getCurrentVolume(): Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    private fun setVolume(volume: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }
}