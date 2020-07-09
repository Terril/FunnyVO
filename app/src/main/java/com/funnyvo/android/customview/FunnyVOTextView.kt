package com.funnyvo.android.customview

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import com.funnyvo.android.R
import com.funnyvo.android.simpleclasses.Variables.APP_NAME


class FunnyVOTextView(context: Context?, val attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    init {
        init()
    }

    private fun init() {
        if (attrs != null) {
            setCustomFont(context, attrs)
        }
    }

    private fun setCustomFont(ctx: Context, attrs: AttributeSet) {
        val a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomTextView)
        val customFont = a.getString(R.styleable.CustomTextView_customFont)
        setCustomFont(ctx, customFont)
        a.recycle()
    }

    fun setCustomFont(ctx: Context, fontName: String?): Boolean {
        var fontName = fontName
        val typeface: Typeface?
        try {
            if (fontName == null) {
                fontName = "latoregular.ttf"
            }
            typeface = Typeface.createFromAsset(ctx.assets, fontName)
        } catch (e: Exception) {
            Log.e(APP_NAME, "Unable to load typeface: " + e.message)
            return false
        }
        setTypeface(typeface)
        return true
    }
}