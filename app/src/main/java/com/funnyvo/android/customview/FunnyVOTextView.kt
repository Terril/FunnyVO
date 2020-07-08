package com.funnyvo.android.customview

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class FunnyVOTextView(context: Context?, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    init {
        init()
    }

    private fun init() {
        val tf = Typeface.createFromAsset(context.assets, "latoregular.ttf")
        typeface = tf

    }
}