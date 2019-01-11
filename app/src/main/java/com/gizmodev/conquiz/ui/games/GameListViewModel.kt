package com.gizmodev.conquiz.ui.games

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.ui.core.AppViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class GameListViewModel(
    private val gameApi: GameApi
) : AppViewModel() {

    val state = State()

    fun list() {
        if (state.loading.get() || state.loaded.get()) return
        state.loading.set(true)

        gameApi.getGames()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Timber.d("Games loaded")
                    state.games.clear()
                    state.games.addAll(it.games)
                    state.loading.set(false)
                    state.loaded.set(true)
                },
                {
                    Timber.e(it, "Failed to load games")
                    state.loading.set(false)
                }
            )
            .untilCleared()
    }

    class State {
        val games = ObservableArrayList<Game>()
        val loading = ObservableBoolean()
        val loaded = ObservableBoolean()
    }
}