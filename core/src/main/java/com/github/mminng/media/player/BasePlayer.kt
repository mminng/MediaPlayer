package com.github.mminng.media.player

/**
 * Created by zh on 2021/12/10.
 */
abstract class BasePlayer : Player {

    private var _listener: Player.Listener? = null

    final override fun videoSizeChanged(width: Int, height: Int) {
        _listener?.onVideoSizeChanged(width, height)
    }

    final override fun stateIdle() {
        _listener?.onPlayerStateChanged(PlayerState.IDLE)
    }

    final override fun stateInitialized() {
        _listener?.onPlayerStateChanged(PlayerState.INITIALIZED)
    }

    final override fun statePreparing() {
        _listener?.onPlayerStateChanged(PlayerState.PREPARING)
    }

    final override fun statePrepared() {
        _listener?.onPlayerStateChanged(PlayerState.PREPARED)
    }

    final override fun stateBufferingStart() {
        _listener?.onPlayerStateChanged(PlayerState.BUFFERING)
    }

    final override fun stateBufferingEnd() {
        _listener?.onPlayerStateChanged(PlayerState.BUFFERED)
    }

    final override fun stateRenderingStart() {
        _listener?.onPlayerStateChanged(PlayerState.RENDERING)
    }

    final override fun stateStarted() {
        _listener?.onPlayerStateChanged(PlayerState.STARTED)
    }

    final override fun statePaused() {
        _listener?.onPlayerStateChanged(PlayerState.PAUSED)
    }

    final override fun stateCompletion() {
        _listener?.onPlayerStateChanged(PlayerState.COMPLETION)
    }

    final override fun stateError(error: String) {
        _listener?.onPlayerStateChanged(PlayerState.ERROR, error)
    }

    final override fun getPlayerState(): PlayerState {
        _listener?.let {
            return it.requirePlayerState()
        }
        return PlayerState.IDLE
    }

    final override fun setListener(listener: Player.Listener) {
        if (_listener === listener) return
        _listener = listener
    }
}