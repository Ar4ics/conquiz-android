package com.gizmodev.conquiz

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gizmodev.conquiz.network.LoginApi
import com.gizmodev.conquiz.ui.game.GameListActivity
import com.gizmodev.conquiz.utils.AuthenticationInterceptor
import com.gizmodev.conquiz.utils.Constants.GOOGLE_SIGN_IN
import com.gizmodev.conquiz.utils.Constants.TOKEN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Response
import javax.inject.Inject



class LoginActivity : AppCompatActivity(), View.OnClickListener {

    @Inject
    lateinit var loginApi: LoginApi

    @Inject
    lateinit var authenticationInterceptor: AuthenticationInterceptor

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in_button -> signIn()
        }
    }

    var compositeDisposable = CompositeDisposable()

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidInjection.inject(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(resources.getString(R.string.google_client_id))
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GET_AUTH_CODE)
    }

    private fun onSuccess(response: Response<Any>) {
        val headers = response.headers()
        val token = headers.get("Authorization")
        Log.d(GOOGLE_SIGN_IN, "token = $token")
        if (token != null) {
            sharedPreferences.edit().putString(TOKEN, token).apply()
            goToGames(token)
        }
    }

    private fun onError(error: Throwable) {
        Log.d(GOOGLE_SIGN_IN, "error = ${error.localizedMessage}")
        sharedPreferences.edit().remove(TOKEN).apply()
    }

    private fun goToGames(token: String) {
        authenticationInterceptor.token = token;
        val intent = Intent(this, GameListActivity::class.java)
        startActivity(intent)
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            try {
                Log.d(GOOGLE_SIGN_IN, "id=" + account.id)
                Log.d(GOOGLE_SIGN_IN, "isExpired=" + account.isExpired)
                Log.d(GOOGLE_SIGN_IN, "serverAuthCode=" + account.serverAuthCode)
                val authCode = account.serverAuthCode
                if (authCode != null) {
                    authenticate(authCode)
                }
            } catch (e: Exception) {
                Log.e(GOOGLE_SIGN_IN, e.localizedMessage)
            }
        }
    }

    private fun authenticate(googleAuthCode: String) {

        val d = loginApi.getToken(googleAuthCode).subscribe(
            { result -> onSuccess(result) },
            { error -> onError(error) }
        )
        compositeDisposable.add(d)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_GET_AUTH_CODE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d(GOOGLE_SIGN_IN, "signInResult:failed code = " + e.localizedMessage)
            updateUI(null)
        }

    }

    companion object {
        const val RC_GET_AUTH_CODE = 1
    }
}
