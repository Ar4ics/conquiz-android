package com.gizmodev.conquiz.network

import com.gizmodev.conquiz.model.GameDetails
import com.gizmodev.conquiz.model.GameMessageGroup
import com.gizmodev.conquiz.model.GamesInfo
import com.gizmodev.conquiz.model.User
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GameApi {
    @GET("games")
    fun getGames(): Single<GamesInfo>

    @GET("users")
    fun getUsers(): Single<List<User>>

    @GET("games/{id}")
    fun getGame(@Path(value = "id", encoded = true) id: Int): Single<GameDetails>

    @FormUrlEncoded
    @POST("games")
    fun createGame(
        @Field("title") title: String,
        @Field("count_x") countX: Int,
        @Field("count_y") countY: Int,
        @Field("users[]") users: List<Int>
    ): Single<Response<ResponseBody>>

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

    @GET("games/{id}/message")
    fun getGameMessages(@Path(value = "id", encoded = true) id: Int): Single<List<GameMessageGroup>>

    @FormUrlEncoded
    @POST("games/{id}/message")
    fun sendMessage(
        @Path(value = "id", encoded = true) id: Int,
        @Field("message") message: String
    ): Single<Response<ResponseBody>>
}