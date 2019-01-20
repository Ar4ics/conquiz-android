package com.gizmodev.conquiz.utils

import android.content.Context

class SharedPrefStorage(private val context: Context) {

    fun writeToken(message: String): Boolean {
        return context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
            .edit().putString(Constants.TOKEN, message).commit()
    }

    fun removeToken(): Boolean {
        return context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
            .edit().remove(Constants.TOKEN).commit()
    }

    fun readToken(): String? {
        return context.getSharedPreferences(
            Constants.SHARED_PREFS,
            Context.MODE_PRIVATE
        ).getString(Constants.TOKEN, "")
    }
}