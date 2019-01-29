package com.gizmodev.conquiz.ui.games

import androidx.lifecycle.MediatorLiveData
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
    val user = gameHolder.user

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
                    setFilteredGames(state.my.value ?: false, state.checkedButton.value ?: R.id.all)
                },
                { error ->
                    Timber.e("Failed to load games: ${error.localizedMessage}")
                    state.setGamesError(error)
                }
            )
            .untilCleared()

    }

    fun setFilteredGames(my: Boolean, checkedButton: Int) {
        var filtered = state.games.value?.data
        Timber.d("user = $user")
        Timber.d("my = $my, checkedButton = $checkedButton")
        if (my) {
            filtered = filtered?.filter { it.user_colors.any { userColor -> userColor.user_id == user?.id } }
        }
        if (checkedButton == com.gizmodev.conquiz.R.id.current) {
            filtered = filtered?.filter { !it.isFinished() }
        }
        if (checkedButton == com.gizmodev.conquiz.R.id.finished) {
            filtered = filtered?.filter { it.isFinished() }
        }
        state.setFilteredGames(filtered)
    }

    class State {
        val games = MutableLiveData<Result<List<Game>>>()
        val filteredGames = MutableLiveData<List<Game>>()

        val checkedButton = MutableLiveData<Int>(R.id.all)
        val my = MutableLiveData<Boolean>(false)

        val liveDataMerger = MediatorLiveData<Any>()

        init {
            liveDataMerger.addSource(checkedButton) { value -> liveDataMerger.setValue(value) }
            liveDataMerger.addSource(my) { value -> liveDataMerger.setValue(value) }
        }

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
        }

        fun setFilteredGames(gamesInfo: List<Game>?) {
            filteredGames.value = gamesInfo
        }
    }
}