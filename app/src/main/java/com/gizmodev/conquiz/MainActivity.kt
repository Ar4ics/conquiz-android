package com.gizmodev.conquiz

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gizmodev.conquiz.network.LoginApi
import com.gizmodev.conquiz.ui.game.GameListActivity
import com.gizmodev.conquiz.utils.AuthenticationInterceptor
import com.gizmodev.conquiz.utils.Constants.GOOGLE_SIGN_IN
import com.gizmodev.conquiz.utils.Constants.TOKEN
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Response
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var loginApi: LoginApi

    @Inject
    lateinit var authenticationInterceptor: AuthenticationInterceptor

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidInjection.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }


    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        if (sharedPreferences.contains(TOKEN)) {
            val token = sharedPreferences.getString(TOKEN, "");
            val d = loginApi.refreshToken("Bearer $token").subscribe(
                { result -> onSuccess(result) },
                { error -> onError(error) }
            )
            compositeDisposable.add(d)
        } else {
            goLogin()
        }
    }

    private fun onSuccess(response: Response<Any>) {
        val headers = response.headers()
        val token = headers.get("Authorization")
        Log.d(GOOGLE_SIGN_IN, "token = $token")
        if (token != null) {
            sharedPreferences.edit().putString(TOKEN, token).apply()
            goToGames(token)
        } else {
            goLogin()
        }
    }

    private fun onError(error: Throwable) {
        Log.d(GOOGLE_SIGN_IN, "error = ${error.localizedMessage}")
        sharedPreferences.edit().remove(TOKEN).apply()
        goLogin()
    }

    private fun goLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun goToGames(token: String) {
        authenticationInterceptor.token = token

        val intent = Intent(this, GameListActivity::class.java)
        startActivity(intent)
    }
}
