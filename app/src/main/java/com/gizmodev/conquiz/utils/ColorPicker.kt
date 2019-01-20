package com.gizmodev.conquiz.utils

import android.graphics.Color
import androidx.annotation.ColorInt

object ColorPicker {

    @ColorInt
    @JvmStatic
    fun pick(color: String?): Int =
        when (color) {
            "LightPink" -> Color.RED
            "LightGreen" -> Color.GREEN
            "LightBlue" -> Color.BLUE
            null -> Color.WHITE
            else -> {
                try {
                    Color.parseColor(color)
                } catch (e: Throwable) {
                    Color.YELLOW
                }
            }
        }
}
