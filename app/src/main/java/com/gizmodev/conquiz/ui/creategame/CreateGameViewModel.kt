package com.gizmodev.conquiz.ui.creategame

import androidx.lifecycle.MutableLiveData
import com.gizmodev.conquiz.model.User
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.network.GameHolder
import com.gizmodev.conquiz.network.PusherHolder
import com.gizmodev.conquiz.network.Result
import com.gizmodev.conquiz.ui.core.AppViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class CreateGameViewModel(
    private val gameApi: GameApi,
    private val pusherHolder: PusherHolder,
    val gameHolder: GameHolder
) : AppViewModel() {

    val state = State()
    private val room = 0
    private var subscription: Boolean = false

    init {

        gameHolder.onlineUsers.observable.subscribe {
            Timber.d("online users: $it")
            val users = it[room]
            if (users != null) {
                state.setOnlineUsers(users)
            }
        }.untilCleared()

        state.setUsersLoading()
        gameApi.getGames()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    Timber.d("Users loaded: ${result.users}")
                    state.setUsers(result.users)
                    pusherHolder.connectPresence(room)
                    subscription = true
                },
                { error ->
                    Timber.e("Failed to load users: ${error.localizedMessage}")
                    state.setUsersError(error)
                    subscription = false
                }
            )
            .untilCleared()
    }

    override fun onCleared() {
        super.onCleared()
        if (subscription) {
            pusherHolder.disconnectPresence(room)
        }
    }

    fun createGame(title: String, countX: Int, countY: Int, users: List<Int>) {
        gameApi.createGame(title, countX, countY, users)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    Timber.d("Game created: $result")
                    state.gameCreated.value = true
                },
                { error ->
                    Timber.e("Failed to create game: ${error.localizedMessage}")
                    state.gameCreated.value = false
                }
            )
            .untilCleared()
    }

    class State {
        val countX = MutableLiveData<String>()
        val countY = MutableLiveData<String>()
        val gameTitle = MutableLiveData<String>()
        val users = MutableLiveData<List<PairUser>>()
        val initialUsers = MutableLiveData<Result<List<User>>>()

        val gameCreated = MutableLiveData<Boolean>()

        fun setUsersError(throwable: Throwable) {
            initialUsers.value = Result(
                Result.Status.COMPLETED,
                null,
                throwable
            )
        }

        fun setUsersLoading() {
            initialUsers.value =
                Result(Result.Status.LOADING, null, null)
        }


        fun setUsers(iu: List<User>) {
            initialUsers.value = Result(
                Result.Status.COMPLETED,
                iu,
                null
            )
            users.postValue(iu.map { State.PairUser(it) })
        }

        fun setOnlineUsers(onlineUsers: List<User>) {

            val old = users.value?.toMutableList()

            if (old != null){
                for ((index, u) in old.withIndex()) {
                    if (onlineUsers.find { it.id == u.user.id} != null) {
                        old[index] = old[index].copy(online = true)
                    } else {
                        old[index] = old[index].copy(online = false)
                    }
                }
                val sorted = old.sortedBy { !it.online }
                Timber.d("sorted: $sorted")

                users.postValue(sorted)
            }
        }

        data class PairUser(val user: User, val online: Boolean = false, var checked: Boolean = false)
    }
}