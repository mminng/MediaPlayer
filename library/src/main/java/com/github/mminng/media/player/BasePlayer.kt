package com.github.mminng.media.player

/**
 * Created by zh on 2021/12/10.
 */
abstract class BasePlayer : Player {

    private var _listener: Player.Listener? = null

    override fun videoSizeChanged(width: Int, height: Int) {
        _listener?.onVideoSizeChanged(width, height)
    }

    override fun stateIdle() {
        _listener?.onPlayerStateChanged(PlayerState.IDLE)
    }

    override fun stateInitialized() {
        _listener?.onPlayerStateChanged(PlayerState.INITIALIZED)
    }

    override fun statePreparing() {
        _listener?.onPlayerStateChanged(PlayerState.PREPARING)
    }

    override fun statePrepared() {
        _listener?.onPlayerStateChanged(PlayerState.PREPARED)
    }

    override fun stateBufferingStart() {
        _listener?.onPlayerStateChanged(PlayerState.BUFFERING)
    }

    override fun stateBufferingEnd() {
        _listener?.onPlayerStateChanged(PlayerState.BUFFERED)
    }

    override fun stateRenderingStart() {
        _listener?.onPlayerStateChanged(PlayerState.RENDERING)
    }

    override fun stateStarted() {
        _listener?.onPlayerStateChanged(PlayerState.STARTED)
    }

    override fun statePaused() {
        _listener?.onPlayerStateChanged(PlayerState.PAUSED)
    }

    override fun stateCompletion() {
        _listener?.onPlayerStateChanged(PlayerState.COMPLETION)
    }

    override fun stateError(errorMessage: String) {
        _listener?.onPlayerStateChanged(PlayerState.ERROR, errorMessage)
    }

    override fun getPlayerState(): PlayerState {
        _listener?.requirePlayerState()?.let {
            return it
        }
        return PlayerState.IDLE
    }

    override fun setListener(listener: Player.Listener) {
        if (_listener === listener) return
        _listener = listener
    }

}