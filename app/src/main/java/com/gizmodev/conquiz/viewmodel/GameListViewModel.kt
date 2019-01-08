package com.gizmodev.conquiz.viewmodel

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.model.GamesInfo
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.utils.Constants.LOAD_GAMES
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GameListViewModel @Inject constructor(private val gameApi: GameApi): ViewModel(){

    private lateinit var subscription: Disposable
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    val errorMessage:MutableLiveData<Int> = MutableLiveData()
    val gameList:MutableLiveData<List<Game>> = MutableLiveData()
    val errorClickListener = View.OnClickListener { loadGames() }

    init{
        loadGames()
    }

    private fun loadGames(){
        subscription = gameApi.getGames()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrieveGameListStart() }
            .doOnTerminate { onRetrieveGameListFinish() }
            .subscribe(
                { result -> onRetrieveGameListSuccess(result) },
                { onRetrieveGameListError(it) }
            )
    }

    private fun onRetrieveGameListStart(){
        loadingVisibility.value = View.VISIBLE
        errorMessage.value = null
    }

    private fun onRetrieveGameListFinish(){
        loadingVisibility.value = View.GONE
    }

    private fun onRetrieveGameListSuccess(gamesInfo:GamesInfo){
        gameList.value = gamesInfo.games
    }

    private fun onRetrieveGameListError(error: Throwable){
        Log.d(LOAD_GAMES, error.localizedMessage)
        errorMessage.value = R.string.game_error
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }
}