package com.github.mminng.media.controller

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.github.mminng.media.R
import com.github.mminng.media.utils.convertMillis
import com.github.mminng.media.widget.MarqueeTextView

/**
 * Created by zh on 2021/9/20.
 */
class DefaultController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseController(context, attrs), View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private val titleView: MarqueeTextView by lazy {
        findViewById(R.id.media_title)
    }
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
    private val topControllerView: LinearLayout by lazy {
        findViewById(R.id.media_controller_top)
    }
    private val bottomControllerView: LinearLayout by lazy {
        findViewById(R.id.media_controller_bottom)
    }
    private val errorMessageView: TextView by lazy {
        getErrorView().findViewById(R.id.default_error_message)
    }

    private var _seekFromUser: Boolean = false
    private var _titleStr: String = ""
    private var _topControllerVisibility: Int = View.INVISIBLE
    private var _bottomControllerVisibility: Int = View.VISIBLE

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
        topControllerView.visibility = _topControllerVisibility
        bottomControllerView.visibility = _bottomControllerVisibility
        titleView.text = _titleStr
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        _seekFromUser = true
        showController(false)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            positionView.text = convertMillis(progress)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        _seekFromUser = false
        showController()
        seekBar?.let {
            controllerListener?.onSeekTo(it.progress)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            playPauseView -> {
                showController()
                controllerListener?.onPlayOrPause(true)
            }
            fullScreenView -> {
                showController()
                controllerListener?.onFullScreenChanged()
            }
            else -> {
                //NO OP
            }
        }
    }

    override fun onPlayOrPause(isPlaying: Boolean) {
        if (isPlaying) {
            playPauseView.setImageResource(R.drawable.ic_action_playing)
        } else {
            playPauseView.setImageResource(R.drawable.ic_action_paused)
        }
    }

    override fun onFullScreenChanged(isFullScreen: Boolean) {
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

    override fun onBufferPosition(position: Int) {
        timeBar.secondaryProgress = position
    }

    override fun onPlayerError(errorMessage: String) {
        errorMessageView.text = errorMessage
    }

    override fun onShowController() {
        if (_topControllerVisibility == View.VISIBLE) {
            setTopControllerVisibility(View.VISIBLE)
        }
        setBottomControllerVisibility(View.VISIBLE)
    }

    override fun onHideController() {
        setTopControllerVisibility(View.INVISIBLE, false)
        setBottomControllerVisibility(View.INVISIBLE)
    }

    private fun bindCoverView() {
        val play: ImageView = getCoverView().findViewById(R.id.default_cover_play_imageview)
        val cover: ImageView = getCoverView().findViewById(R.id.default_cover_imageview)
        play.setOnClickListener {
            getCoverView().visibility = GONE
            controllerListener?.onPrepare(true)
        }
        bindCoverImage(cover)
    }

    private fun bindCompletionView() {
        val replay: TextView =
            getCompletionView().findViewById(R.id.default_completion_replay_button)
        replay.setOnClickListener {
            getCompletionView().visibility = GONE
            controllerListener?.onReplay()
        }
    }

    private fun bindErrorView() {
        val retry: TextView = getErrorView().findViewById(R.id.default_error_retry_button)
        retry.setOnClickListener {
            getErrorView().visibility = GONE
            controllerListener?.onRetry()
        }
    }

    fun setMediaTitle(title: String) {
        _titleStr = title
        if (isControllerReady()) {
            titleView.text = title
        }
    }

    fun setCoverPlayButtonResource(@DrawableRes res: Int) {
        getCoverView()
            .findViewById<ImageView>(R.id.default_cover_play_imageview)
            .setImageResource(res)
    }

    fun setTopControllerVisibility(visibility: Int, syncVisibility: Boolean = true) {
        if (syncVisibility) {
            _topControllerVisibility = visibility
        }
        if (isControllerReady()) {
            topControllerView.visibility = visibility
            if (visibility == View.VISIBLE) {
                titleView.marquee()
            } else {
                titleView.cancel()
            }
        }
    }

    fun setBottomControllerVisibility(visibility: Int) {
        _bottomControllerVisibility = visibility
        if (isControllerReady()) {
            bottomControllerView.visibility = visibility
        }
    }

}