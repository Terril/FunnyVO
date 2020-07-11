package com.funnyvo.android.filter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.funnyvo.android.R
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.filter.NoFilter
import com.otaliastudios.cameraview.filters.HueFilter
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
//            holder.ivPhoto.setBackgroundColor(context!!.resources.getColor(R.color.redcolor))
//        }
//        when (filterName) {
//            "BRIGHTNESS" -> holder.ivPhoto.filter = GPUImageBrightnessFilter()
//            "EXPOSURE" -> holder.ivPhoto.filter = GPUImageExposureFilter()
//            "FILTER_GROUP_SAMPLE" -> holder.ivPhoto.filter = GPUImageVignetteFilter()
//            "GAMMA" -> holder.ivPhoto.filter = GPUImageGammaFilter()
//            "GRAY_SCALE" -> holder.ivPhoto.filter = GPUImageGrayscaleFilter()
//            "HAZE" -> holder.ivPhoto.filter = GPUImageHazeFilter()
//            "HIGHLIGHT_SHADOW" -> holder.ivPhoto.filter = GPUImageHighlightShadowFilter()
//            "HUE" -> holder.ivPhoto.filter = GPUImageHueFilter()
//            "INVERT" -> holder.ivPhoto.filter = GPUImageColorInvertFilter()
//            "LUMINANCE" -> holder.ivPhoto.filter = GPUImageLuminanceFilter()
//            "MONOCHROME" -> holder.ivPhoto.filter = GPUImageMonochromeFilter()
//            "OPACITY" -> holder.ivPhoto.filter = GPUImageOpacityFilter()
//            "PIXELATION" -> holder.ivPhoto.filter = GPUImagePixelationFilter()
//            "POSTERIZE" -> holder.ivPhoto.filter = GPUImagePosterizeFilter()
//            "RGB" -> holder.ivPhoto.filter = GPUImageRGBFilter()
//            "SATURATION" -> holder.ivPhoto.filter = GPUImageSaturationFilter()
//            "SEPIA" -> holder.ivPhoto.filter = GPUImageSepiaToneFilter()
//            "SHARP" -> holder.ivPhoto.filter = GPUImageSharpenFilter()
//            "BILATERAL_BLUR" -> holder.ivPhoto.filter = GPUImageBilateralBlurFilter()
//            "BOX_BLUR" -> holder.ivPhoto.filter = GPUImageBoxBlurFilter()
//            "BULGE_DISTORTION" -> holder.ivPhoto.filter = GPUImageLuminanceFilter()
//            "CGA_COLORSPACE" -> holder.ivPhoto.filter = GPUImageCGAColorspaceFilter()
//            "CONTRAST" -> holder.ivPhoto.filter = GPUImageContrastFilter()
//            "CROSSHATCH" -> holder.ivPhoto.filter = GPUImageCrosshatchFilter()
//            "GAUSSIAN_FILTER" -> holder.ivPhoto.filter = GPUImageGaussianBlurFilter()
//            "HALFTONE" -> holder.ivPhoto.filter = GPUImageHalftoneFilter()
//            "LUMINANCE_THRESHOLD" -> holder.ivPhoto.filter = GPUImageLuminanceThresholdFilter()
//            "SOLARIZE" -> holder.ivPhoto.filter = GPUImageSolarizeFilter()
//            "SPHERE_REFRACTION" -> holder.ivPhoto.filter = GPUImageSphereRefractionFilter()
//            "SWIRL" -> holder.ivPhoto.filter = GPUImageSwirlFilter()
//            "TONE" -> holder.ivPhoto.filter = GPUImageToneCurveFilter()
//            "VIBRANCE" -> holder.ivPhoto.filter = GPUImageVibranceFilter()
//            "VIGNETTE" -> holder.ivPhoto.filter = GPUImageVignetteFilter()
//            "WEAK_PIXEL" -> holder.ivPhoto.filter = GPUImageWeakPixelInclusionFilter()
//            "ZOOM_BLUR" -> holder.ivPhoto.filter = GPUImageBoxBlurFilter()
//        }
        holder.bind(i, datalist[i], listener)
    }

}