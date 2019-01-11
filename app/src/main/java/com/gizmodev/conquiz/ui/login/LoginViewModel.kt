package com.gizmodev.conquiz.ui.login

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.gizmodev.conquiz.network.AuthenticationInterceptor
import com.gizmodev.conquiz.network.LoginApi
import com.gizmodev.conquiz.ui.core.AppViewModel
import com.gizmodev.conquiz.utils.SharedPrefStorage
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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

            // Signed in successfully, show authenticated UI.
            account?.serverAuthCode?.let {
                authenticate(it)
            }
        } catch (error: Exception) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Timber.e("failed getting code: ${error.localizedMessage}")
            sharedPrefStorage.removeToken()
            state.setError(error)
        }

    }


    private fun authenticate(googleAuthCode: String) {

        loginApi.getToken(googleAuthCode).subscribe(
            { result -> onSuccess(result) },
            { error -> onError(error) }
        ).untilCleared()
    }


    private fun onSuccess(response: Response<Any>) {
        val headers = response.headers()
        val token = headers.get("Authorization")
        Timber.d("success token = $token")
        if (token != null && response.isSuccessful) {
            sharedPrefStorage.writeToken(token)
            authenticationInterceptor.token = token;
            state.setSuccess()
        } else {
            state.setError(Throwable("error logging"))
        }
    }

    private fun onError(error: Throwable) {
        Timber.e("error getting token = ${error.localizedMessage}")
        sharedPrefStorage.removeToken()
        state.setError(error)
    }

    class State {
        val error = ObservableBoolean()
        val errorMessage = ObservableField<String>()
        val signing = ObservableBoolean()
        val signed = ObservableBoolean()

        fun setSuccess() {
            signing.set(false)
            signed.set(true)
        }

        fun setError(e: Throwable) {
            signing.set(false)
            signed.set(false)
            error.set(true)
            errorMessage.set(e.localizedMessage)
        }
    }

}