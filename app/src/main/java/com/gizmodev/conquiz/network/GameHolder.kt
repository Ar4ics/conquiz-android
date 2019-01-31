package com.gizmodev.conquiz.network

import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.model.Question
import com.gizmodev.conquiz.model.User
import com.gizmodev.conquiz.model.UserColor
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameHolder @Inject constructor() {
    var user: User? = null
    var player: UserColor? = null
    var question: Question? = null
    var game: Game? = null
    val onlineUsers: Variable<Map<Int, List<User>>> = Variable(mutableMapOf())

}

class Variable<T>(defaultValue: T) {
    var value: T = defaultValue
        set(value) {
            field = value
            observable.onNext(value)
        }

    val observable = BehaviorSubject.createDefault(value)
}