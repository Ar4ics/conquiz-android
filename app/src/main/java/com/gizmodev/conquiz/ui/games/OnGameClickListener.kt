package com.gizmodev.conquiz.ui.games

import com.gizmodev.conquiz.model.Game

interface OnGameClickListener {
    fun onGameClicked(game: Game)
}