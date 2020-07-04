package com.funnyvo.android.filter.addons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.daasuu.gpuv.egl.filter.GlOverlayFilter
import com.funnyvo.android.simpleclasses.Variables

class FunnyVOUserOverlayFilter(val bitmap: Bitmap) : GlOverlayFilter() {
    val name: String? = Variables.sharedPreferences.getString(Variables.f_name, "") + Variables.sharedPreferences.getString(Variables.l_name, "")

    val userName: String? = Variables.sharedPreferences.getString(Variables.u_name, "")
    override fun drawCanvas(canvas: Canvas?) {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 18.0F
        if (bitmap != null && !bitmap.isRecycled) {
            canvas?.drawBitmap(bitmap, canvas.width / 2.0F, canvas.height / 2.0F, null)
            canvas?.drawText(name.orEmpty(), canvas.width / 2.0F, (canvas.height / 2.0F) + 15.0F, paint)
            paint.style = Paint.Style.STROKE
            paint.textSize = 15.0F
            canvas?.drawText(userName.orEmpty(), canvas.width / 2.0F, (canvas.height / 2.0F) + 15.0F, paint)
        }
    }

    override fun release() {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle();
        }
        super.release()
    }

}
