package com.github.mminng.media.player

/**
 * Created by zh on 2022/2/16.
 */
interface PlayerListener {

    fun onPrepared()

    fun onStarted()

    fun onPaused()

    fun onScreenChanged(isFullScreen: Boolean)

    fun onCompletion()

    fun onError(errorMessage: String)

}

class OnPlayerListener : PlayerListener {

    private var _prepared: (() -> Unit)? = null
    private var _started: (() -> Unit)? = null
    private var _paused: (() -> Unit)? = null
    private var _screenChanged: ((isFullScreen: Boolean) -> Unit)? = null
    private var _completion: (() -> Unit)? = null
    private var _error: ((errorMessage: String) -> Unit)? = null

    override fun onPrepared() {
        _prepared?.invoke()
    }

    override fun onStarted() {
        _started?.invoke()
    }

    override fun onPaused() {
        _paused?.invoke()
    }

    override fun onScreenChanged(isFullScreen: Boolean) {
        _screenChanged?.invoke(isFullScreen)
    }

    override fun onCompletion() {
        _completion?.invoke()
    }

    override fun onError(errorMessage: String) {
        _error?.invoke(errorMessage)
    }

    fun prepared(listener: () -> Unit) {
        _prepared = listener
    }

    fun started(listener: () -> Unit) {
        _started = listener
    }

    fun paused(listener: () -> Unit) {
        _paused = listener
    }

    fun screenChanged(listener: (isFullScreen: Boolean) -> Unit) {
        _screenChanged = listener
    }

    fun completion(listener: () -> Unit) {
        _completion = listener
    }

    fun error(listener: (errorMessage: String) -> Unit) {
        _error = listener
    }

}