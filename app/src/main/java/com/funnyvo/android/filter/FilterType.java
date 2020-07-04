package com.funnyvo.android.filter;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.daasuu.gpuv.egl.filter.GlBilateralFilter;
import com.daasuu.gpuv.egl.filter.GlBoxBlurFilter;
import com.daasuu.gpuv.egl.filter.GlBrightnessFilter;
import com.daasuu.gpuv.egl.filter.GlBulgeDistortionFilter;
import com.daasuu.gpuv.egl.filter.GlCGAColorspaceFilter;
import com.daasuu.gpuv.egl.filter.GlContrastFilter;
import com.daasuu.gpuv.egl.filter.GlCrosshatchFilter;
import com.daasuu.gpuv.egl.filter.GlExposureFilter;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;
import com.daasuu.gpuv.egl.filter.GlGammaFilter;
import com.daasuu.gpuv.egl.filter.GlGaussianBlurFilter;
import com.daasuu.gpuv.egl.filter.GlGrayScaleFilter;
import com.daasuu.gpuv.egl.filter.GlHalftoneFilter;
import com.daasuu.gpuv.egl.filter.GlHazeFilter;
import com.daasuu.gpuv.egl.filter.GlHighlightShadowFilter;
import com.daasuu.gpuv.egl.filter.GlHueFilter;
import com.daasuu.gpuv.egl.filter.GlInvertFilter;
import com.daasuu.gpuv.egl.filter.GlLuminanceFilter;
import com.daasuu.gpuv.egl.filter.GlLuminanceThresholdFilter;
import com.daasuu.gpuv.egl.filter.GlMonochromeFilter;
import com.daasuu.gpuv.egl.filter.GlOpacityFilter;
import com.daasuu.gpuv.egl.filter.GlOverlayFilter;
import com.daasuu.gpuv.egl.filter.GlPixelationFilter;
import com.daasuu.gpuv.egl.filter.GlPosterizeFilter;
import com.daasuu.gpuv.egl.filter.GlRGBFilter;
import com.daasuu.gpuv.egl.filter.GlSaturationFilter;
import com.daasuu.gpuv.egl.filter.GlSepiaFilter;
import com.daasuu.gpuv.egl.filter.GlSharpenFilter;
import com.daasuu.gpuv.egl.filter.GlSolarizeFilter;
import com.daasuu.gpuv.egl.filter.GlSphereRefractionFilter;
import com.daasuu.gpuv.egl.filter.GlSwirlFilter;
import com.daasuu.gpuv.egl.filter.GlToneCurveFilter;
import com.daasuu.gpuv.egl.filter.GlToneFilter;
import com.daasuu.gpuv.egl.filter.GlVibranceFilter;
import com.daasuu.gpuv.egl.filter.GlVignetteFilter;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
import com.daasuu.gpuv.egl.filter.GlWeakPixelInclusionFilter;
import com.daasuu.gpuv.egl.filter.GlZoomBlurFilter;
import com.funnyvo.android.R;
import com.funnyvo.android.filter.addons.FunnyVOUserOverlayFilter;
import com.funnyvo.android.simpleclasses.Variables;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.funnyvo.android.simpleclasses.Variables.APP_NAME;

// this is the all available filters
public enum FilterType {
    DEFAULT,
    BRIGHTNESS,
    EXPOSURE,
    FILTER_GROUP_SAMPLE,
    GAMMA,
    GRAY_SCALE,
    HAZE,
    HIGHLIGHT_SHADOW,
    HUE,
    INVERT,
    LUMINANCE,
    MONOCHROME,
    OPACITY,
    PIXELATION,
    POSTERIZE,
    RGB,
    SATURATION,
    SEPIA,
    SHARP,
    BILATERAL_BLUR,
    BOX_BLUR,
    BULGE_DISTORTION,
    CGA_COLORSPACE,
    CONTRAST,
    CROSSHATCH,
    GAUSSIAN_FILTER,
    HALFTONE,
    LUMINANCE_THRESHOLD,
    SOLARIZE,
    SPHERE_REFRACTION,
    SWIRL,
    TONE_CURVE_SAMPLE,
    TONE,
    VIBRANCE,
    VIGNETTE,
    WEAK_PIXEL,
    ZOOM_BLUR;

    public static List<FilterType> createFilterList() {
        return Arrays.asList(FilterType.values());
    }

