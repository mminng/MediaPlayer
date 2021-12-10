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

    override fun playingChanged(isPlaying: Boolean) {
        _playerStateListener?.onPlayingChanged(isPlaying)
    }

    override fun playerStateChanged(state: PlayerState) {
        _playerStateListener?.onPlayerStateChanged(state)
    }

    override fun bufferingUpdate(bufferingProgress: Int) {
        _playerListener?.onBufferingUpdate(bufferingProgress)
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