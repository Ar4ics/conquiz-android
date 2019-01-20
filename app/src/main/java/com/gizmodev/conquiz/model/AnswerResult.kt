package com.gizmodev.conquiz.model

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.android.parcel.Parcelize

data class Result(val user_name: String, val answer: Int, val is_correct: Boolean, val time: String)

data class AnswerResults(val results: List<Result>, val correct: Int, val deleted_boxes: List<Box>)
data class CorrectAnswerResults(val results: List<Result>, val correct: Int)
data class CompetitiveAnswerResults(val results: List<Result>, val correct: Int, val result_box: Box, val is_exact: Boolean, val winner: UserColor?)

@Parcelize
data class AnswerVariant(val title: String, val value: Int, val is_correct: Boolean? = null, val players: List<Pair<String, String>>? = null, val picked: Boolean = false) : Parcelable {
    fun getPlayers() : String {
        return players?.joinToString() ?: ""
    }

    fun isResultsExists(): Boolean {
        return players != null && is_correct != null
    }

    @ColorInt
    fun getColor(): Int {
        if (is_correct == null) {
            return getPickedColor()
        } else {
            return if (is_correct) Color.GREEN else Color.RED
        }
    }

    @ColorInt
    fun getPickedColor(): Int {
        return if (picked) Color.YELLOW else Color.WHITE
    }
}