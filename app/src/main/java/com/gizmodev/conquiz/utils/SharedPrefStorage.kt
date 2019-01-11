package com.gizmodev.conquiz.utils

import android.content.Context
import io.reactivex.Observable

class SharedPrefStorage(private val context: Context) {

    fun writeToken(message: String) {
        context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
            .edit().putString(Constants.TOKEN, message).apply();
    }

    fun removeToken() {
        context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
            .edit().remove(Constants.TOKEN).apply();
    }

    fun readToken(): Observable<String> {
        return Observable.defer {
            val token =
                context.getSharedPreferences(
                    Constants.SHARED_PREFS,
                    Context.MODE_PRIVATE
                ).getString(Constants.TOKEN, "")
            Observable.just(token)
        }
    }

}