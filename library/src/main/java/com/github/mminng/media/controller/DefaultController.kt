package com.github.mminng.media.controller

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.github.mminng.media.R
import com.github.mminng.media.player.PlayerState
import com.github.mminng.media.utils.convertMillis
import com.github.mminng.media.utils.e
import com.github.mminng.media.widget.MarqueeTextView
import com.github.mminng.media.widget.Menu
import com.github.mminng.media.widget.MenuView

/**
 * Created by zh on 2021/9/20.
 */
@SuppressLint("UseCompatLoadingForColorStateLists")
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
    private val bufferView: ProgressBar by lazy {
        getBufferView().findViewById(R.id.media_buffer)
    }
    private val progressText: TextView by lazy {
        getSwipeProgressView().findViewById(R.id.swipe_progress_text)
    }
    private val brightnessBar: ProgressBar by lazy {
        getSwipeBrightnessView().findViewById(R.id.swipe_brightness_bar)
    }
    private val volumeIcon: ImageView by lazy {
        getSwipeVolumeView().findViewById(R.id.swipe_volume_icon)
    }
    private val volumeBar: ProgressBar by lazy {
        getSwipeVolumeView().findViewById(R.id.swipe_volume_bar)
    }
    private val touchSpeedView: LinearLayout by lazy {
        getTouchSpeedView().findViewById(R.id.media_touch_speed)
    }
    private val touchSpeedIcon: ImageView by lazy {
        getTouchSpeedView().findViewById(R.id.media_touch_speed_icon)
    }

    private var _activeColor: Int = R.color.teal
    private var _seekFromUser: Boolean = false
    private var _isFullScreen: Boolean = false
    private var _topEnable: Boolean = true
    private var _speedEnable: Boolean = true
    private var _titleStr: String = ""
    private var touchSpeedIconAnimator: ValueAnimator =
        ObjectAnimator.ofFloat(touchSpeedIcon, "alpha", 1.0F, 0.3F)
    private val speedMenu: MenuView<Float> = MenuView(context)
    private var _speedData: List<Menu<Float>> = listOf(
        Menu(false, "2.0X", 2.0F),
        Menu(false, "1.5X", 1.5F),
        Menu(false, "1.25X", 1.25F),
        Menu(true, "1.0X", 1.0F),
        Menu(false, "0.75X", 0.75F),
        Menu(false, "0.5X", 0.5F)
    )

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
        bufferView.indeterminateTintList = resources.getColorStateList(_activeColor)
        bindCoverView()
        bindCompletionView()
        bindErrorView()
        addSpeedMenu()
        topControllerView.visibility = if (_topEnable) VISIBLE else GONE
        titleView.text = _titleStr
        touchSpeedIconAnimator.duration = 700
        touchSpeedIconAnimator.repeatCount = ValueAnimator.INFINITE
        touchSpeedIconAnimator.repeatMode = ValueAnimator.REVERSE
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
            seekTo(it.progress)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            backView -> {
                showController()
                playerBack()
            }
            playPauseView -> {
                showController()
                playOrPause(true)
            }
            fullScreenView -> {
                showController()
                screenChanged()
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
        _isFullScreen = isFullScreen
        if (isFullScreen) {
            fullScreenView.visibility = GONE
            if (_speedEnable) {
                speedView.visibility = VISIBLE
            }
            if (bottomControllerView.visibility == VISIBLE) {
                topControllerView.visibility = VISIBLE
            }
        } else {
            fullScreenView.visibility = VISIBLE
            speedView.visibility = GONE
            if (!_topEnable) {
                topControllerView.visibility = GONE
            }
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
        setTopVisibility(View.VISIBLE)
        setBottomVisibility(View.VISIBLE)
    }

    override fun onHideController() {
        setTopVisibility(View.GONE)
        setBottomVisibility(View.GONE)
    }

    override fun onSingleTap() {
        when {
            speedMenu.isShowing() -> {
                speedMenu.hide()
            }
            else -> {
                super.onSingleTap()
            }
        }
    }

    override fun onDoubleTap() {
        when {
            speedMenu.isShowing() -> {
                speedMenu.hide()
            }
            else -> {
                super.onDoubleTap()
            }
        }
    }

    override fun onLongTap(isTouch: Boolean) {
        super.onLongTap(isTouch)
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        if (isTouch) {
            touchSpeedView.visibility = VISIBLE
            touchSpeedIconAnimator.start()
        } else {
            touchSpeedView.visibility = GONE
            touchSpeedIconAnimator.cancel()
        }
    }

    override fun onSwipeProgressView(
        show: Boolean,
        currentPosition: Int,
        duration: Int,
        canSeek: Boolean
    ) {
        if (show) {
            if (getSwipeProgressView().visibility != VISIBLE) {
                getSwipeProgressView().visibility = VISIBLE
            }
            "${convertMillis(currentPosition)}/${convertMillis(duration)}".also {
                progressText.text = it
            }
        } else {
            if (getSwipeProgressView().visibility != GONE) {
                getSwipeProgressView().visibility = GONE
            }
            if (canSeek) {
                seekTo(currentPosition)
            }
        }
    }

    override fun onSwipeBrightnessView(show: Boolean, progress: Int, max: Int) {
        if (show) {
            if (getSwipeBrightnessView().visibility != VISIBLE) {
                getSwipeBrightnessView().visibility = VISIBLE
                brightnessBar.max = max
            }
            brightnessBar.progress = progress
        } else {
            if (getSwipeBrightnessView().visibility != GONE) {
                getSwipeBrightnessView().visibility = GONE
            }
        }
    }

    override fun onSwipeVolumeView(show: Boolean, progress: Int, max: Int) {
        if (show) {
            if (getSwipeVolumeView().visibility != VISIBLE) {
                getSwipeVolumeView().visibility = VISIBLE
                volumeBar.max = max
            }
            if (progress > 0) {
                volumeIcon.setImageResource(R.drawable.ic_volume_on)
            } else {
                volumeIcon.setImageResource(R.drawable.ic_volume_mute)
            }
            volumeBar.progress = progress
        } else {
            if (getSwipeVolumeView().visibility != GONE) {
                getSwipeVolumeView().visibility = GONE
            }
        }
    }

    override fun onCanBack(): Boolean {
        return when {
            speedMenu.isShowing() -> {
                speedMenu.hide()
                false
            }
            else -> {
                super.onCanBack()
            }
        }
    }

    private fun addSpeedMenu() {
        addView(speedMenu)
        speedMenu.setSelectedColor(ContextCompat.getColor(context, _activeColor))
        speedMenu.setMenuData(_speedData)
        speedMenu.setOnMenuSelectedListener { text, value ->
            if (value == 1.0F) {
                speedView.text = resources.getString(R.string.player_speed)
            } else {
                speedView.text = text
            }
            changeSpeed(value)
        }
        if (_isFullScreen) {
            if (_speedEnable) speedView.visibility = VISIBLE else speedMenu.visibility = GONE
        }
    }

    private fun bindCoverView() {
        val play: ImageView = getCoverView().findViewById(R.id.default_cover_play_imageview)
        val cover: ImageView = getCoverView().findViewById(R.id.default_cover_imageview)
        play.setOnClickListener {
            if (getPlayerState() != PlayerState.INITIALIZED) return@setOnClickListener
            getCoverView().visibility = GONE
            prepare(true)
        }
        bindCoverImage(cover)
    }

    private fun bindCompletionView() {
        val replay: TextView =
            getCompletionView().findViewById(R.id.default_completion_replay_button)
        replay.setOnClickListener {
            getCompletionView().visibility = GONE
            replay()
        }
    }

    private fun bindErrorView() {
        val retry: TextView = getErrorView().findViewById(R.id.default_error_retry_button)
        retry.setOnClickListener {
            getErrorView().visibility = GONE
            retry()
        }
    }

    private fun setTopVisibility(visibility: Int) {
        if (isControllerReady()) {
            if (_isFullScreen) {
                topControllerView.visibility = visibility
            } else {
                if (_topEnable) {
                    topControllerView.visibility = visibility
                } else {
                    topControllerView.visibility = GONE
                }
            }
            if (visibility == View.VISIBLE) {
                titleView.marquee()
            } else {
                titleView.cancel()
            }
        }
    }

    private fun setBottomVisibility(visibility: Int) {
        if (isControllerReady()) {
            bottomControllerView.visibility = visibility
        }
    }

    fun setTitle(title: String) {
        _titleStr = title
        if (isControllerReady()) {
            titleView.text = title
        }
    }

    fun setSpeedData(speedData: List<Menu<Float>>) {
        _speedData = speedData
        if (isControllerReady()) {
            speedMenu.setMenuData(speedData)
        }
    }

    fun setSpeedEnable(enable: Boolean) {
        _speedEnable = enable
        if (isControllerReady() && _isFullScreen) {
            if (enable) speedView.visibility = VISIBLE else speedMenu.visibility = GONE
        }
    }

    fun setStyleColor(@ColorRes activeColor: Int) {
        _activeColor = activeColor
        if (isControllerReady()) {
            speedMenu.setSelectedColor(ContextCompat.getColor(context, activeColor))
            bufferView.indeterminateTintList = resources.getColorStateList(activeColor)
        }
    }

    fun setCoverPlayButtonResource(@DrawableRes res: Int) {
        getCoverView()
            .findViewById<ImageView>(R.id.default_cover_play_imageview)
            .setImageResource(res)
    }

    fun setTopControllerEnable(enable: Boolean) {
        _topEnable = enable
        if (isControllerReady()) {
            if (enable) {
                topControllerView.visibility = VISIBLE
            } else {
                topControllerView.visibility = GONE
            }
        }
    }

}