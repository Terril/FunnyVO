package com.funnyvo.android.videorecording.photoeditor

/**
 * Created on 1/17/2018.
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 *
 *
 */
interface BrushViewChangeListener {
    fun onViewAdd(brushDrawingView: BrushDrawingView?)
    fun onViewRemoved(brushDrawingView: BrushDrawingView?)
    fun onStartDrawing()
    fun onStopDrawing()
}