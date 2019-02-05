package com.gizmodev.conquiz.network

import android.content.Context
import com.gizmodev.conquiz.utils.Constants
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PresenceChannelEventListener
import com.pusher.client.channel.User
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import timber.log.Timber
import javax.inject.Singleton


@Singleton
class PusherHolder(
    private val context: Context,
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val gameHolder: GameHolder
) {

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
        const val GameMessageCreated = "GameMessageCreated"

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
            WinnerFound,
            GameMessageCreated
        ).map { "$EventPrefix$it" }.toTypedArray()
    }

    var pusher: Pusher? = null

    fun errorParsingJson() {
        Timber.e("Cannot parse pusher json")
    }

    fun connectPresence(gameId: Int) {
        gameHolder.user ?: return
        val channel = pusher?.getPresenceChannel("presence-users.$gameId")
        if (channel != null && channel.isSubscribed) return
        pusher?.subscribePresence("presence-users.$gameId", object : PresenceChannelEventListener {
            override fun onEvent(channelName: String?, eventName: String?, data: String?) {

            }

            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                Timber.d("failed entering channel: $message $e")
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                Timber.d("success entering channel: $channelName")
            }

            override fun onUsersInformationReceived(channelName: String?, users: MutableSet<User>?) {
                Timber.d("users in channel $channelName: $users")

                if (users == null) return
                val us = mutableListOf<com.gizmodev.conquiz.model.User>()

                for (user in users) {
                    val info = Result.fromJson<com.gizmodev.conquiz.model.User>(user.info)
                    if (info != null) {
                        us.add(info)
                    }
                }

                val old = gameHolder.onlineUsers.value
                gameHolder.onlineUsers.value = old.plus(gameId to us)
            }

            override fun userSubscribed(channelName: String?, user: User?) {
                Timber.d("user entered in $channelName: $user")

                if (user == null) return
                val u = Result.fromJson<com.gizmodev.conquiz.model.User>(user.info)
                val oldUsers = gameHolder.onlineUsers.value[gameId]?.toMutableList()
                if (oldUsers != null && u != null) {
                    oldUsers.add(u)
                    gameHolder.onlineUsers.value = gameHolder.onlineUsers.value.plus(gameId to oldUsers)
                }
            }

            override fun userUnsubscribed(channelName: String?, user: User?) {
                Timber.d("user leaved from $channelName: $user")

                if (user == null) return
                val u = Result.fromJson<com.gizmodev.conquiz.model.User>(user.info)
                val oldUsers = gameHolder.onlineUsers.value[gameId]?.toMutableList()
                if (oldUsers != null && u != null) {
                    oldUsers.remove(u)
                    gameHolder.onlineUsers.value = gameHolder.onlineUsers.value.plus(gameId to oldUsers)
                }
            }
        })
    }

    fun disconnectPresence(gameId: Int) {
        gameHolder.user ?: return
        pusher?.unsubscribe("presence-users.$gameId")
    }

    fun connect() {
        val auth =
            HttpAuthorizer("${Constants.REST_API_URL_HEROKU}${context.resources.getString(com.gizmodev.conquiz.R.string.pusher_auth_endpoint)}")
        auth.setHeaders(mapOf("Authorization" to "Bearer ${authenticationInterceptor.token}"))
        val options =
            PusherOptions().setCluster(context.resources.getString(com.gizmodev.conquiz.R.string.pusher_cluster))
                .setAuthorizer(auth)
        pusher = Pusher(context.resources.getString(com.gizmodev.conquiz.R.string.pusher_key), options)

        pusher?.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange?) {
                Timber.d("State changed to ${change?.currentState} from ${change?.previousState}")
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