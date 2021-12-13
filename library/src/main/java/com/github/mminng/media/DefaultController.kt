package com.github.mminng.media

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.github.mminng.media.controller.BaseController
import com.github.mminng.media.utils.convertMillis
import com.squareup.picasso.Picasso

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
    private val fullScreen: ImageView by lazy {
        findViewById(R.id.media_fullScreen)
    }

    private var seekFromUser: Boolean = false

    override fun setControllerLayout(): Int {
        return R.layout.default_controller_layout
    }

    override fun onLayoutCreated(view: View) {
        //also you can do findViewById() here
        playPauseView.setOnClickListener(this)
        fullScreen.setOnClickListener(this)
        timeBar.setOnSeekBarChangeListener(this)
        val coverView: View = getCoverView()
        val completionView: View = getCompletionView()
        val errorView: View = getErrorView()
        val play = coverView.findViewById<ImageView>(R.id.default_cover_play)
        val cover = coverView.findViewById<ImageView>(R.id.default_cover_imageview)
        val replay = completionView.findViewById<TextView>(R.id.default_completion_replay)
        val errorMessage = errorView.findViewById<TextView>(R.id.default_error_message)
        val retry = errorView.findViewById<TextView>(R.id.default_error_retry)
        Picasso.get().load("https://img1.baidu.com/it/u=1438323812,1496169743&fm=26&fmt=auto")
            .into(cover)
        play.setOnClickListener {
            coverView.visibility = GONE
            controllerListener?.onPrepareAsync()
        }
        replay.setOnClickListener {
            completionView.visibility = GONE
            controllerListener?.onReplay()
        }
        errorMessage.text = getErrorMessage()
        retry.setOnClickListener {
            errorView.visibility = GONE
            controllerListener?.onRetry()
        }
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

    override fun onProgressUpdate(progress: Int) {
        if (!seekFromUser) {
            positionView.text = convertMillis(progress)
            timeBar.progress = progress
        }
    }

    override fun onBufferingProgressUpdate(bufferingProgress: Int) {
        timeBar.secondaryProgress = bufferingProgress
    }

}