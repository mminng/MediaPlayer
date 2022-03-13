package com.github.mminng.media.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by zh on 2022/2/1.
 */
@SuppressLint("AppCompatCustomView")
internal class MarqueeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextView(context, attrs) {

    override fun isFocused(): Boolean {
        return true
    }

    fun marquee() {
        ellipsize = TextUtils.TruncateAt.MARQUEE
    }

    fun cancel() {
        ellipsize = TextUtils.TruncateAt.END
    }
}