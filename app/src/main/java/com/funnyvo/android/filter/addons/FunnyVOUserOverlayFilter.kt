package com.funnyvo.android.filter.addons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.daasuu.gpuv.egl.filter.GlOverlayFilter
import com.funnyvo.android.simpleclasses.Variables

class FunnyVOUserOverlayFilter(val bitmap: Bitmap) : GlOverlayFilter() {
    override fun drawCanvas(canvas: Canvas?) {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 18.0F
        if (bitmap != null && !bitmap.isRecycled) {
            canvas?.drawBitmap(bitmap, canvas.width / 2.0F, canvas.height / 2.0F, null)
        }
    }

    override fun release() {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle();
        }
        super.release()
    }

}
