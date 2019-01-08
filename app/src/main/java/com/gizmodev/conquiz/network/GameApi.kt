package com.gizmodev.conquiz.network

import com.gizmodev.conquiz.model.GameDetails
import com.gizmodev.conquiz.model.GamesInfo
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface GameApi {
    @GET("games")
    fun getGames(): Observable<GamesInfo>

    @GET("games/{id}")
    fun getGame(@Path(value = "id", encoded = true) id: String): Observable<GameDetails>
}