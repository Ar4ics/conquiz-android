package com.gizmodev.conquiz.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.model.GameDetails
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GameViewModel @Inject constructor(private val gameApi: GameApi, private val gameId: Int) : ViewModel() {

    private lateinit var subscription: Disposable
    val errorMessage: MutableLiveData<Int> = MutableLiveData()
    val gameDetails: MutableLiveData<GameDetails> = MutableLiveData()

    init{
        loadGames()
    }

    private fun loadGames(){
        subscription = gameApi.getGame(gameId.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrieveGameStart() }
            .doOnTerminate { onRetrieveGameFinish() }
            .subscribe(
                { result -> onRetrieveGameSuccess(result) },
                { onRetrieveGameError(it) }
            )
    }

    private fun onRetrieveGameStart(){
        errorMessage.value = null
    }

    private fun onRetrieveGameFinish(){
    }

    private fun onRetrieveGameSuccess(gameDetails: GameDetails){
        this.gameDetails.value = gameDetails
    }

    private fun onRetrieveGameError(error: Throwable){
        Log.d(Constants.LOAD_GAMES, error.localizedMessage)
        errorMessage.value = R.string.game_error
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }
}