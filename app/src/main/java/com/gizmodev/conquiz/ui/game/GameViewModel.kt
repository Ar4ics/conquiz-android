package com.gizmodev.conquiz.ui.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.gizmodev.conquiz.model.*
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.network.GameHolder
import com.gizmodev.conquiz.network.PusherHolder
import com.gizmodev.conquiz.network.Result
import com.gizmodev.conquiz.ui.core.AppViewModel
import com.gizmodev.conquiz.utils.DateTimePicker
import com.gizmodev.conquiz.utils.DoubleTrigger
import com.gizmodev.conquiz.utils.toString
import com.pusher.client.channel.ChannelEventListener
import com.pusher.client.channel.PresenceChannelEventListener
import com.pusher.client.channel.User
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit


class GameViewModel(
    private val gameApi: GameApi,
    private val pusherHolder: PusherHolder,
    val gameHolder: GameHolder
) : AppViewModel() {

    val state = State()
    fun loadGame(game: Game) {
        gameHolder.game = game
        state.setGameLoading()
        gameApi.getGame(game.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onRetrieveGameSuccess(result) },
                { error -> onRetrieveGameError(error) }
            ).untilCleared()
        state.setGameMessagesLoading()
        gameApi.getGameMessages(game.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onRetrieveGameMessagesSuccess(result) },
                { error -> onRetrieveGameMessagesError(error) }
            ).untilCleared()
    }

    private fun onRetrieveGameMessagesError(throwable: Throwable) {
        Timber.e("failed to load game: ${throwable.localizedMessage}")
        state.setGameMessagesError(throwable)
    }

    private fun onRetrieveGameMessagesSuccess(result: List<GameMessageGroup>) {
        Timber.d("success load messages: $result")
        state.setGameMessages(result)
    }


    private fun onRetrieveGameSuccess(gameDetails: GameDetails) {
        Timber.d("success load game: $gameDetails")
        gameHolder.player = gameDetails.player
        gameHolder.question = gameDetails.question
        state.setGame(gameDetails)
    }

    private fun onRetrieveGameError(error: Throwable) {
        state.setGameError(error)
        Timber.e("failed to load game: ${error.localizedMessage}")
    }

    private var clickedBox: Box? = null

    fun clickBox(box: Box) {
        val player = gameHolder.player
        val game = gameHolder.game

        clickedBox = box


        if (player != null && game != null) {
            state.replaceBox(box.copy(color = "gray"))

            gameApi.boxClick(game.id, box.x, box.y, player.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> onBoxClickSuccess(result) },
                    { error -> onBoxClickError(error) }
                ).untilCleared()
        }
    }

    private fun onBoxClickSuccess(result: Response<ResponseBody>) {
        val response = result.body()?.string()
        Timber.d("success box click: $response")
        if (response != null && response.contains("error", ignoreCase = true)) {
            val box = clickedBox
            box?.let {
                state.replaceBox(box)
            }
        }
    }

    private fun onBoxClickError(error: Throwable) {
        Timber.e("failed to box click: ${error.localizedMessage}")
        state.setGameError(error)
        val box = clickedBox
        box?.let {
            state.replaceBox(box)
        }
    }


    fun sendMessage(message: String) {
        val user = gameHolder.user
        val game = gameHolder.game

        if (user != null && game != null) {
            gameApi.sendMessage(game.id, message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> onGameMessageSuccess(result) },
                    { error -> onGameMessageError(error) }
                ).untilCleared()
        }
    }

    private fun onGameMessageSuccess(result: Response<ResponseBody>) {
        val response = result.body()?.string()
        Timber.d("success message: $response")
    }

    private fun onGameMessageError(error: Throwable) {
        Timber.e("failed to send message: ${error.localizedMessage}")
    }

    fun loadPusher() {

        pusherHolder.pusher?.subscribe(
            "game.${gameHolder.game?.id}",
            object : ChannelEventListener {
                override fun onSubscriptionSucceeded(channelName: String?) {
                    Timber.d("Subscribed to channel $channelName")
                }

                override fun onEvent(channelName: String?, eventName: String?, data: String?) {
                    Timber.d("Event $eventName on channel $channelName with data $data")

                    if (data == null) return

                    when (eventName?.removePrefix(PusherHolder.EventPrefix)) {
                        PusherHolder.BaseCreated, PusherHolder.BoxClicked -> {
                            val box = Result.fromJson<Box>(data)
                            Timber.d("box: $box")
                            if (box != null) {
                                state.replaceBox(box)
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.AnswersResults -> {
                            val answersResults = Result.fromJson<AnswerResults>(data)
                            Timber.d("AnswerResults: $answersResults")
                            if (answersResults != null) {
                                state.answerResults.postValue(answersResults)
                                state.removeBoxes(answersResults.deleted_boxes)
                                Timber.d("start delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                Completable
                                    .timer(3, TimeUnit.SECONDS)
                                    .subscribeBy(
                                        onComplete = {
                                            state.setQuestion(null)
                                            Timber.d("complete delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                        },
                                        onError = {
                                            Timber.e("error delay: ${it.localizedMessage}")
                                        }
                                    )
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.CorrectAnswers -> {
                            val correctAnswers = Result.fromJson<CorrectAnswerResults>(data)
                            Timber.d("CorrectAnswerResults: $correctAnswers")
                            if (correctAnswers != null) {
                                state.answerResults.postValue(AnswerResults(correctAnswers.results, correctAnswers.correct, listOf()))
                                Timber.d("start delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                state.showExactQuestion.postValue(false)
                                Completable
                                    .timer(3, TimeUnit.SECONDS)
                                    .subscribeBy(
                                        onComplete = {
                                            state.showExactQuestion.postValue(true)
                                            Timber.d("complete delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                        },
                                        onError = {
                                            Timber.e("error delay: ${it.localizedMessage}")
                                        }
                                    )
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.CompetitiveAnswerResults -> {
                            val competitiveAnswerResults = Result.fromJson<CompetitiveAnswerResults>(data)
                            Timber.d("CompetitiveAnswerResults: $competitiveAnswerResults")
                            if (competitiveAnswerResults != null) {
                                state.competitiveAnswerResults.postValue(competitiveAnswerResults)
                                state.replaceBox(competitiveAnswerResults.result_box)
                                Timber.d("start delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                Completable
                                    .timer(3, TimeUnit.SECONDS)
                                    .subscribeBy(
                                        onComplete = {
                                            state.setQuestion(null)
                                            Timber.d("complete delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                        },
                                        onError = {
                                            Timber.e("error delay: ${it.localizedMessage}")
                                        }
                                    )
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.WhoMoves -> {
                            val whoMoves = Result.fromJson<WhoMoves>(data)
                            Timber.d("whoMoves: $whoMoves")
                            if (whoMoves != null) {
                                state.setWhoMoves(whoMoves.user_color)
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.WhoAttack -> {
                            val whoAttack = Result.fromJson<WhoAttack>(data)
                            Timber.d("whoAttack: $whoAttack")
                            if (whoAttack != null) {
                                state.setCompetitiveBox(whoAttack.competitive_box)
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.NewQuestion, PusherHolder.NewExactQuestion -> {
                            val question = Result.fromJson<Question>(data)
                            Timber.d("question: $question")
                            if (question != null) {
                                state.setQuestion(question)
                                gameHolder.question = question
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.UserColorsChanged -> {
                            val players = Result.fromJson<Players>(data)
                            Timber.d("players: $players")
                            if (players != null) {
                                state.setPlayers(players.user_colors)
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.WinnerFound -> {
                            val winner = Result.fromJson<Winner>(data)
                            Timber.d("winner: $winner")
                            if (winner != null) {
                                state.setWinner(winner.winner)
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.GameMessageCreated -> {
                            val gameMessage = Result.fromJson<GameMessage>(data)
                            Timber.d("gameMessage: $gameMessage")
                            if (gameMessage != null) {
                                state.addGameMessage(gameMessage)
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                    }
                }
            },
            *PusherHolder.AllEvents
        )


        pusherHolder.pusher?.subscribePresence("presence-users.${gameHolder.game?.id}", object : PresenceChannelEventListener {
            override fun onEvent(channelName: String?, eventName: String?, data: String?) {

            }

            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                Timber.d("failed entering channel: $message $e")
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                Timber.d("success entering channel: $channelName")
            }

            override fun onUsersInformationReceived(channelName: String?, users: MutableSet<User>?) {
                Timber.d("users in channel: $users")

                if (users == null) return
                val us = mutableListOf<com.gizmodev.conquiz.model.User>()

                for (user in users) {
                    val info = Result.fromJson<com.gizmodev.conquiz.model.User>(user.info)
                    if (info != null) {
                        us.add(info)
                    }
                }

                state.onlineUsers.postValue(us)
            }

            override fun userSubscribed(channelName: String?, user: User?) {
                Timber.d("user entered: $user")

                if (user == null) return
                val u = Result.fromJson<com.gizmodev.conquiz.model.User>(user.info)
                val oldUsers = state.onlineUsers.value?.toMutableList()
                if (oldUsers != null && u != null) {
                    oldUsers.add(u)
                    state.onlineUsers.postValue(oldUsers)
                }
            }

            override fun userUnsubscribed(channelName: String?, user: User?) {
                Timber.d("user leaved: $user")

                if (user == null) return
                val u = Result.fromJson<com.gizmodev.conquiz.model.User>(user.info)
                val oldUsers = state.onlineUsers.value?.toMutableList()
                if (oldUsers != null && u != null) {
                    oldUsers.remove(u)
                    state.onlineUsers.postValue(oldUsers)
                }
            }

        })
    }

    override fun onCleared() {
        pusherHolder.pusher?.unsubscribe("game.${gameHolder.game?.id}")
        pusherHolder.pusher?.unsubscribe("presence-users.${gameHolder.game?.id}")
        super.onCleared()
    }

    class State {
        val gameDetails = MutableLiveData<Result<GameDetails>>()
        val field = MutableLiveData<List<Box>>()
        val players = MutableLiveData<List<UserColor>>()
        val question = MutableLiveData<Question?>()
        val whoMoves = MutableLiveData<UserColor?>()
        val winner = MutableLiveData<UserColor?>()
        val answerResults = MutableLiveData<AnswerResults>()
        val competitiveAnswerResults = MutableLiveData<CompetitiveAnswerResults>()

        val showExactQuestion = MutableLiveData<Boolean>()
        val exactQuestion = Transformations.map(DoubleTrigger(question, showExactQuestion)) {
            val q = it.first
            val showExactQuestion = it.second
            q != null && q.is_exact_answer && (showExactQuestion != null && showExactQuestion || showExactQuestion == null)
        }

        val whoMovesName = Transformations.map(whoMoves) {
            if (it != null) {
                "Ходит ${it.user.name}"
            } else {
                ""
            }
        }

        val winnerName = Transformations.map(winner) {
            if (it != null) {
                "Победитель ${it.user.name}"
            } else {
                ""
            }
        }

        fun setCompetitiveBox(competitiveBox: CompetitiveBox?) {
            val oldField = field.value?.toMutableList()
            if (oldField != null) {
                if (competitiveBox != null) {
                    val oldBox = oldField.find { it.x == competitiveBox.x && it.y == competitiveBox.y }
                    val oldBoxIndex = oldField.indexOfFirst { it.x == competitiveBox.x && it.y == competitiveBox.y }
                    if (oldBoxIndex != -1 && oldBox != null) {
                        oldField[oldBoxIndex] = oldBox.copy(color = "gray")
                        Timber.d("oldField: $oldField")
                        field.postValue(oldField)
                    } else {
                        Timber.e("oldField error")
                    }
                }
            }
        }

        fun replaceBox(box: Box) {
            val oldField = field.value?.toMutableList()
            if (oldField != null) {
                val oldBox = oldField.find { it.x == box.x && it.y == box.y }
                val oldBoxIndex = oldField.indexOfFirst { it.x == box.x && it.y == box.y }
                if (oldBoxIndex != -1 && oldBox != null) {
                    oldField[oldBoxIndex] = box
                } else {
                    Timber.e("oldField error")
                }

                Timber.d("oldField: $oldField")
                field.postValue(oldField)
            }
        }

        fun removeBoxes(boxes: List<Box>) {
            val oldField = field.value?.toMutableList()
            if (oldField != null) {
                for (box in boxes) {
                    val oldBox = oldField.find { it.x == box.x && it.y == box.y }
                    val oldBoxIndex = oldField.indexOfFirst { it.x == box.x && it.y == box.y }
                    if (oldBoxIndex != -1 && oldBox != null) {
                        oldField[oldBoxIndex] = oldBox.copy(color = "white", cost = 0, user_color_id = null)
                    } else {
                        Timber.e("oldField error")
                    }
                }
                Timber.d("oldField: $oldField")
                field.postValue(oldField)
            }
        }

        fun setWhoMoves(userColor: UserColor?) {
            whoMoves.postValue(userColor)
        }

        fun setQuestion(q: Question?) {
            if (question.value != null && q != null) {
                showExactQuestion.postValue(false)
            }
            question.postValue(q)
        }

        fun setPlayers(userColors: List<UserColor>) {
            players.postValue(userColors)
        }

        fun setWinner(userColor: UserColor?) {
            winner.postValue(userColor)
        }

        fun setGameError(throwable: Throwable) {
            gameDetails.value = Result(
                Result.Status.COMPLETED,
                null,
                throwable
            )
        }

        fun setGameLoading() {
            gameDetails.value =
                    Result(Result.Status.LOADING, null, null)
        }


        fun setGame(game: GameDetails) {
            gameDetails.value = Result(
                Result.Status.COMPLETED,
                game,
                null
            )
            field.value = game.field.flatten()
            setWhoMoves(game.who_moves)
            setPlayers(game.user_colors)
            setWinner(game.winner)
            setCompetitiveBox(game.competitive_box)
            setQuestion(game.question)
        }


        val gameMessages = MutableLiveData<Result<List<GameMessage>>>()
        val messages = MutableLiveData<List<GameMessage>>()

        val onlineUsers = MutableLiveData<List<com.gizmodev.conquiz.model.User>>()
        val onlineUsersText = Transformations.map(onlineUsers) { list ->
            list.joinToString { it.name }
        }

        fun setGameMessagesError(throwable: Throwable) {
            gameMessages.value = Result(
                Result.Status.COMPLETED,
                null,
                throwable
            )
        }

        fun setGameMessagesLoading() {
            gameMessages.value =
                Result(Result.Status.LOADING, null, null)
        }

        fun setGameMessages(gm: List<GameMessageGroup>) {
            val msgs = gm.flatMap { it.messages }
            gameMessages.value = Result(
                Result.Status.COMPLETED,
                msgs,
                null
            )
            messages.value = msgs
        }

        fun addGameMessage(gm: GameMessage) {
            val m = messages.value?.toMutableList()
            if (m != null) {
                m.add(gm)
                messages.postValue(m)
            }
        }
    }
}