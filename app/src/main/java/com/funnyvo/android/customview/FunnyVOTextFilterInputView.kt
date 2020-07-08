package com.funnyvo.android.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.funnyvo.android.R
import kotlinx.android.synthetic.main.view_video_input_message.view.*

class FunnyVOTextFilterInputView(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_video_input_message, this, false)
        btnTextAdded.setOnClickListener {
            
        }
    }

}