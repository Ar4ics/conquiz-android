package com.gizmodev.conquiz.ui.profile

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.gizmodev.conquiz.model.User
import com.gizmodev.conquiz.network.AuthenticationInterceptor
import com.gizmodev.conquiz.network.LoginApi
import com.gizmodev.conquiz.ui.core.AppViewModel
import com.gizmodev.conquiz.utils.SharedPrefStorage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber

class ProfileViewModel (
    private val loginApi: LoginApi,
    private val sharedPrefStorage: SharedPrefStorage,
    private val authenticationInterceptor: AuthenticationInterceptor
) : AppViewModel() {

    val state = State()

    init {
        sharedPrefStorage.readToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Timber.d("Token loaded")
                    if (it.isNotBlank()) {
                        authenticate(it)
                    } else {
                        state.setSignedOut()
                    }
                },
                {
                    Timber.e(it, "Failed to load token")
                    state.setSignedOut()
                }
            )
            .untilCleared()
    }

    private fun authenticate(token: String) {
        authenticationInterceptor.token = token;
        loginApi.refreshToken().subscribe(
            { result -> onSuccess(result) },
            { error -> onError(error) }
        ).untilCleared()
    }


    private fun onSuccess(response: Response<Any>) {
        val headers = response.headers()
        val token = headers.get("Authorization")
        Timber.d("success token = $token")
        Timber.d("response = $response")
        if (token != null && response.isSuccessful) {
            sharedPrefStorage.writeToken(token)
            authenticationInterceptor.token = token;
            loginApi.getUser().subscribe(
                { result -> onUserSuccess(result.data) },
                { error -> onError(error) }
            ).untilCleared()
        } else {
            state.setSignedOut()
        }
    }

    private fun onUserSuccess(user: User) {
        state.setSignedIn(user)
    }

    private fun onError(error: Throwable) {
        Timber.e("error = ${error.localizedMessage}")
        sharedPrefStorage.removeToken()
        state.setSignedOut()
    }

    fun logout() {
        loginApi.logout().subscribe {
            sharedPrefStorage.removeToken()
            state.setSignedOut()
        }.untilCleared()
    }

    class State {
        val signedOut = ObservableBoolean()
        val signedIn = ObservableBoolean()
        val user = ObservableField<User>()

        fun setSignedOut() {
            signedIn.set(false)
            signedOut.set(true)
        }

        fun setSignedIn(user: User) {
            signedOut.set(false)
            signedIn.set(true)
            this.user.set(user)
        }
    }

}