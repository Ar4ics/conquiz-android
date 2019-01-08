package com.gizmodev.conquiz.network

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface LoginApi {
    @FormUrlEncoded
    @POST("auth/google")
    fun getToken(
        @Field("code") code: String
    ): Observable<Response<Any>>

    @GET("auth/refresh")
    fun refreshToken(@Header("Authorization") token: String): Observable<Response<Any>>
}