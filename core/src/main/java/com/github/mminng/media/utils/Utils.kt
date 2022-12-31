package com.github.mminng.media.utils

import java.util.*
import kotlin.math.abs

/**
 * Created by zh on 2021/10/3.
 */
fun getStringForTime(builder: StringBuilder, formatter: Formatter, time: Long): String {
    var timeMs = time
    if (timeMs < 0) {
        timeMs = 0
    }
    timeMs = abs(timeMs)
    val totalSeconds = (timeMs + 500) / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600
    builder.setLength(0)
    return if (hours > 0) formatter.format("%d:%02d:%02d", hours, minutes, seconds)
        .toString() else formatter.format("%02d:%02d", minutes, seconds).toString()
}