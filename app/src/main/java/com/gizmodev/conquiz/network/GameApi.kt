package com.gizmodev.conquiz.network

import com.gizmodev.conquiz.model.GameDetails
import com.gizmodev.conquiz.model.GamesInfo
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface GameApi {
    @GET("games")
    fun getGames(): Observable<GamesInfo>

    @GET("games/{id}")
    fun getGame(@Path(value = "id", encoded = true) id: Int): Observable<GameDetails>

    @FormUrlEncoded
    @POST("games/{id}/base/box/clicked")
    fun boxClick(
        @Path(value = "id", encoded = true) id: Int,
        @Field("x") x: Int,
        @Field("y") y: Int,
        @Field("userColorId") userColorId: Int
    ): Observable<Response<Any>>

    @FormUrlEncoded
    @POST("games/{id}/base/user/answered")
    fun questionAnswer(@Path(value = "id", encoded = true) id: String): Observable<Response<Any>>
}