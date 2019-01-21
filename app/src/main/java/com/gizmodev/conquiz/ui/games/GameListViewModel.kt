package com.gizmodev.conquiz.ui.games

import androidx.lifecycle.MutableLiveData
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.network.GameHolder
import com.gizmodev.conquiz.network.Result
import com.gizmodev.conquiz.ui.core.AppViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class GameListViewModel(
    private val gameApi: GameApi,
    private val gameHolder: GameHolder
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
                    state.setGames(result.games)
                },
                { error ->
                    Timber.e("Failed to load games: ${error.localizedMessage}")
                    state.setGamesError(error)
                }
            )
            .untilCleared()
    }

    fun setFilteredGames(my: Boolean?, checkedButton: Int?) {
        var filtered = state.games.value?.data
        val user = gameHolder.user
        Timber.d("user = $user")
        Timber.d("my = $my, checkedButton = $checkedButton")
        if (my != null && my == true) {
            filtered = filtered?.filter { it.user_colors.any { userColor -> userColor.user_id == user?.id } }
        }
        if (checkedButton != null) {
            if (checkedButton == R.id.second) {
                filtered = filtered?.filter { !it.isFinished() }
            }
            if (checkedButton == R.id.third) {
                filtered = filtered?.filter { it.isFinished() }
            }
        }
        state.setFilteredGames(filtered)
    }

    class State {
        val games = MutableLiveData<Result<List<Game>>>()

        val filteredGames = MutableLiveData<List<Game>?>()

        val checkedButton = MutableLiveData<Int>()
        val my = MutableLiveData<Boolean>()

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

        fun setGames(gamesInfo: List<Game>?) {
            games.value = Result(
                Result.Status.COMPLETED,
                gamesInfo,
                null
            )
            checkedButton.value = R.id.second
            my.value = true
        }

        fun setFilteredGames(gamesInfo: List<Game>?) {
            filteredGames.value = gamesInfo
        }
    }
}