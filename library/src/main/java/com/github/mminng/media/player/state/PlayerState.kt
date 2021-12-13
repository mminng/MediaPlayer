package com.github.mminng.media.player.state

/**
 * Created by zh on 2021/12/5.
 */
sealed class PlayerState {

    object IDLE : PlayerState()
    object INITIALIZED : PlayerState()
    object PREPARING : PlayerState()
    object PREPARED : PlayerState()
    object BUFFERING : PlayerState()
    object BUFFERED : PlayerState()
    object STARTED : PlayerState()
    object PAUSED : PlayerState()
    object COMPLETION : PlayerState()
    object ERROR : PlayerState()

}
