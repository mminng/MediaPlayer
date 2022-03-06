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
import com.github.mminng.media.player.PlayerState
import com.github.mminng.media.utils.convertMillis
import com.github.mminng.media.widget.MarqueeTextView
import com.github.mminng.media.widget.Menu
import com.github.mminng.media.widget.MenuView

/**
 * Created by zh on 2021/9/20.
 */
class DefaultController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseController(context, attrs), View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private val backView: ImageView by lazy {
        findViewById(R.id.media_back)
    }
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
    private val speedView: TextView by lazy {
        findViewById(R.id.media_speed)
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
    private var _topControllerVisibility: Int = View.GONE
    private var _bottomControllerVisibility: Int = View.VISIBLE
    private val speedMenu: MenuView<Float> = MenuView(context)

    override fun setControllerLayout(): Int {
        return R.layout.default_controller_layout
    }

    override fun onLayoutCreated(view: View) {
        //findViewById() here
        backView.setOnClickListener(this)
        playPauseView.setOnClickListener(this)
        fullScreenView.setOnClickListener(this)
        speedView.setOnClickListener(this)
        timeBar.setOnSeekBarChangeListener(this)
        bindCoverView()
        bindCompletionView()
        bindErrorView()
        addSpeedMenu()
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
            backView -> {
                showController()
                controllerListener?.onPlayerBack()
            }
            playPauseView -> {
                showController()
                controllerListener?.onPlayOrPause(true)
            }
            fullScreenView -> {
                showController()
                controllerListener?.onScreenChanged()
            }
            speedView -> {
                hideController()
                speedMenu.show()
            }
            else -> {
                //NO OP
            }
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

    override fun onPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            playPauseView.setImageResource(R.drawable.ic_action_playing)
        } else {
            playPauseView.setImageResource(R.drawable.ic_action_paused)
        }
    }

    override fun onScreenChanged(isFullScreen: Boolean) {
        if (isFullScreen) {
            fullScreenView.setImageResource(R.drawable.ic_action_fullscreen_exit)
            speedView.visibility = VISIBLE
        } else {
            fullScreenView.setImageResource(R.drawable.ic_action_fullscreen)
            speedView.visibility = GONE
        }
    }

    override fun onCompletion() {
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
    }

    override fun onPlayerError(errorMessage: String) {
        errorMessageView.text = errorMessage
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
    }

    override fun onShowController() {
        if (_topControllerVisibility == View.VISIBLE) {
            setTopControllerVisibility(View.VISIBLE)
        }
        setBottomControllerVisibility(View.VISIBLE)
    }

    override fun onHideController() {
        setTopControllerVisibility(View.GONE, false)
        setBottomControllerVisibility(View.GONE)
    }

    override fun onSingleTap() {
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        } else {
            super.onSingleTap()
        }
    }

    override fun onDoubleTap() {
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        } else {
            super.onDoubleTap()
        }
    }

    override fun onLongTap(isTouch: Boolean) {
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        super.onLongTap(isTouch)
    }

    override fun onCanBack(): Boolean {
        return if (speedMenu.isShowing()) {
            speedMenu.hide()
            false
        } else {
            super.onCanBack()
        }
    }

    private fun addSpeedMenu() {
        addView(speedMenu)
        val speedData: List<Menu<Float>> = listOf(
            Menu(false, "2.0X", 2.0F),
            Menu(false, "1.5X", 1.5F),
            Menu(false, "1.25X", 1.25F),
            Menu(true, "1.0X", 1.0F),
            Menu(false, "0.75X", 0.75F),
            Menu(false, "0.5X", 0.5F)
        )
        speedMenu.setMenuData(speedData)
        speedMenu.setOnMenuSelectedListener {
            controllerListener?.onChangeSpeed(it)
        }
    }

    private fun bindCoverView() {
        val play: ImageView = getCoverView().findViewById(R.id.default_cover_play_imageview)
        val cover: ImageView = getCoverView().findViewById(R.id.default_cover_imageview)
        play.setOnClickListener {
            if (getPlayerState() != PlayerState.INITIALIZED) return@setOnClickListener
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

    fun setTitle(title: String) {
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