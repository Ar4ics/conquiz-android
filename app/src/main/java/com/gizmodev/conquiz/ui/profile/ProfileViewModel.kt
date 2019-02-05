package com.gizmodev.conquiz.ui.profile

import androidx.lifecycle.MutableLiveData
import com.gizmodev.conquiz.model.User
import com.gizmodev.conquiz.model.UserLogin
import com.gizmodev.conquiz.network.*
import com.gizmodev.conquiz.ui.core.AppViewModel
import com.gizmodev.conquiz.utils.SharedPrefStorage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ProfileViewModel(
    private val loginApi: LoginApi,
    private val sharedPrefStorage: SharedPrefStorage,
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val gameHolder: GameHolder,
    private val pusherHolder: PusherHolder
) : AppViewModel() {

    val state = State()

    init {
//        launch {
//            try {
//                val data= sharedPrefStorage.readToken()
//                Timber.d("Token loaded: $data")
//                if (data != null && data.isNotBlank()) {
//                    authenticate(data)
//                } else {
//                    state.setSignedOut()
//                }
//            } catch (e: Exception) {
//                Timber.e("Failed to load token: ${e.localizedMessage}")
//                state.setSignedOut()
//            }
//        }
        state.setSigningIn()
        Observable.defer {
            val token = sharedPrefStorage.readToken()
            Observable.just(token)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {
                loadPusher()
            }
            .subscribe(
                { result ->
                    Timber.d("Token loaded: $result")
                    if (result != null && result.isNotBlank()) {
                        authenticationInterceptor.token = result
                        authenticate()
                    } else {
                        state.setSignedOut(Throwable("token not found in shared prefs"))
                    }
                },
                { error ->
                    Timber.e("Failed to load token: ${error.localizedMessage}")
                    state.setSignedOut(error)
                }
            )
            .untilCleared()
    }

    private fun authenticate() {

//        launch {
//            try {
//                authenticationInterceptor.token = token;
//                val data = loginApi.refreshToken().await()
//                onSuccess(data)
//            } catch (e: Exception) {
//                onError(e)
//            }
//        }
        loginApi.getUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onUserSuccess(result) },
                { error -> onUserError(error) }
            ).untilCleared()
    }

    private fun onUserSuccess(userLogin: UserLogin) {
        Timber.d("success logged in = $userLogin")
        gameHolder.user = userLogin.data
        state.setSignedIn(userLogin)
    }

    private fun loadPusher() {
        Observable.fromCallable {
            pusherHolder.connect()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Timber.d("pusher loaded")

                },
                { error ->
                    Timber.e("pusher error load: $error")
                    state.setSignedOut(error)

                }
            ).untilCleared()
    }

    private fun onUserError(throwable: Throwable) {
        Timber.e("error logging = ${throwable.localizedMessage}")
        removeToken()
    }

    override fun onCleared() {
        super.onCleared()
        pusherHolder.disconnect()
    }

    private fun removeToken() {
        Timber.d("perform remove token")
        Observable.defer {
            val success = sharedPrefStorage.removeToken()
            Observable.just(success)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {
                gameHolder.user = null
                gameHolder.game = null
                gameHolder.question = null
                gameHolder.player = null
                gameHolder.onlineUsers.value = mapOf()
                authenticationInterceptor.token = null
            }
            .subscribe(
                { result ->
                    if (result) {
                        state.setSignedOut(Throwable("token successfully removed"))
                    } else {
                        state.setSignedOut(Throwable("token cannot be removed"))
                    }
                },
                { error ->
                    Timber.e("Failed to remove token: ${error.localizedMessage}")
                    state.setSignedOut(error)
                }
            )
            .untilCleared()
    }


    fun logout() {
        Timber.d("perform logout")
        loginApi.logout()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {
                removeToken()
            }
            .subscribe(
                {
                    Timber.d("Success logout")
                },
                { error ->
                    Timber.e("Failed to logout: ${error.localizedMessage}")
                }
            ).untilCleared()
    }

    class State {
        val user = MutableLiveData<Result<User>>()

        fun setSignedOut(throwable: Throwable) {
            user.value = Result(
                Result.Status.COMPLETED,
                null,
                throwable
            )
            Timber.e("called setSignedOut: $throwable")
        }

        fun setSigningIn() {
            user.value =
                Result(Result.Status.LOADING, null, null)
        }

        fun setSignedIn(userLogin: UserLogin) {
            user.value = Result(
                Result.Status.COMPLETED,
                userLogin.data,
                null
            )
        }
    }

}