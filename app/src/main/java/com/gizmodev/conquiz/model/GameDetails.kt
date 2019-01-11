package com.gizmodev.conquiz.model

data class GameDetails(
    val game: Game,
    val field: List<List<Box>>,
    val user_colors: List<UserColor>,
    val player: UserColor?,
    val who_moves: UserColor?,
    val question: Question?,
    val competitive_box: CompetitiveBox?,
    val winner: UserColor?
)