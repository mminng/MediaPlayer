package com.github.mminng.media.player

/**
 * Created by zh on 2022/2/16.
 */
internal interface PlayerListener {

    fun onPrepared()

    fun onStarted()

    fun onPaused()

    fun onScreenChanged(fullscreen: Boolean)

    fun onCompletion()

    fun onError(error: String)
}

class OnPlayerListener : PlayerListener {

    private var _prepared: (() -> Unit)? = null
    private var _started: (() -> Unit)? = null
    private var _paused: (() -> Unit)? = null
    private var _screenChanged: ((fullscreen: Boolean) -> Unit)? = null
    private var _completion: (() -> Unit)? = null
    private var _error: ((error: String) -> Unit)? = null

    override fun onPrepared() {
        _prepared?.invoke()
    }

    override fun onStarted() {
        _started?.invoke()
    }

    override fun onPaused() {
        _paused?.invoke()
    }

    override fun onScreenChanged(fullscreen: Boolean) {
        _screenChanged?.invoke(fullscreen)
    }

    override fun onCompletion() {
        _completion?.invoke()
    }

    override fun onError(error: String) {
        _error?.invoke(error)
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

    fun screenChanged(listener: (fullscreen: Boolean) -> Unit) {
        _screenChanged = listener
    }

    fun completion(listener: () -> Unit) {
        _completion = listener
    }

    fun error(listener: (error: String) -> Unit) {
        _error = listener
    }
}