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
    val prefix = if (timeMs < 0) "-" else ""
    timeMs = abs(timeMs)
    val totalSeconds = (timeMs + 500) / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600
    builder.setLength(0)
    return if (hours > 0) formatter.format("%s%d:%02d:%02d", prefix, hours, minutes, seconds)
        .toString() else formatter.format("%s%02d:%02d", prefix, minutes, seconds).toString()
}