package com.gizmodev.conquiz.model

data class Question(val id: Int, val title: String?, val answers: List<String>?, val is_exact_answer: Boolean)