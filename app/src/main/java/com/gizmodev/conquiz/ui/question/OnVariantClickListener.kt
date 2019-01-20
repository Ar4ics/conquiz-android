package com.gizmodev.conquiz.ui.question

import com.gizmodev.conquiz.model.AnswerVariant

interface OnVariantClickListener {
    fun onVariantClick(answerVariant: AnswerVariant)

}