package com.gizmodev.conquiz.model

data class Box(val x: Int, val y: Int, val color: String, val cost: Int, val user_color_id: Int?, val loss_user_color_id: Int?, val base: Base?) {

    fun getBoxCost() : String {
        return if (cost != 0) {
            if (base != null) {
                "$cost <${base.guards}>"
            } else {
                "$cost"
            }
        } else {
            ""
        }
    }

    fun getUserBase() : String {
        return if (base != null) {
            base.user_name
        } else {
            ""
        }
    }
}
