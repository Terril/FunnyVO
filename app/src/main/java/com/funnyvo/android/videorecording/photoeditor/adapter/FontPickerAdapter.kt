package com.funnyvo.android.videorecording.photoeditor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.funnyvo.android.R

class FontPickerAdapter internal constructor(private val context: Context, selectedPos: Int, fontPickerFonts: List<String>, fontIds: List<Int>) : RecyclerView.Adapter<FontPickerAdapter.ViewHolder>() {
    private val inflater: LayoutInflater
    private val fontPickerFonts: List<String>
    private val fontId: List<Int>
    var selecetedPosition = 0
    private var onFontSelectListner: OnFontSelectListner? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_font_select, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.name.text = fontPickerFonts[i]
        val typeface = ResourcesCompat.getFont(context, fontId[i])
        viewHolder.name.typeface = typeface
        if (selecetedPosition == i) {
            viewHolder.name.setBackgroundColor(ContextCompat.getColor(context, R.color.palette_cheddar_dark))
        } else {
            viewHolder.name.setBackgroundColor(ContextCompat.getColor(context, R.color.black_trasp))
        }
    }

    override fun getItemCount(): Int {
        return fontPickerFonts.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: Button = itemView.findViewById(R.id.btnFontName)

        init {
            name.setOnClickListener {
                if (adapterPosition != selecetedPosition) {
                    if (onFontSelectListner != null) {
                        selecetedPosition = adapterPosition
                        name.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        onFontSelectListner!!.onFontSelcetion(adapterPosition)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun setOnFontSelectListener(onFontSelectListner: OnFontSelectListner?) {
        this.onFontSelectListner = onFontSelectListner
    }

    interface OnFontSelectListner {
        fun onFontSelcetion(position: Int)
    }

    init {
        selecetedPosition = selectedPos
        inflater = LayoutInflater.from(context)
        fontId = fontIds
        this.fontPickerFonts = fontPickerFonts
    }
}