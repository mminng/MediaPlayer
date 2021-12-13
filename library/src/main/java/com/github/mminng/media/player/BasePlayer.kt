package com.github.mminng.media.player

import com.github.mminng.media.player.state.PlayerState

/**
 * Created by zh on 2021/12/10.
 */
abstract class BasePlayer : Player {

    private var _playerListener: Player.OnPlayerListener? = null
    private var _playerStateListener: Player.OnPlayerStateListener? = null

    override fun videoSizeChanged(width: Int, height: Int) {
        _playerListener?.onVideoSizeChanged(width, height)
    }

    override fun bufferingUpdate(bufferingProgress: Int) {
        _playerListener?.onBufferingUpdate(bufferingProgress)
    }

    override fun prepared() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.PREPARED)
    }

    override fun bufferingStart() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.BUFFERING)
    }

    override fun bufferingEnd() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.BUFFERED)
    }

    override fun renderingStart() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.RENDERING)
    }

    override fun completion() {
        _playerStateListener?.onPlayerStateChanged(PlayerState.COMPLETION)
    }

    override fun error(errorMessage: String) {
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