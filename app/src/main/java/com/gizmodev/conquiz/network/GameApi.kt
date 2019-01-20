package com.gizmodev.conquiz.network

import com.gizmodev.conquiz.model.GameDetails
import com.gizmodev.conquiz.model.GamesInfo
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GameApi {
    @GET("games")
    fun getGames(): Single<GamesInfo>

    @GET("games/{id}")
    fun getGame(@Path(value = "id", encoded = true) id: Int): Single<GameDetails>

    @FormUrlEncoded
    @POST("games/{id}/base/box/clicked")
    fun boxClick(
        @Path(value = "id", encoded = true) id: Int,
        @Field("x") x: Int,
        @Field("y") y: Int,
        @Field("userColorId") userColorId: Int
    ): Single<Response<ResponseBody>>

    @FormUrlEncoded
    @POST("games/{id}/base/user/answered")
    fun questionAnswer(
        @Path(value = "id", encoded = true) id: Int,
        @Field("userAnswer") userAnswer: Int,
        @Field("questionId") questionId: Int,
        @Field("userColorId") userColorId: Int
    ): Single<Response<ResponseBody>>
}