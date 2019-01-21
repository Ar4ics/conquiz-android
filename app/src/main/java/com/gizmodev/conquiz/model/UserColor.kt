package com.gizmodev.conquiz.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserColor(val id: Int, val user_id: Int, val color: String, val score: Int, val user: User, val place: Int?): Parcelable {
    fun getUserPlace() : String {
        return if (place != null) {
            "$place место"
        } else {
            ""
        }
    }
}

data class Players(val user_colors: List<UserColor>)
data class WhoMoves(val user_color: UserColor)
data class WhoAttack(val competitive_box: CompetitiveBox)
data class Winner(val winner: UserColor)