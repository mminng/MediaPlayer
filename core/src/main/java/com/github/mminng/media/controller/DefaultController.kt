package com.github.mminng.media.controller

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.github.mminng.media.R
import com.github.mminng.media.player.PlayerState
import com.github.mminng.media.utils.getStringForTime
import com.github.mminng.media.widget.MarqueeTextView
import com.github.mminng.media.widget.Menu
import com.github.mminng.media.widget.MenuView
import com.github.mminng.media.widget.timebar.DefaultTimeBar
import com.github.mminng.media.widget.timebar.TimeBar
import java.util.*

/**
 * Created by zh on 2021/9/20.
 */
class DefaultController @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseController(context, attrs), View.OnClickListener, TimeBar.OnScrubListener {

    private val backView: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_back)
    }
    private val titleView: MarqueeTextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_title)
    }
    private val playPauseView: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_play_pause)
    }
    private val timeBar: DefaultTimeBar by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_time_bar)
    }
    private val positionView: TextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_position)
    }
    private val durationView: TextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_duration)
    }
    private val speedView: TextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_speed)
    }
    private val fullScreenView: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_fullScreen)
    }
    private val topView: LinearLayout by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_controller_top)
    }
    private val bottomView: LinearLayout by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.media_controller_bottom)
    }
    private val coverImage: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        getCoverView().findViewById(R.id.default_cover_image)
    }
    private val errorTextView: TextView by lazy(LazyThreadSafetyMode.NONE) {
        getErrorView().findViewById(R.id.default_error_text)
    }
    private val bufferingView: ProgressBar by lazy(LazyThreadSafetyMode.NONE) {
        getBufferingView().findViewById(R.id.media_buffering)
    }
    private val seekText: TextView by lazy(LazyThreadSafetyMode.NONE) {
        getSwipeSeekView().findViewById(R.id.swipe_seek_text)
    }
    private val brightnessBar: ProgressBar by lazy(LazyThreadSafetyMode.NONE) {
        getSwipeBrightnessView().findViewById(R.id.swipe_brightness_bar)
    }
    private val volumeIcon: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        getSwipeVolumeView().findViewById(R.id.swipe_volume_icon)
    }
    private val volumeBar: ProgressBar by lazy(LazyThreadSafetyMode.NONE) {
        getSwipeVolumeView().findViewById(R.id.swipe_volume_bar)
    }
    private val touchSpeedView: LinearLayout by lazy(LazyThreadSafetyMode.NONE) {
        getTouchSpeedView().findViewById(R.id.media_touch_speed)
    }
    private val touchSpeedIcon: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        getTouchSpeedView().findViewById(R.id.media_touch_speed_icon)
    }

    private var _activeColor: Int = android.R.color.holo_blue_light
    private val builder: StringBuilder = StringBuilder()
    private val formatter = Formatter(builder, Locale.getDefault())
    private var _seekFromUser: Boolean = false
    private var _isFullScreen: Boolean = false
    private var _topEnable: Boolean = true
    private var _speedEnable: Boolean = true
    private var _timeBarEnable: Boolean = true
    private var _titleText: String = ""
    private val speedIconAnimator: ValueAnimator =
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

    @Suppress("DEPRECATION")
    @SuppressLint("UseCompatLoadingForColorStateLists")
    override fun onLayoutCreated(view: View) {
        //findViewById() here
        backView.setOnClickListener(this)
        playPauseView.setOnClickListener(this)
        fullScreenView.setOnClickListener(this)
        speedView.setOnClickListener(this)
        timeBar.addListener(this)
        timeBar.isEnabled = _timeBarEnable
        timeBar.setPlayedColor(ContextCompat.getColor(context, _activeColor))
        bufferingView.indeterminateTintList = resources.getColorStateList(_activeColor)
        bindCoverView()
        bindCompletionView()
        bindErrorView()
        addSpeedMenu()
        topView.visibility = if (_topEnable) VISIBLE else GONE
        titleView.text = _titleText
        speedIconAnimator.duration = 700
        speedIconAnimator.repeatCount = ValueAnimator.INFINITE
        speedIconAnimator.repeatMode = ValueAnimator.REVERSE
    }

    override fun onScrubStart(timeBar: TimeBar?, position: Long) {
        _seekFromUser = true
        showController(false)
    }

    override fun onScrubMove(timeBar: TimeBar?, position: Long) {
        positionView.text = getStringForTime(builder, formatter, position)
    }

    override fun onScrubStop(timeBar: TimeBar?, position: Long, canceled: Boolean) {
        _seekFromUser = false
        showController()
        positionView.text = getStringForTime(builder, formatter, position)
        timeBar?.setPosition(position)
        seekTo(position)
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

    override fun onDuration(duration: Long) {
        durationView.text = getStringForTime(builder, formatter, duration)
        timeBar.setDuration(duration)
    }

    override fun onPosition(position: Long) {
        if (!_seekFromUser) {
            positionView.text = getStringForTime(builder, formatter, position)
            timeBar.setPosition(position)
        }
    }

    override fun onBufferedPosition(bufferedPosition: Long) {
        timeBar.setBufferedPosition(bufferedPosition)
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
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        if (isFullScreen) {
            fullScreenView.visibility = GONE
            if (_speedEnable) {
                speedView.visibility = VISIBLE
            }
            if (bottomView.visibility == VISIBLE) {
                topView.visibility = VISIBLE
            }
        } else {
            fullScreenView.visibility = VISIBLE
            speedView.visibility = GONE
            if (_topEnable) {
                if (bottomView.visibility == VISIBLE) {
                    topView.visibility = VISIBLE
                }
            } else {
                topView.visibility = GONE
            }
        }
    }

    override fun onCompletion() {
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        if (touchSpeedView.visibility == VISIBLE) {
            touchSpeedView.visibility = GONE
            speedIconAnimator.cancel()
        }
    }

    override fun onPlayerError(error: String) {
        errorTextView.text = error
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        if (touchSpeedView.visibility == VISIBLE) {
            touchSpeedView.visibility = GONE
            speedIconAnimator.cancel()
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

    override fun requireCover(): ImageView = coverImage

    override fun canBack(): Boolean {
        return when {
            speedMenu.isShowing() -> {
                speedMenu.hide()
                false
            }
            else -> {
                super.canBack()
            }
        }
    }

    override fun release() {
        super.release()
        speedIconAnimator.cancel()
        timeBar.removeListener(this)
    }

    /*Gesture Listener*/
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

    override fun onLongTap(touching: Boolean) {
        super.onLongTap(touching)
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        if (touching) {
            touchSpeedView.visibility = VISIBLE
            speedIconAnimator.start()
        } else {
            touchSpeedView.visibility = GONE
            speedIconAnimator.cancel()
        }
    }

    override fun onSwipeSeekView(
        showing: Boolean,
        position: Long,
        duration: Long,
        allowSeek: Boolean
    ) {
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        if (showing) {
            if (getSwipeSeekView().visibility != VISIBLE) {
                getSwipeSeekView().visibility = VISIBLE
            }
            "${
                getStringForTime(
                    builder,
                    formatter,
                    position
                )
            }/${
                getStringForTime(
                    builder,
                    formatter,
                    duration
                )
            }".also {
                seekText.text = it
            }
        } else {
            if (getSwipeSeekView().visibility != GONE) {
                getSwipeSeekView().visibility = GONE
            }
            if (allowSeek) {
                positionView.text = getStringForTime(builder, formatter, position)
                timeBar.setPosition(position)
                seekTo(position)
            }
        }
    }

    override fun onSwipeBrightnessView(showing: Boolean, position: Int, max: Int) {
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        if (showing) {
            if (getSwipeBrightnessView().visibility != VISIBLE) {
                getSwipeBrightnessView().visibility = VISIBLE
                brightnessBar.max = max
            }
            brightnessBar.progress = position
        } else {
            if (getSwipeBrightnessView().visibility != GONE) {
                getSwipeBrightnessView().visibility = GONE
            }
        }
    }

    override fun onSwipeVolumeView(showing: Boolean, position: Int, max: Int) {
        if (speedMenu.isShowing()) {
            speedMenu.hide()
        }
        if (showing) {
            if (getSwipeVolumeView().visibility != VISIBLE) {
                getSwipeVolumeView().visibility = VISIBLE
                volumeBar.max = max
            }
            if (position > 0) {
                volumeIcon.setImageResource(R.drawable.ic_volume)
            } else {
                volumeIcon.setImageResource(R.drawable.ic_volume_mute)
            }
            volumeBar.progress = position
        } else {
            if (getSwipeVolumeView().visibility != GONE) {
                getSwipeVolumeView().visibility = GONE
            }
        }
    }
    /*Gesture Listener*/

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
        val play: ImageView = getCoverView().findViewById(R.id.default_cover_play_image)
        play.setOnClickListener {
            getCoverView().visibility = GONE
            if (getPlayerState() == PlayerState.INITIALIZED) {
                prepare(true)
                return@setOnClickListener
            }
            if (getPlayerState() == PlayerState.COMPLETION) {
                replay()
            }
        }
    }

    private fun bindCompletionView() {
        val replay: TextView = getCompletionView().findViewById(R.id.default_replay_button)
        replay.setOnClickListener {
            getCompletionView().visibility = GONE
            replay()
        }
    }

    private fun bindErrorView() {
        val retry: TextView = getErrorView().findViewById(R.id.default_retry_button)
        retry.setOnClickListener {
            getErrorView().visibility = GONE
            retry()
        }
    }

    private fun setTopVisibility(visibility: Int) {
        if (isReady()) {
            if (_isFullScreen) {
                topView.visibility = visibility
            } else {
                if (_topEnable) {
                    topView.visibility = visibility
                } else {
                    topView.visibility = GONE
                }
            }
            if (topView.visibility == View.VISIBLE) {
                titleView.marquee()
            } else {
                titleView.cancel()
            }
        }
    }

    private fun setBottomVisibility(visibility: Int) {
        if (isReady()) {
            bottomView.visibility = visibility
        }
    }

    fun setTitle(title: String) {
        _titleText = title
        if (isReady()) {
            titleView.text = title
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("UseCompatLoadingForColorStateLists")
    fun setStyleColor(@ColorRes activeColor: Int) {
        _activeColor = activeColor
        if (isReady()) {
            speedMenu.setSelectedColor(ContextCompat.getColor(context, activeColor))
            bufferingView.indeterminateTintList = resources.getColorStateList(activeColor)
            timeBar.setPlayedColor(ContextCompat.getColor(context, activeColor))
        }
    }

    fun setSpeedData(speedData: List<Menu<Float>>) {
        _speedData = speedData
        if (isReady()) {
            speedMenu.setMenuData(speedData)
        }
    }

    fun setSpeedEnable(enable: Boolean) {
        _speedEnable = enable
        if (isReady() && _isFullScreen) {
            if (enable) speedView.visibility = VISIBLE else speedMenu.visibility = GONE
        }
    }

    fun setTimeBarEnable(enable: Boolean) {
        _timeBarEnable = enable
        if (isReady()) {
            timeBar.isEnabled = _timeBarEnable
        }
    }

    fun setCoverPlayResource(@DrawableRes res: Int) {
        getCoverView()
            .findViewById<ImageView>(R.id.default_cover_play_image)
            .setImageResource(res)
    }

    fun setTopControllerEnable(enable: Boolean) {
        _topEnable = enable
        if (isReady()) {
            if (enable) {
                if (bottomView.visibility == VISIBLE) {
                    topView.visibility = VISIBLE
                }
            } else {
                topView.visibility = GONE
            }
        }
    }
}