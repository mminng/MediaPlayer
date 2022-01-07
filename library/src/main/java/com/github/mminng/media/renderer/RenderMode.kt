package com.github.mminng.media.renderer

/**
 * Created by zh on 2021/10/5.
 */
sealed class RenderMode {

    object FIT : RenderMode()
    object FILL : RenderMode()
    object ZOOM : RenderMode()
    object DEFAULT : RenderMode()

}