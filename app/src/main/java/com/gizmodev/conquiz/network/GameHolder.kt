package com.gizmodev.conquiz.network

import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.model.Question
import com.gizmodev.conquiz.model.User
import com.gizmodev.conquiz.model.UserColor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameHolder @Inject constructor() {
    var user: User? = null
    var player: UserColor? = null
    var question: Question? = null
    var game: Game? = null
}