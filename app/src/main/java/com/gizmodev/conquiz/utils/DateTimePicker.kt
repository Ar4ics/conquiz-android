package com.gizmodev.conquiz.utils

import java.text.SimpleDateFormat
import java.util.*

class DateTimePicker {
    companion object {
        fun getCurrentDateTime(): Date {
            return Calendar.getInstance().time
        }
    }
}

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}