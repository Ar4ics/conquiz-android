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
import com.gizmodev.conquiz.utils.EventBus
import com.gizmodev.conquiz.utils.toString
import com.pusher.client.channel.ChannelEventListener
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
    val game = gameHolder.game

    init {
        if (game != null) {
            state.setGameLoading()
            loadPusher()
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

            EventBus.subscribe<List<Box>>()
                .subscribe {
                    Timber.d("List<Box> received: $it")
                    state.removeBoxes(it)
                }.untilCleared()

            EventBus.subscribe<Box>()
                .subscribe {
                    Timber.d("Box received: $it")
                    state.replaceBox(it)
                }.untilCleared()
        } else {
            Timber.e("game $game not found")
        }
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

    private fun loadPusher() {
        val id = gameHolder.game?.id ?: return
        val pusher = pusherHolder.pusher ?: return
        pusher.subscribe(
            "game.$id",
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
                        PusherHolder.AnswersResults -> {
                            val answersResults = Result.fromJson<AnswerResults>(data)
                            Timber.d("AnswerResults: $answersResults")
                            if (answersResults != null) {
                                state.answerResults.postValue(answersResults)
                                Timber.d("start delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                Completable
                                    .timer(3, TimeUnit.SECONDS)
                                    .subscribeBy(
                                        onComplete = {
                                            state.setQuestion(null)
                                            state.removeBoxes(answersResults.deleted_boxes)
                                            Timber.d("complete delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                        },
                                        onError = {
                                            Timber.e("error delay: ${it.localizedMessage}")
                                        }
                                    ).untilCleared()
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.CorrectAnswers -> {
                            val correctAnswers = Result.fromJson<CorrectAnswerResults>(data)
                            Timber.d("CorrectAnswerResults: $correctAnswers")
                            if (correctAnswers != null) {
                                state.answerResults.postValue(
                                    AnswerResults(
                                        correctAnswers.results,
                                        correctAnswers.correct,
                                        listOf()
                                    )
                                )
                            }
                        }
                        PusherHolder.CompetitiveAnswerResults -> {
                            val competitiveAnswerResults = Result.fromJson<CompetitiveAnswerResults>(data)
                            Timber.d("CompetitiveAnswerResults: $competitiveAnswerResults")
                            if (competitiveAnswerResults != null) {
                                state.competitiveAnswerResults.postValue(competitiveAnswerResults)
                                Timber.d("start delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                Completable
                                    .timer(3, TimeUnit.SECONDS)
                                    .subscribeBy(
                                        onComplete = {
                                            state.setQuestion(null)
                                            state.setCompetitiveBox(null)
                                            state.replaceBoxes(competitiveAnswerResults)
                                            Timber.d("complete delay: ${DateTimePicker.getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss")}")
                                        },
                                        onError = {
                                            Timber.e("error delay: ${it.localizedMessage}")
                                        }
                                    ).untilCleared()
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                        PusherHolder.NewQuestion, PusherHolder.NewExactQuestion -> {
                            val question = Result.fromJson<Question>(data)
                            Timber.d("question: $question")
                            gameHolder.question = question
                            if (question != null) {
                                state.setQuestion(question)
                            } else {
                                pusherHolder.errorParsingJson()
                            }
                        }
                    }
                }
            },
            *PusherHolder.AllEvents
        )

        state.onlineUsers.postValue(gameHolder.onlineUsers.value[id])

        gameHolder.onlineUsers.observable.subscribe {
            Timber.d("online users: $it")
            val users = it[id]
            state.onlineUsers.postValue(users)
        }.untilCleared()

        pusherHolder.connectPresence(id)

    }

    override fun onCleared() {
        super.onCleared()
        val id = gameHolder.game?.id ?: return
        val pusher = pusherHolder.pusher ?: return
        pusher.unsubscribe("game.$id")
        pusherHolder.disconnectPresence(id)
    }

    class State {
        val gameDetails = MutableLiveData<Result<GameDetails>>()
        val field = MutableLiveData<List<Box>>()
        val players = MutableLiveData<List<UserColor>>()
        val question = MutableLiveData<Question?>()
        val whoMoves = MutableLiveData<UserColor?>()
        val winner = MutableLiveData<UserColor?>()
        val competitiveBox = MutableLiveData<CompetitiveBox?>()
        val answerResults = MutableLiveData<AnswerResults>()
        val competitiveAnswerResults = MutableLiveData<CompetitiveAnswerResults>()

        fun setQuestion(q: Question?) {
            question.postValue(q)
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

        val competitiveTitle = Transformations.map(competitiveBox) {
            var title = ""
            if (it != null) {
                if (it.competitors == null || it.competitors.isEmpty()) {
                    title = "Разделение территории"
                } else {
                    val players = players.value
                    val c1 = players?.find { p -> p.id == it.competitors[0] }
                    val c2 = players?.find { p -> p.id == it.competitors[1] }
                    if (c1 != null && c2 != null) {
                        title = "${c1.user.name} нападает на ${c2.user.name}"
                    }
                }
            } else {
                title = "Захват территории"
            }
            Timber.d("title: $title")
            title
        }

        fun setCompetitiveBox(competitiveBox: CompetitiveBox?) {

            this.competitiveBox.postValue(competitiveBox)
            Timber.d("competitiveBox: $competitiveBox")

            if (competitiveBox != null) {
                val oldField = field.value?.toMutableList() ?: return
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
            setQuestion(game.question)
            setWhoMoves(game.who_moves)
            setPlayers(game.user_colors)
            setWinner(game.winner)
            setCompetitiveBox(game.competitive_box)
        }


        val gameMessages = MutableLiveData<Result<List<GameMessage>>>()
        val messages = MutableLiveData<List<GameMessage>>()

        val onlineUsers = MutableLiveData<List<com.gizmodev.conquiz.model.User>?>()
        val onlineUsersText = Transformations.map(onlineUsers) { list ->
            list?.joinToString { it.name }
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

        fun replaceBoxes(competitiveAnswerResults: CompetitiveAnswerResults) {
            val targetBox = competitiveAnswerResults.result_box
            val winner = competitiveAnswerResults.winner
            val oldField = field.value?.toMutableList()
            if (oldField != null) {
                val oldBox = oldField.find { it.x == targetBox.x && it.y == targetBox.y }
                val oldBoxIndex = oldField.indexOfFirst { it.x == targetBox.x && it.y == targetBox.y }
                if (oldBoxIndex != -1 && oldBox != null) {
                    oldField[oldBoxIndex] = targetBox
                } else {
                    Timber.e("oldField error")
                }
                if (winner != null &&
                    targetBox.loss_user_color_id != null
                ) {
                    val newField = oldField.map {
                        if (it.user_color_id == targetBox.loss_user_color_id) {
                            it.copy(color = winner.color, user_color_id = winner.id)
                        } else it
                    }
                    Timber.d("newField: $newField")
                    field.postValue(newField)
                } else {
                    Timber.d("oldField: $oldField")
                    field.postValue(oldField)
                }
            }
        }
    }
}