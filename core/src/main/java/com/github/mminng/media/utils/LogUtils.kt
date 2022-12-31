package com.github.mminng.media.utils

import android.util.Log
import com.github.mminng.media.BuildConfig

/**
 * Created by zh on 2021/10/1.
 */
private const val TAG: String = "PlayerDebug"

internal fun d(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TAG, message)
    }
}

internal fun e(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(TAG, message)
    }
}