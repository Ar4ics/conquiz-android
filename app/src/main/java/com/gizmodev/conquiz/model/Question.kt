package com.gizmodev.conquiz.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Question(val id: Int, val title: String, val answers: List<String>?, val is_exact_answer: Boolean) : Parcelable

@Parcelize
data class GameQuestion(val title: String, val answers: List<AnswerVariant>? = null) : Parcelable