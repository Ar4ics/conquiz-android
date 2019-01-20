package com.gizmodev.conquiz.network

import com.squareup.moshi.Moshi

data class Result<T>(
    val status: Status,
    val data: T?,
    val exception: Throwable?
) {
    enum class Status {
        LOADING,
        COMPLETED,
    }

    companion object {
        inline fun <reified T: Any> fromJson(json: String): T? {
            return Moshi.Builder().build().adapter(T::class.java).fromJson(json)
        }
    }

    fun fail() : Boolean {
        return data == null && status == Status.COMPLETED
    }

    fun success() : Boolean {
        return data != null
    }

    fun loading(): Boolean {
        return status == Status.LOADING
    }

    fun hasError(): Boolean {
        return exception != null
    }

    fun error(): String {
        return exception?.localizedMessage ?: ""
    }
}