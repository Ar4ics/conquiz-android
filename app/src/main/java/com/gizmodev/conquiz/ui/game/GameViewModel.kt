package com.gizmodev.conquiz.ui.game

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.gizmodev.conquiz.model.Box
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.model.GameDetails
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.ui.core.AppViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber

class GameViewModel(private val gameApi: GameApi): AppViewModel() {

    lateinit var gameDetails: GameDetails

    val state = State()
    fun loadGame(game: Game){
        gameApi.getGame(game.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onRetrieveGameSuccess(result) },
                { error -> onRetrieveGameError(error) }
            ).untilCleared()
    }


    private fun onRetrieveGameSuccess(result: GameDetails) {
        gameDetails = result
        state.setGame(result)
    }

    private fun onRetrieveGameError(error: Throwable) {
        state.setError(error)
        Timber.e("failed to load game: ${error.localizedMessage}")
    }

    fun clickBox(box: Box) {
        val player = gameDetails.player ?: return
        gameApi.boxClick(gameDetails.game.id, box.x, box.y, player.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onBoxClickSuccess(result) },
                { error -> onBoxClickError(error) }
            ).untilCleared()

    }

    private fun onBoxClickSuccess(result: Response<Any>) {
        Timber.d("success box click: ${result.body()}")
    }

    private fun onBoxClickError(error: Throwable) {
        Timber.e("failed to box click: ${error.localizedMessage}")
        state.setError(error)
    }

    class State {
        val error = ObservableBoolean()
        val errorMessage = ObservableField<String>()
        val field = ObservableArrayList<Box>()
        val gameDetails = ObservableField<GameDetails>()

        fun setError(e: Throwable) {
            error.set(true)
            errorMessage.set(e.localizedMessage)
        }

        fun setGame(gameDetails: GameDetails) {
            this.gameDetails.set(gameDetails)
            val flatten = gameDetails.field.flatten()
            field.clear()
            field.addAll(flatten)
            error.set(false)
        }
    }
}