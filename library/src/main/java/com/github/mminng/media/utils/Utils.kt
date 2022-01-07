package com.github.mminng.media.utils

import com.github.mminng.media.renderer.RenderMode
import java.util.*
import kotlin.math.abs

/**
 * Created by zh on 2021/10/3.
 */
fun convertMillis(timeMs: Int): String {
    val builder: StringBuilder = StringBuilder()
    val formatter = Formatter(builder, Locale.getDefault())
    var position = timeMs
    if (position < 0) {
        position = 0
    }
    val prefix = if (position < 0) "-" else ""
    position = abs(position)
    val totalSeconds: Int = (position + 500) / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600
    builder.setLength(0)
    return if (hours > 0) formatter.format("%s%d:%02d:%02d", prefix, hours, minutes, seconds)
        .toString() else formatter.format("%s%02d:%02d", prefix, minutes, seconds).toString()
}