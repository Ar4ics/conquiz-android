package com.gizmodev.conquiz.ui.games

import androidx.lifecycle.MutableLiveData
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.model.GamesInfo
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.network.Result
import com.gizmodev.conquiz.ui.core.AppViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class GameListViewModel(
    private val gameApi: GameApi
) : AppViewModel() {

    val state = State()

    init {
//        launch {
//            try {
//                val data = gameApi.getGames().await()
//                withContext(Dispatchers.Default) {
//                    onSuccess(data)
//                }
//            } catch (e: Exception) {
//                onError(e)
//            }
//        }
        state.setGamesLoading()
        gameApi.getGames()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    Timber.d("Games loaded: $result")
                    state.setGames(result)
                },
                { error ->
                    Timber.e("Failed to load games: ${error.localizedMessage}")
                    state.setGamesError(error)
                }
            )
            .untilCleared()
    }

    class State {
        val games = MutableLiveData<Result<List<Game>>>()

        fun setGamesError(throwable: Throwable) {
            games.value = Result(
                Result.Status.COMPLETED,
                null,
                throwable
            )
        }

        fun setGamesLoading() {
            games.value =
                    Result(Result.Status.LOADING, null, null)
        }

        fun setGames(gamesInfo: GamesInfo) {
            games.value = Result(
                Result.Status.COMPLETED,
                gamesInfo.games,
                null
            )
        }
    }
}