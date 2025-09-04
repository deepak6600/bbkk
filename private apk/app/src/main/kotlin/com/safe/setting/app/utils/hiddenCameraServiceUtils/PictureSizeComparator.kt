package com.safe.setting.app.utils.hiddenCameraServiceUtils

import android.util.Size

internal class PictureSizeComparator : Comparator<Size> {
    override fun compare(a: Size, b: Size): Int {
        return b.height * b.width - a.height * a.width
    }
}