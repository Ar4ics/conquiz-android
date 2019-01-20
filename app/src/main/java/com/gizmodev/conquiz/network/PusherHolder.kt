package com.gizmodev.conquiz.network

import android.content.Context
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.utils.Constants
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import timber.log.Timber

class PusherHolder(private val context: Context, private val authenticationInterceptor: AuthenticationInterceptor) {

    companion object {
        const val EventPrefix = "App\\Events\\"

        const val BaseCreated = "BaseCreated"
        const val BoxClicked = "BoxClicked"
        const val CompetitiveAnswerResults = "CompetitiveAnswerResults"
        const val CorrectAnswers = "CorrectAnswers"
        const val AnswersResults = "AnswersResults"
        const val WhoMoves = "WhoMoves"
        const val WhoAttack = "WhoAttack"
        const val NewQuestion = "NewQuestion"
        const val NewExactQuestion = "NewExactQuestion"
        const val UserColorsChanged = "UserColorsChanged"
        const val WinnerFound = "WinnerFound"

        val AllEvents = listOf(
            BaseCreated,
            BoxClicked,
            CompetitiveAnswerResults,
            CorrectAnswers,
            AnswersResults,
            WhoMoves,
            WhoAttack,
            NewQuestion,
            NewExactQuestion,
            UserColorsChanged,
            WinnerFound
        ).map { "$EventPrefix$it" }.toTypedArray()
    }

    var pusher: Pusher? = null

    fun errorParsingJson() {
        Timber.e("Cannot parse pusher json")
    }

    fun connect() {
        val auth =
            HttpAuthorizer("${Constants.REST_API_URL}${context.resources.getString(R.string.pusher_auth_endpoint)}")
        auth.setHeaders(mapOf("Authorization" to "Bearer ${authenticationInterceptor.token}"))
        val options =
            PusherOptions().setCluster(context.resources.getString(R.string.pusher_cluster)).setAuthorizer(auth)
        pusher = Pusher(context.resources.getString(R.string.pusher_key), options)

        pusher?.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Timber.d("State changed to ${change.currentState} from ${change.previousState}")
            }

            override fun onError(message: String?, code: String?, e: Exception?) {
                Timber.e("There was a problem connecting: $message $code ${e?.localizedMessage}")
            }
        })

    }

    fun disconnect() {
        pusher?.disconnect()
        pusher = null
    }
}