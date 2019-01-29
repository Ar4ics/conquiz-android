package com.gizmodev.conquiz.model

data class GameMessage(val id: Int, val message: String, val game_id: Int, val user_id: Int, val user: User, val time: String, val date: String)
data class GameMessageGroup(val date: String, val messages: List<GameMessage>)