    public static Collection<GlFilter> createGlFilter(FilterType filterType, Context context, Bitmap bitmap) {
        switch (filterType) {
            case DEFAULT:
                GlFilter filter = new GlFilter();
                return returnWithWaterMarkToFilters(filter, bitmap);
            case BILATERAL_BLUR:
                GlBilateralFilter bilateralFilter = new GlBilateralFilter();
                return returnWithWaterMarkToFilters(bilateralFilter, bitmap);
            case BOX_BLUR:
                GlBoxBlurFilter boxBlurFilter = new GlBoxBlurFilter();
                return returnWithWaterMarkToFilters(boxBlurFilter, bitmap);
            case BRIGHTNESS:
                GlBrightnessFilter glBrightnessFilter = new GlBrightnessFilter();
                glBrightnessFilter.setBrightness(0.1f);
                return returnWithWaterMarkToFilters(glBrightnessFilter, bitmap);
            case BULGE_DISTORTION:
                GlBulgeDistortionFilter bulgeDistortionFilter = new GlBulgeDistortionFilter();
                return returnWithWaterMarkToFilters(bulgeDistortionFilter, bitmap);
            case CGA_COLORSPACE:
                GlCGAColorspaceFilter glCGAColorspaceFilter = new GlCGAColorspaceFilter();
                return returnWithWaterMarkToFilters(glCGAColorspaceFilter, bitmap);
            case CONTRAST:
                GlContrastFilter glContrastFilter = new GlContrastFilter();
                glContrastFilter.setContrast(2.5f);
                return returnWithWaterMarkToFilters(glContrastFilter, bitmap);
            case CROSSHATCH:
                GlCrosshatchFilter glCrosshatchFilter = new GlCrosshatchFilter();
                return returnWithWaterMarkToFilters(glCrosshatchFilter, bitmap);
            case EXPOSURE:
                GlExposureFilter glExposureFilter = new GlExposureFilter();
                return returnWithWaterMarkToFilters(glExposureFilter, bitmap);
            case FILTER_GROUP_SAMPLE:
                GlFilterGroup filterGroup = new GlFilterGroup(new GlSepiaFilter(), new GlVignetteFilter());
                return returnWithWaterMarkToFilters(filterGroup, bitmap);
            case GAMMA:
                GlGammaFilter glGammaFilter = new GlGammaFilter();
                glGammaFilter.setGamma(2f);
                return returnWithWaterMarkToFilters(glGammaFilter, bitmap);
            case GAUSSIAN_FILTER:
                GlGaussianBlurFilter glGaussianBlurFilter = new GlGaussianBlurFilter();
                return returnWithWaterMarkToFilters(glGaussianBlurFilter, bitmap);
            case GRAY_SCALE:
                GlGrayScaleFilter glGrayScaleFilter = new GlGrayScaleFilter();
                return returnWithWaterMarkToFilters(glGrayScaleFilter, bitmap);
            case HALFTONE:
                GlHalftoneFilter glHalftoneFilter = new GlHalftoneFilter();
                return returnWithWaterMarkToFilters(glHalftoneFilter, bitmap);
            case HAZE:
                GlHazeFilter glHazeFilter = new GlHazeFilter();
                glHazeFilter.setSlope(-0.5f);
                return returnWithWaterMarkToFilters(glHazeFilter, bitmap);
            case HIGHLIGHT_SHADOW:
                GlHighlightShadowFilter glHighlightShadowFilter = new GlHighlightShadowFilter();
                return returnWithWaterMarkToFilters(glHighlightShadowFilter, bitmap);
            case HUE:
                GlHueFilter glHueFilter = new GlHueFilter();
                return returnWithWaterMarkToFilters(glHueFilter, bitmap);
            case INVERT:
                GlInvertFilter glInvertFilter = new GlInvertFilter();
                return returnWithWaterMarkToFilters(glInvertFilter, bitmap);
            case LUMINANCE:
                GlLuminanceFilter glLuminanceFilter = new GlLuminanceFilter();
                return returnWithWaterMarkToFilters(glLuminanceFilter, bitmap);
            case LUMINANCE_THRESHOLD:
                GlLuminanceThresholdFilter glLuminanceThresholdFilter = new GlLuminanceThresholdFilter();
                return returnWithWaterMarkToFilters(glLuminanceThresholdFilter, bitmap);
            case MONOCHROME:
                GlMonochromeFilter glMonochromeFilter = new GlMonochromeFilter();
                return returnWithWaterMarkToFilters(glMonochromeFilter, bitmap);
            case OPACITY:
                GlOpacityFilter glOpacityFilter = new GlOpacityFilter();
                return returnWithWaterMarkToFilters(glOpacityFilter, bitmap);
            case PIXELATION:
                GlPixelationFilter glPixelationFilter = new GlPixelationFilter();
                return returnWithWaterMarkToFilters(glPixelationFilter, bitmap);
            case POSTERIZE:
                GlPosterizeFilter glPosterizeFilter = new GlPosterizeFilter();
                return returnWithWaterMarkToFilters(glPosterizeFilter, bitmap);
            case RGB:
                GlRGBFilter glRGBFilter = new GlRGBFilter();
                glRGBFilter.setRed(0f);
                return returnWithWaterMarkToFilters(glRGBFilter, bitmap);
            case SATURATION:
                GlSaturationFilter glSaturationFilter = new GlSaturationFilter();
                return returnWithWaterMarkToFilters(glSaturationFilter, bitmap);
            case SEPIA:
                GlSepiaFilter glSepiaFilter = new GlSepiaFilter();
                return returnWithWaterMarkToFilters(glSepiaFilter, bitmap);
            case SHARP:
                GlSharpenFilter glSharpenFilter = new GlSharpenFilter();
                glSharpenFilter.setSharpness(3f);
                return returnWithWaterMarkToFilters(glSharpenFilter, bitmap);
            case SOLARIZE:
                GlSolarizeFilter glSolarizeFilter = new GlSolarizeFilter();
                return returnWithWaterMarkToFilters(glSolarizeFilter, bitmap);
            case SPHERE_REFRACTION:
                GlSphereRefractionFilter glSphereRefractionFilter = new GlSphereRefractionFilter();
                return returnWithWaterMarkToFilters(glSphereRefractionFilter, bitmap);
            case SWIRL:
                GlSwirlFilter swirlFilter = new GlSwirlFilter();
                return returnWithWaterMarkToFilters(swirlFilter, bitmap);
            case TONE_CURVE_SAMPLE:
                try {
                    InputStream is = context.getAssets().open("acv/tone_cuver_sample.acv");
                    GlToneCurveFilter glToneCurveFilter = new GlToneCurveFilter(is);
                    return returnWithWaterMarkToFilters(glToneCurveFilter, bitmap);
                } catch (IOException e) {
                    Log.e("FilterType", "Error");
                }
                return returnWithWaterMarkToFilters(new GlFilter(), bitmap);
            case TONE:
                return returnWithWaterMarkToFilters(new GlToneFilter(), bitmap);

            case VIBRANCE:
                GlVibranceFilter glVibranceFilter = new GlVibranceFilter();
                glVibranceFilter.setVibrance(3f);
                return returnWithWaterMarkToFilters(glVibranceFilter, bitmap);
            case VIGNETTE:
                return returnWithWaterMarkToFilters(new GlVignetteFilter(), bitmap);

            case WEAK_PIXEL:
                return returnWithWaterMarkToFilters(new GlWeakPixelInclusionFilter(), bitmap);

            case ZOOM_BLUR:
                return returnWithWaterMarkToFilters(new GlZoomBlurFilter(), bitmap);

            default:
                return returnWithWaterMarkToFilters(new GlFilter(), bitmap);
        }
    }

    public static Collection<GlFilter> createGlFilterWithOverlay(FilterType filterType, Context context, Bitmap bitmap) {

        Collection<GlFilter> filters = createGlFilter(filterType, context, bitmap);
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File filePath = new File(directory, context.getString(R.string.user_profile));
        Bitmap userBitmap = BitmapFactory.decodeFile(filePath.getPath());
        filters.add(new FunnyVOUserOverlayFilter(userBitmap));
        return filters;
    }

    private static List<GlFilter> returnWithWaterMarkToFilters(GlFilter filter, Bitmap bitmap) {
        ArrayList<GlFilter> filters = new ArrayList<>();
        GlWatermarkFilter watermarkFilter = new GlWatermarkFilter(bitmap, GlWatermarkFilter.Position.RIGHT_BOTTOM);
        filters.add(filter);
        filters.add(watermarkFilter);

        return filters;
    }
}