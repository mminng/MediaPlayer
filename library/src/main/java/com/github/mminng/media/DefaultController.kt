package com.github.mminng.media

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
class DefaultController @JvmOverloads constructor(
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
    private val fullScreenView: ImageView by lazy {
        findViewById(R.id.media_fullScreen)
    }
    private val errorText: TextView by lazy {
        getErrorView().findViewById(R.id.default_error_message)
    }

    private var _seekFromUser: Boolean = false

    override fun setControllerLayout(): Int {
        return R.layout.default_controller_layout
    }

    override fun onLayoutCreated(view: View) {
        //also you can do findViewById() here
        playPauseView.setOnClickListener(this)
        fullScreenView.setOnClickListener(this)
        timeBar.setOnSeekBarChangeListener(this)
        bindCoverView()
        bindCompletionView()
        bindErrorView()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        _seekFromUser = true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            positionView.text = convertMillis(progress)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        _seekFromUser = false
        seekBar?.let {
            controllerListener?.onSeekTo(it.progress)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            playPauseView -> {
                controllerListener?.onPlayPause(true)
            }
            fullScreenView -> {
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
            fullScreenView.setImageResource(R.drawable.ic_action_fullscreen_exit)
        } else {
            fullScreenView.setImageResource(R.drawable.ic_action_fullscreen)
        }
    }

    override fun onDuration(duration: Int) {
        durationView.text = convertMillis(duration)
        timeBar.max = duration
    }

    override fun onCurrentPosition(position: Int) {
        if (!_seekFromUser) {
            positionView.text = convertMillis(position)
            timeBar.progress = position
        }
    }

    override fun onCurrentBufferingPosition(bufferingPosition: Int) {
        timeBar.secondaryProgress = bufferingPosition
    }

    override fun onPlayerError(errorMessage: String) {
        errorText.text = errorMessage
    }

    private fun bindCoverView() {
        val play: ImageView = getCoverView().findViewById(R.id.default_cover_play)
        val cover: ImageView = getCoverView().findViewById(R.id.default_cover_imageview)
        play.setOnClickListener {
            getCoverView().visibility = GONE
            controllerListener?.prepare(true)
        }
        bindCoverImage(cover)
    }

    private fun bindCompletionView() {
        val replay: TextView = getCompletionView().findViewById(R.id.default_completion_replay)
        replay.setOnClickListener {
            getCompletionView().visibility = GONE
            controllerListener?.onReplay()
        }
    }

    private fun bindErrorView() {
        val retry: TextView = getErrorView().findViewById(R.id.default_error_retry)
        retry.setOnClickListener {
            getErrorView().visibility = GONE
            controllerListener?.onRetry()
        }
    }

}