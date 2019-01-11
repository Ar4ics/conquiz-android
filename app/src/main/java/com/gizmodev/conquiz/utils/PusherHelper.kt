package com.gizmodev.conquiz.utils

import android.content.Context
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.network.AuthenticationInterceptor
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import io.reactivex.Observable
import timber.log.Timber

class PusherHelper(private val context: Context, private val authenticationInterceptor: AuthenticationInterceptor) {

    val gameEvents = arrayListOf(
        "BaseCreated",
        "WhoAttack",
        "CompetitiveAnswerResults",
        "CorrectAnswers",
        "AnswersResults",
        "NewExactQuestion",
        "NewQuestion",
        "UserColorsChanged",
        "WhoMoves",
        "BoxClicked"
    ).map { "App\\Events\\$it" }



    fun getPusher() : Observable<Pusher> {
        return Observable.fromCallable {
            val auth = HttpAuthorizer("${Constants.REST_API_URL}${context.resources.getString(R.string.pusher_auth_endpoint)}")
            auth.setHeaders(mapOf("Authorization" to "Bearer ${authenticationInterceptor.token}"))
            val options = PusherOptions().setCluster(context.resources.getString(R.string.pusher_cluster)).setAuthorizer(auth)
            val pusher = Pusher(context.resources.getString(R.string.pusher_key), options)
            pusher.connect(object : ConnectionEventListener {
                override fun onConnectionStateChange(change: ConnectionStateChange) {
                    Timber.d("State changed to ${change.currentState} from ${change.previousState}")
                    if (change.currentState == ConnectionState.CONNECTED) {
                    }
                }

                override fun onError(message: String, code: String, e: Exception) {
                    Timber.e("There was a problem connecting: $message $code ${e.localizedMessage}")
                }
            })
            pusher
        }
    }
}