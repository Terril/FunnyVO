package com.funnyvo.android.filter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.funnyvo.android.R
import com.otaliastudios.cameraview.filter.Filters
import kotlinx.android.synthetic.main.item_camera_filter_layout.view.*


class CameraFilterAdapter(context: Context?) : RecyclerView.Adapter<CameraFilterAdapter.CustomViewHolder>() {
    var context: Context? = null
    lateinit var datalist: List<Filters>
    lateinit var listener: OnItemClickListener
    lateinit var icon: Bitmap

    interface OnItemClickListener {
        fun onItemClick(view: View?, postion: Int, item: Filters)
    }

    constructor(context: Context?, icon: Bitmap, arrayList: List<Filters>, listener: OnItemClickListener) : this(context) {
        this.context = context
        datalist = arrayList
        this.listener = listener
        this.icon = icon
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_camera_filter_layout, viewGroup, false)
        view.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtFilter = view.txtfilter
        val imgPhoto = view.imvfilterPhoto
        fun bind(pos: Int, item: Filters, listener: OnItemClickListener?) {
            itemView.setOnClickListener { v -> listener?.onItemClick(v, pos, item) }
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val filterName = datalist[i].name
        holder.txtFilter.text = filterName.replace("_", " ")
//        if (PreviewVideoActivity.select_postion == i) {
//            holder.ivPhoto.setBackgroundColor(context!!.resources.getColor(R.color.redcolor))
//        }
        //holder.imgPhoto.setImageBitmap(datalist[i].newInstance().fragmentShader)

        holder.imgPhoto.setImage(icon)
        holder.bind(i, datalist[i], listener)
    }

}