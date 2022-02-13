package com.github.mminng.media.utils

import java.util.*

/**
 * Created by zh on 2021/10/3.
 */
fun convertMillis(duration: Int): String {
    if (duration <= 0) {
        return "00:00"
    }
    val formatter = Formatter(StringBuilder(), Locale.getDefault())
    val totalSeconds = duration / 1000
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return when {
        hours >= 100 -> {
            formatter
                .format("%d:%02d:%02d", hours, minutes, seconds)
                .toString()
        }
        hours > 0 -> {
            formatter
                .format("%02d:%02d:%02d", hours, minutes, seconds)
                .toString()
        }
        else -> {
            formatter
                .format("%02d:%02d", minutes, seconds)
                .toString()
        }
    }
}