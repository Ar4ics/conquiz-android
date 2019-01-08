package com.gizmodev.conquiz.utils

import android.util.Log
import com.gizmodev.conquiz.utils.Constants.MAIN_APP
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationInterceptor @Inject constructor() : Interceptor {

    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        builder.header("Content-Type", "application/json");
        Log.d(MAIN_APP, "token=$token")
        if (token != null) {
            builder.header("Authorization", "Bearer $token")
        }
        val request = builder.build()
        return chain.proceed(request)
    }
}