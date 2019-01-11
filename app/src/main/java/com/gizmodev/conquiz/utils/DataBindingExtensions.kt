package com.gizmodev.conquiz.utils

import com.gizmodev.conquiz.model.Base
import com.gizmodev.conquiz.model.Box
import com.gizmodev.conquiz.model.GameDetails
import com.gizmodev.conquiz.model.UserColor

object DataBindingExtensions {

    @JvmStatic
    fun getMovingUser(userColor: UserColor?) : String {
        return if (userColor?.user != null) {
            "Ходит ${userColor.user.name}"
        } else {
            ""
        }
    }

    @JvmStatic
    fun getBoxCost(box: Box?) : String {
        return if (box != null && box.cost != 0) {
            "${box.cost}"
        } else {
            ""
        }
    }

    @JvmStatic
    fun movingUserIsVisible(gameDetails: GameDetails?) : Boolean {
        return gameDetails?.who_moves != null && gameDetails.winner == null
    }

    @JvmStatic
    fun getWinner(gameDetails: GameDetails?) : String {
        return if (gameDetails?.winner != null) {
            "Победитель ${gameDetails.winner.user?.name}"
        } else {
            ""
        }
    }

    @JvmStatic
    fun getUserPlace(userColor: UserColor?) : String {
        return if (userColor?.place != null) {
            "${userColor.place} место"
        } else {
            ""
        }
    }

    @JvmStatic
    fun getUserBase(base: Base?) : String {
        return if (base != null) {
            "${base.user_name} <${base.guards}>"
        } else {
            ""
        }
    }
}