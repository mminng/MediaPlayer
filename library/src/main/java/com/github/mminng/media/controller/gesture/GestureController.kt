package com.github.mminng.media.controller.gesture

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Vibrator
import android.provider.Settings
import android.util.AttributeSet
import android.view.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Created by zh on 2022/1/31.
 */
class GestureController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Gesture, View.OnTouchListener {

    companion object {
        private const val MOTION_NONE: Int = -1
        private const val MOTION_PROGRESS: Int = 0
        private const val MOTION_VOLUME: Int = 1
        private const val MOTION_BRIGHTNESS: Int = 2
    }

    private val gestureDetector: GestureDetector
    private var _listener: Gesture.Listener? = null
    private var _gestureEnable: Boolean = true
    private var _hasLongTap: Boolean = false

    //restore play speed
    private var _shouldRestore: Boolean = true
    private var _canMove = false
    private var _motionType = MOTION_NONE
    private var _viewWidth: Int = 0
    private var _startX: Float = 0F
    private var _startY: Float = 0F
    private var _moveX: Float = 0F
    private var _moveY: Float = 0F
    private var _seekOffset: Int = 0
    private var _currentPosition: Int = 0
    private var _duration: Int = 0
    private var _currentBrightness: Float = 0.0F
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var _currentVolume: Int = 0
    private var _maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

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

                override fun onLongPress(e: MotionEvent?) {
                    if (_gestureEnable) {
                        val vibrate: Vibrator =
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (vibrate.hasVibrator()) {
                            vibrate.cancel()
                            vibrate.vibrate(30)
                        }
                        _hasLongTap = true
                        _shouldRestore = true
                        _listener?.onLongTap(isTouch = true)
                    }
                    super.onLongPress(e)
                }
            })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (!_gestureEnable) return gestureDetector.onTouchEvent(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                _viewWidth = width
                _startX = event.x
                _startY = event.y
                _seekOffset = 0
                _listener?.let {
                    _currentPosition = it.getCurrentPosition()
                    _duration = it.getDuration()
                }
                _currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                _currentBrightness = getScreenBrightness()
                _canMove = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (_hasLongTap) return gestureDetector.onTouchEvent(event)
                _moveX = event.x - _startX
                _moveY = event.y - _startY
                if (_moveX.absoluteValue < 50 && _moveY.absoluteValue < 50 && !_canMove) {
                    return gestureDetector.onTouchEvent(event)
                } else {
                    _canMove = true
                }
                if (_motionType == MOTION_NONE) {
                    _motionType = if (_moveX.absoluteValue > _moveY.absoluteValue) {
                        MOTION_PROGRESS
                    } else {
                        if (_startX < _viewWidth / 2) {
                            MOTION_BRIGHTNESS
                        } else {
                            MOTION_VOLUME
                        }
                    }
                }
                when (_motionType) {
                    MOTION_PROGRESS -> {
                        if (_moveX.absoluteValue < 15) return gestureDetector.onTouchEvent(event)
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
                        _listener?.onSwipeProgressView(
                            true,
                            _seekOffset + _currentPosition,
                            _duration
                        )
                    }
                    MOTION_BRIGHTNESS -> {
                        if (_moveY.absoluteValue < 10) return gestureDetector.onTouchEvent(event)
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
                        setScreenBrightness(_currentBrightness)
                        _listener?.onSwipeBrightnessView(
                            true,
                            (_currentBrightness * 100).roundToInt(),
                            100
                        )
                    }
                    MOTION_VOLUME -> {
                        if (_moveY.absoluteValue < 20) return gestureDetector.onTouchEvent(event)
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
                        if (_currentVolume > _maxVolume) {
                            _currentVolume = _maxVolume
                        }
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, _currentVolume, 0)
                        _listener?.onSwipeVolumeView(true, _currentVolume, _maxVolume)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (_hasLongTap) {
                    _listener?.onLongTap(isTouch = false, restoreSpeed = _shouldRestore)
                }
                _listener?.onSwipeProgressView(
                    false,
                    _seekOffset + _currentPosition,
                    _duration,
                    canSeek = _motionType == MOTION_PROGRESS
                )
                _listener?.onSwipeVolumeView(
                    false,
                    _maxVolume,
                    _currentVolume
                )
                _listener?.onSwipeBrightnessView(
                    false,
                    (_currentBrightness * 100).roundToInt(),
                    100
                )
                _hasLongTap = false
                _motionType = MOTION_NONE
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    override fun setListener(listener: Gesture.Listener) {
        if (_listener === listener) return
        _listener = listener
    }

    override fun getView(): View = this

    override fun setGestureEnable(enable: Boolean) {
        _gestureEnable = enable
    }

    override fun getGestureEnable(): Boolean = _gestureEnable

    override fun setRestoreSpeed(shouldRestore: Boolean) {
        _shouldRestore = shouldRestore
    }

    private fun setScreenBrightness(brightness: Float) {
        val window = (context as Activity).window
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }

    private fun getScreenBrightness(): Float {
        val sysBrightness =
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        val currentBrightness: Float = (context as Activity).window.attributes.screenBrightness
        if (currentBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            return sysBrightness / 255F
        }
        return currentBrightness
    }

}