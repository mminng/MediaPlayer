package com.github.mminng.media.controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.github.mminng.media.R
import com.github.mminng.media.utils.convertMillis

/**
 * Created by zh on 2021/9/20.
 */
class ControllerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Controller, View.OnClickListener,
    SeekBar.OnSeekBarChangeListener {

    private var playPauseView: ImageView
    private var timeBar: SeekBar
    private var positionView: TextView
    private var durationView: TextView
    private var fullScreen: ImageView

    private var controllerListener: Controller.OnControllerListener? = null
    private var seekFromUser: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.player_control_view, this)
        playPauseView = findViewById(R.id.media_play_pause)
        timeBar = findViewById(R.id.media_seekbar)
        positionView = findViewById(R.id.media_position)
        durationView = findViewById(R.id.media_duration)
        fullScreen = findViewById(R.id.media_fullScreen)
        playPauseView.setOnClickListener(this)
        fullScreen.setOnClickListener(this)
        timeBar.setOnSeekBarChangeListener(this)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        seekFromUser = true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            positionView.text = convertMillis(progress)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekFromUser = false
        seekBar?.let {
            controllerListener?.onProgressChanged(it.progress)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            playPauseView -> {
                controllerListener?.onPlayPause()
            }
            fullScreen -> {
                controllerListener?.onFullScreen()
            }
            else -> {
                //NO OP
            }
        }
    }

    override fun onPlayPause(isPlaying: Boolean) {
        if (isPlaying) {
            playPauseView.setImageResource(R.drawable.ic_action_playing)
        } else {
            playPauseView.setImageResource(R.drawable.ic_action_paused)
        }
    }

    override fun onFullScreen(isFullScreen: Boolean) {
        if (isFullScreen) {
            fullScreen.setImageResource(R.drawable.ic_action_fullscreen_exit)
        } else {
            fullScreen.setImageResource(R.drawable.ic_action_fullscreen)
        }
    }

    override fun onDuration(duration: Int) {
        durationView.text = convertMillis(duration)
        timeBar.max = duration
    }

    override fun onProgress(progress: Int) {
        if (!seekFromUser) {
            positionView.text = convertMillis(progress)
            timeBar.progress = progress
        }
    }

    override fun setOnControllerListener(listener: Controller.OnControllerListener) {
        if (controllerListener === listener) return
        controllerListener = listener
    }

}