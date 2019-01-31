package com.gizmodev.conquiz.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationInterceptor @Inject constructor() : Interceptor {

    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        builder.header("Accept", "application/json")
        Timber.d("token = $token")
        if (token != null) {
            builder.header("Authorization", "Bearer $token")
        }
        val request = builder.build()
        return chain.proceed(request)
    }
}