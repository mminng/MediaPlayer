package com.easyfun.mediaplayer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.github.mminng.media.controller.BaseController
import com.github.mminng.media.utils.convertMillis

/**
 * Created by zh on 2021/9/20.
 */
class MyControllerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseController(context, attrs), View.OnClickListener,
    SeekBar.OnSeekBarChangeListener {

    private val playPauseView: ImageView by lazy {
        findViewById(R.id.media_play_pause)
    }
    private val timeBar: SeekBar by lazy {
        findViewById(R.id.media_seekbar)
    }
    private val positionView: TextView by lazy {
        findViewById(R.id.media_position)
    }
    private val durationView: TextView by lazy {
        findViewById(R.id.media_duration)
    }
    private val fullScreen: ImageView by lazy {
        findViewById(R.id.media_fullScreen)
    }

    private var seekFromUser: Boolean = false

    override fun setControllerLayout(): Int {
        return R.layout.my_control_view
    }

    override fun onLayoutCreated(view: View) {
        //also you can do findViewById() here
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
            controllerListener?.onSeekTo(it.progress)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            playPauseView -> {
                controllerListener?.onPlayOrPause(true)
            }
            fullScreen -> {
                controllerListener?.onScreenChanged()
            }
            else -> {
                //NO OP
            }
        }
    }

    override fun onPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            playPauseView.setImageResource(R.drawable.ic_action_playing)
        } else {
            playPauseView.setImageResource(R.drawable.ic_action_paused)
        }
    }

    override fun onScreenChanged(isFullScreen: Boolean) {
        if (isFullScreen) {
            fullScreen.setImageResource(R.drawable.ic_action_fullscreen_exit)
        } else {
            fullScreen.setImageResource(R.drawable.ic_action_fullscreen)
        }
    }

    override fun onCompletion() {
    }

    override fun onPlayerError(errorMessage: String) {
    }

    override fun onDuration(duration: Int) {
        durationView.text = convertMillis(duration)
        timeBar.max = duration
    }

    override fun onCurrentPosition(position: Int) {
        if (!seekFromUser) {
            positionView.text = convertMillis(position)
            timeBar.progress = position
        }
    }

    override fun onBufferPosition(position: Int) {
        timeBar.secondaryProgress = position
    }

    override fun onShowController() {
        TODO("Not yet implemented")
    }

    override fun onHideController() {
        TODO("Not yet implemented")
    }
}