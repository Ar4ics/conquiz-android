package com.gizmodev.conquiz.network

import com.gizmodev.conquiz.model.UserLogin
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginApi {
    @FormUrlEncoded
    @POST("auth/google")
    fun getToken(
        @Field("code") code: String
    ): Observable<Response<Any>>

    @GET("auth/refresh")
    fun refreshToken(): Observable<Response<Any>>

    @GET("auth/user")
    fun getUser(): Observable<UserLogin>

    @POST("auth/logout")
    fun logout(): Observable<Response<Any>>
}