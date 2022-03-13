package com.github.mminng.media.player

/**
 * Created by zh on 2022/2/3.
 */
sealed class PlayerOrientation {
    internal object NONE : PlayerOrientation()
    internal object UNSPECIFIED : PlayerOrientation()
    object LANDSCAPE : PlayerOrientation()
    object PORTRAIT : PlayerOrientation()
}