package com.github.mminng.media.player.state

/**
 * Created by zh on 2021/12/5.
 */
sealed class PlayerState {

    object IDLE : PlayerState()
    object BUFFERING : PlayerState()
    object BUFFERED : PlayerState()
    object PREPARED : PlayerState()
    object STARTED : PlayerState()
    object PAUSED : PlayerState()
    object COMPLETED : PlayerState()
    object ERROR : PlayerState()

}
