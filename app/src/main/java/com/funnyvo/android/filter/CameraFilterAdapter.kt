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
import jp.co.cyberagent.android.gpuimage.filter.*
import kotlinx.android.synthetic.main.item_camera_filter_layout.view.*


class CameraFilterAdapter(context: Context?) : RecyclerView.Adapter<CameraFilterAdapter.CustomViewHolder>() {
    var context: Context? = null
    lateinit var datalist: List<Filters>
    lateinit var listener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(view: View?, postion: Int, item: Filters)
    }

    constructor(context: Context?, arrayList: List<Filters>, listener: OnItemClickListener) : this(context) {
        this.context = context
        datalist = arrayList
        this.listener = listener
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
//            holder.imgPhoto.setBackgroundColor(context!!.resources.getColor(R.color.redcolor))
//        }
        //holder.imgPhoto.setImageBitmap(datalist[i].newInstance().fragmentShader)
        val icon = BitmapFactory.decodeResource(context?.resources,
                R.drawable.image_filter_holder)
        holder.imgPhoto.setImage(icon)
        when (filterName) {
            "BRIGHTNESS" -> holder.imgPhoto.filter = GPUImageBrightnessFilter()
            "AUTOFIX" -> holder.imgPhoto.filter = GPUImageExposureFilter()
            "BLACK_AND_WHITE" -> holder.imgPhoto.filter = GPUImageVignetteFilter()
            "GAMMA" -> holder.imgPhoto.filter = GPUImageGammaFilter()
            "GRAYSCALE" -> holder.imgPhoto.filter = GPUImageGrayscaleFilter()
            "HAZE" -> holder.imgPhoto.filter = GPUImageHazeFilter()
            "DOCUMENTARY" -> holder.imgPhoto.filter = GPUImageHighlightShadowFilter()
            "HUE" -> holder.imgPhoto.filter = GPUImageHueFilter()
            "INVERT_COLORS" -> holder.imgPhoto.filter = GPUImageColorInvertFilter()
            "LUMINANCE" -> holder.imgPhoto.filter = GPUImageLuminanceFilter()
            "MONOCHROME" -> holder.imgPhoto.filter = GPUImageMonochromeFilter()
            "DUOTONE" -> holder.imgPhoto.filter = GPUImageOpacityFilter()
            "FILL_LIGHT" -> holder.imgPhoto.filter = GPUImagePixelationFilter()
            "POSTERIZE" -> holder.imgPhoto.filter = GPUImagePosterizeFilter()
            "SATURATION" -> holder.imgPhoto.filter = GPUImageSaturationFilter()
            "SEPIA" -> holder.imgPhoto.filter = GPUImageSepiaToneFilter()
            "SHARPNESS" -> holder.imgPhoto.filter = GPUImageSharpenFilter()
            "CONTRAST" -> holder.imgPhoto.filter = GPUImageContrastFilter()
            "CROSS_PROCESS" -> holder.imgPhoto.filter= GPUImageCrosshatchFilter()
            "GRAIN" -> holder.imgPhoto.filter = GPUImageGaussianBlurFilter()
            "LOMOISH" -> holder.imgPhoto.filter = GPUImageSolarizeFilter()
            "TEMPERATURE" -> holder.imgPhoto.filter = GPUImageVibranceFilter()
            "VIGNETTE" -> holder.imgPhoto.filter = GPUImageVignetteFilter()

        }
        holder.bind(i, datalist[i], listener)
    }

}