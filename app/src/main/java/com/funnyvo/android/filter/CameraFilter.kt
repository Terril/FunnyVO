package com.funnyvo.android.filter

import com.otaliastudios.cameraview.filter.Filter
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.filter.NoFilter
import com.otaliastudios.cameraview.filters.*


object CameraFilter {
    fun createFilterList(): List<Filters> {
        return Filters.values().toList()
    }

    fun createFilter(filterType: Filters): Filter {
        return when (filterType) {
            Filters.NONE -> {
                NoFilter()
            }
            Filters.AUTO_FIX -> {
                AutoFixFilter()
            }
            Filters.BLACK_AND_WHITE -> {
                BlackAndWhiteFilter()
            }
            Filters.BRIGHTNESS -> {
                BrightnessFilter()
            }
            Filters.CONTRAST -> {
                ContrastFilter()
            }
            Filters.CROSS_PROCESS -> {
                CrossProcessFilter()
            }
            Filters.DOCUMENTARY -> {
                DocumentaryFilter()
            }
            Filters.DUOTONE -> {
                DuotoneFilter()
            }
            Filters.FILL_LIGHT -> {
                FillLightFilter()
            }
            Filters.GAMMA -> {
                GammaFilter()
            }
            Filters.GRAIN -> {
                GrainFilter()
            }
            Filters.GRAYSCALE -> {
                GrayscaleFilter()
            }
            Filters.INVERT_COLORS -> {
                InvertColorsFilter()
            }
            Filters.HUE -> {
                HueFilter()
            }
            Filters.LOMOISH -> {
                LomoishFilter()
            }
            Filters.POSTERIZE -> {
                PosterizeFilter()
            }
            Filters.SATURATION -> {
                SaturationFilter()
            }
            Filters.SEPIA -> {
                SepiaFilter()
            }
            Filters.SHARPNESS -> {
                SharpnessFilter()
            }
            Filters.TEMPERATURE -> {
                TemperatureFilter()
            }
            Filters.TINT -> {
                TintFilter()
            }
            Filters.VIGNETTE -> {
                VignetteFilter()
            }
        }
    }

//        open fun createGlFilterWithOverlay(filterType: FilterType?, context: Context, bitmap: Bitmap): Collection<Filter?>? {
//            val filters = createGlFilter(filterType, context, bitmap)
//            filters.add(FunnyVOUserOverlayFilter(bitmap))
//            return filters
//        }


}