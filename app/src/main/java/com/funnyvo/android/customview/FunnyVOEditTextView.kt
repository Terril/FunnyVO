package com.funnyvo.android.customview

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class FunnyVOEditTextView(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    init {
        init()
    }
    private fun init() {
        if (!isInEditMode) {
            val tf = Typeface.createFromAsset(context.assets, "latoregular.ttf")
            typeface = tf
        }
    }
}