package com.gizmodev.conquiz.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Game(val id: Int, val title: String, val count_x: Int, val count_y: Int, val winner_user_color_id: Int?): Parcelable
