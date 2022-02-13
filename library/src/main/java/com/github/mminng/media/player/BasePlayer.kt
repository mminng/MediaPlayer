package com.github.mminng.media.player

/**
 * Created by zh on 2021/12/10.
 */
abstract class BasePlayer : Player {

    private var _playerListener: Player.OnPlayerListener? = null

    override fun videoSizeChanged(width: Int, height: Int) {
        _playerListener?.onVideoSizeChanged(width, height)
    }

    override fun stateIdle() {
        _playerListener?.onPlayerStateChanged(PlayerState.IDLE)
    }

    override fun stateInitialized() {
        _playerListener?.onPlayerStateChanged(PlayerState.INITIALIZED)
    }

    override fun statePreparing() {
        _playerListener?.onPlayerStateChanged(PlayerState.PREPARING)
    }

    override fun statePrepared() {
        _playerListener?.onPlayerStateChanged(PlayerState.PREPARED)
    }

    override fun stateBufferingStart() {
        _playerListener?.onPlayerStateChanged(PlayerState.BUFFERING)
    }

    override fun stateBufferingEnd() {
        _playerListener?.onPlayerStateChanged(PlayerState.BUFFERED)
    }

    override fun stateRenderingStart() {
        _playerListener?.onPlayerStateChanged(PlayerState.RENDERING)
    }

    override fun stateStarted() {
        _playerListener?.onPlayerStateChanged(PlayerState.STARTED)
    }

    override fun statePaused() {
        _playerListener?.onPlayerStateChanged(PlayerState.PAUSED)
    }

    override fun stateCompletion() {
        _playerListener?.onPlayerStateChanged(PlayerState.COMPLETION)
    }

    override fun stateError(errorMessage: String) {
        _playerListener?.onPlayerStateChanged(PlayerState.ERROR, errorMessage)
    }

    override fun getPlayerState(): PlayerState {
        _playerListener?.getPlayerState()?.let {
            return it
        }
        return PlayerState.IDLE
    }

    override fun setOnPlayerListener(listener: Player.OnPlayerListener) {
        if (_playerListener === listener) return
        _playerListener = listener
    }

}