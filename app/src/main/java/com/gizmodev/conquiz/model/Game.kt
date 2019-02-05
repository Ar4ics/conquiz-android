package com.gizmodev.conquiz.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Game(
    val id: Int,
    val title: String,
    val count_x: Int,
    val count_y: Int,
    val stage: String?,
    val date: String,
    val current_question: Question?,
    val user_colors: List<UserColor>,
    val winner_user_color_id: Int?
) : Parcelable {
    fun players(): String {
        return user_colors.joinToString { it.user.name }
    }

    fun isFinished(): Boolean {
        return winner_user_color_id != null
    }

    fun getStageText(): String =
        when (stage) {
            "GAME_STAGE_1" -> "Выбор базы"
            "GAME_STAGE_2" -> "Завоевание"
            "GAME_STAGE_3" -> "Раздел"
            "GAME_STAGE_4" -> "Битва"
            else -> ""
        }
}
