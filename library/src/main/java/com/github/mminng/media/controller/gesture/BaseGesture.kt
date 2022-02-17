package com.github.mminng.media.controller.gesture

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Created by zh on 2022/1/31.
 */
abstract class BaseGesture @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Gesture, View.OnTouchListener {

    private var _listener: Gesture.Listener? = null
    private val gestureDetector: GestureDetector

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
                    val vibrate: Vibrator =
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (vibrate.hasVibrator()) {
                        vibrate.cancel()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrate.vibrate(
                                VibrationEffect.createOneShot(
                                    20,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        } else {
                            vibrate.vibrate(20)
                        }
                    }
                    _listener?.onLongPress()
                    super.onLongPress(e)
                }
            })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun setListener(listener: Gesture.Listener) {
        if (_listener === listener) return
        _listener = listener
    }

    override fun getView(): View = this

}