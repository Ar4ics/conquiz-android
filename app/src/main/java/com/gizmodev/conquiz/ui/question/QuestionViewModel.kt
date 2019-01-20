package com.gizmodev.conquiz.ui.question

import androidx.lifecycle.MutableLiveData
import com.gizmodev.conquiz.model.*
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.network.GameHolder
import com.gizmodev.conquiz.network.PusherHolder
import com.gizmodev.conquiz.network.Result
import com.gizmodev.conquiz.ui.core.AppViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber

class QuestionViewModel(
    private val gameApi: GameApi,
    private val pusherHolder: PusherHolder,
    private val gameHolder: GameHolder
) : AppViewModel() {

    val state = State()

    var av: AnswerVariant? = null

    fun answerToQuestion(answerVariant: AnswerVariant) {
        val player = gameHolder.player
        val game = gameHolder.game
        val question = gameHolder.question

        av = answerVariant

        if (player != null && game != null && question != null) {
            gameApi.questionAnswer(game.id, answerVariant.value, question.id, player.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> onAnswerSuccess(result) },
                    { error -> onAnswerError(error) }
                ).untilCleared()
        }
    }


    private fun onAnswerSuccess(result: Response<ResponseBody>) {
        val response = result.body()?.string()
        Timber.d("success answer: $response")
        if (response != null && !response.contains("error", ignoreCase = true)) {
            val answerVariant = av
            answerVariant?.let {
                state.setVariant(answerVariant.copy(picked = true))
            }
        }
    }


    private fun onAnswerError(throwable: Throwable) {
        Timber.e("failed to answer: ${throwable.localizedMessage}")
        state.setQuestionError(throwable)
    }

    class State {
        val gameQuestion = MutableLiveData<Result<GameQuestion>>()

        val question = MutableLiveData<GameQuestion>()

        fun setQuestionError(throwable: Throwable) {
            gameQuestion.postValue(
                Result(
                    Result.Status.COMPLETED,
                    null,
                    throwable
                )
            )
        }

        fun setQuestion(q: Question) {
            if (q.answers != null) {
                val answers = mutableListOf<AnswerVariant>()

                for ((index, ans) in q.answers.withIndex()) {
                    answers.add(AnswerVariant(title = ans, value = index))
                }

                val gq = GameQuestion(title = q.title, answers = answers)
                question.postValue(gq)
            } else {
                val gq = GameQuestion(title = q.title)
                question.postValue(gq)
            }
        }

        fun setAnswerResults(ar: AnswerResults) {
            Timber.d("setting AnswerResults: $ar")
            val gq = question.value
            if (gq?.answers != null) {
                val newAnswerVariants = gq.answers.toMutableList()
                for ((index, av) in gq.answers.withIndex()) {
                    val users = ar.results.filter { it.answer == av.value }
                    if (users.isNotEmpty()) {
                        newAnswerVariants[index] = newAnswerVariants[index].copy(
                            is_correct = users[0].is_correct,
                            players = users.map { it.user_name to it.time })
                    }
                }
                val correct = newAnswerVariants.indexOfFirst { it.value == ar.correct }
                if (correct > 0) {
                    newAnswerVariants[correct] = newAnswerVariants[correct].copy(is_correct = true)
                }
                val newGameQuestion = gq.copy(answers = newAnswerVariants)
                Timber.d("results: $newGameQuestion")
                question.postValue(newGameQuestion)
            } else {
                Timber.e("error setting AnswerResults")
            }
        }

        fun setCompetitiveAnswerResults(car: CompetitiveAnswerResults) {
            Timber.d("setting CompetitiveAnswerResults: $car")
            val gq = question.value
            if (gq != null) {
                if (gq.answers != null) {
                    val newAnswerVariants = gq.answers.toMutableList()
                    for ((index, av) in gq.answers.withIndex()) {
                        val users = car.results.filter { it.answer == av.value }
                        if (users.isNotEmpty()) {
                            newAnswerVariants[index] = newAnswerVariants[index].copy(
                                is_correct = users[0].is_correct,
                                players = users.map { it.user_name to it.time })
                        }
                    }
                    val correct = newAnswerVariants.indexOfFirst { it.value == car.correct }
                    if (correct > 0) {
                        newAnswerVariants[correct] = newAnswerVariants[correct].copy(is_correct = true)
                    }
                    val newGameQuestion = gq.copy(answers = newAnswerVariants)
                    Timber.d("results: $newGameQuestion")
                    question.postValue(newGameQuestion)
                } else {
                    val newAnswerVariants = mutableListOf<AnswerVariant>()
                    newAnswerVariants.add(
                        AnswerVariant(
                            title = "Правильный ответ: ${car.correct}",
                            value = car.correct,
                            is_correct = true
                        )
                    )
                    if (car.winner != null) {
                        newAnswerVariants.add(
                            AnswerVariant(
                                title = "Победил: ${car.winner.user.name}",
                                value = car.correct
                            )
                        )
                    }
                    for ((index, result) in car.results.withIndex()) {
                        newAnswerVariants.add(
                            AnswerVariant(
                                title = "Дан ответ: ${result.answer}",
                                value = result.answer,
                                is_correct = result.is_correct,
                                players = listOf(result.user_name to result.time)
                            )
                        )
                    }
                    val newGameQuestion = gq.copy(answers = newAnswerVariants)
                    Timber.d("results exact: $newGameQuestion")
                    question.postValue(newGameQuestion)
                }
            } else {
                Timber.e("error setting CompetitiveAnswerResults")
            }
        }

        fun setVariant(answerVariant: AnswerVariant) {
            Timber.d("setting variant: $answerVariant")
            val gq = question.value
            if (gq != null) {
                if (gq.answers != null) {

                    val newAnswerVariants = gq.answers.toMutableList()
                    val new = gq.answers.indexOfFirst { it.value == answerVariant.value }
                    if (new > 0) {
                        newAnswerVariants[new] = answerVariant
                        val newGameQuestion = gq.copy(answers = newAnswerVariants)
                        Timber.d("set variant: $newGameQuestion")
                        question.postValue(newGameQuestion)
                    } else {
                        Timber.e("error setting variant")
                    }
                } else {
                    val newAnswerVariants = mutableListOf<AnswerVariant>()
                    newAnswerVariants.add(answerVariant)
                    val newGameQuestion = gq.copy(answers = newAnswerVariants)
                    Timber.d("set variant exact: $newGameQuestion")
                    question.postValue(newGameQuestion)
                }
            } else {
                Timber.e("error setting AnswerVariant")
            }
        }
    }

}