package com.gizmodev.conquiz.ui.login

import androidx.lifecycle.MutableLiveData
import com.gizmodev.conquiz.network.AuthenticationInterceptor
import com.gizmodev.conquiz.network.LoginApi
import com.gizmodev.conquiz.network.Result
import com.gizmodev.conquiz.ui.core.AppViewModel
import com.gizmodev.conquiz.utils.SharedPrefStorage
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber

class LoginViewModel (
    private val loginApi: LoginApi,
    private val sharedPrefStorage: SharedPrefStorage,
    private val authenticationInterceptor: AuthenticationInterceptor
) : AppViewModel() {

    val state = State()

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.serverAuthCode?.let {
                authenticate(it)
            }
        } catch (error: Exception) {
            Timber.e("failed getting code: ${error.localizedMessage}")
            sharedPrefStorage.removeToken()
            state.setSignedOut(error)
        }

    }


    private fun authenticate(googleAuthCode: String) {

//        launch {
//            try {
//                val data = loginApi.getToken(googleAuthCode).await()
//                onSuccess(data)
//            } catch (e: Exception) {
//                onError(e)
//            }
//        }
        loginApi.getToken(googleAuthCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            { result -> onSuccess(result) },
            { error -> onError(error) }
        ).untilCleared()
    }


    private fun onSuccess(response: Response<Any>) {
        val headers = response.headers()
        val token = headers.get("Authorization")
        Timber.d("success token = $token")
        if (token != null && response.isSuccessful) {
            authenticationInterceptor.token = token;
            writeToken(token)
        } else {
            state.setSignedOut(Throwable("token or response is null"))
        }
    }

    private fun writeToken(token: String) {
        Observable.defer {
            val success = sharedPrefStorage.writeToken(token)
            Observable.just(success)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    if (result) {
                        state.setSignedIn(token)
                    } else {
                        state.setSignedOut(Throwable("token cannot be written"))
                    }
                },
                { error ->
                    Timber.e("Failed to write token: ${error.localizedMessage}")
                    state.setSignedOut(error)
                }
            )
            .untilCleared()
    }

    private fun onError(error: Throwable) {
        Timber.e("error getting token = ${error.localizedMessage}")
        state.setSignedOut(error)
    }

    class State {
        val sign = MutableLiveData<Result<String>>()

        fun setSignedOut(throwable: Throwable) {
            sign.value = Result(
                Result.Status.COMPLETED,
                null,
                throwable
            )
        }

        fun setSigningIn() {
            sign.value =
                    Result(Result.Status.LOADING, null, null)
        }

        fun setSignedIn(token: String) {
            sign.value = Result(
                Result.Status.COMPLETED,
                token,
                null
            )
        }
    }

}