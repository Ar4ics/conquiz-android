package com.gizmodev.conquiz.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Game(val id: Int, val title: String, val count_x: Int, val date: String, val count_y: Int, val user_colors: List<UserColor>, val winner_user_color_id: Int?): Parcelable {
    fun players(): String {
        return user_colors.map { it.user.name }.toString()
    }

    fun isFinished(): Boolean {
        return winner_user_color_id != null
    }
}
