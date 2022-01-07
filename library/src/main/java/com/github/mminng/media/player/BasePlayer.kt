package com.github.mminng.media.player

/**
 * Created by zh on 2021/12/10.
 */
abstract class BasePlayer : Player {

    private var _playerListener: Player.OnPlayerListener? = null
    private var _playerStateListener: Player.OnPlayerStateListener? = null

    override fun videoSizeChanged(width: Int, height: Int) {
        _playerListener?.onVideoSizeChanged(width, height)
    }

    override fun bufferingUpdate(bufferingPosition: Int) {
        _playerListener?.onBufferingUpdate(bufferingPosition)
    }

    override fun stateIdle() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.IDLE)
    }

    override fun stateInitialized() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.INITIALIZED)
    }

    override fun statePreparing() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.PREPARING)
    }

    override fun statePrepared() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.PREPARED)
    }

    override fun stateBufferingStart() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.BUFFERING)
    }

    override fun stateBufferingEnd() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.BUFFERED)
    }

    override fun stateRenderingStart() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.RENDERING)
    }

    override fun stateStarted() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.STARTED)
    }

    override fun statePaused() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.PAUSED)
    }

    override fun stateCompletion() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.COMPLETION)
    }

    override fun stateError(errorMessage: String) {
        _playerStateListener?.onPlayerStateChanged(PlayerState.ERROR, errorMessage)
    }

    override fun getPlayerState(): PlayerState {
        _playerStateListener?.getPlayerState()?.let {
            return it
        }
        return PlayerState.IDLE
    }

    override fun setOnPlayerListener(listener: Player.OnPlayerListener) {
        if (_playerListener === listener) return
        _playerListener = listener
    }

    override fun setOnPlayerStateListener(listener: Player.OnPlayerStateListener) {
        if (_playerStateListener === listener) return
        _playerStateListener = listener
    }

}