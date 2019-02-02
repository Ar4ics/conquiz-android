package com.gizmodev.conquiz.ui.core

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

abstract class AppViewModel : ViewModel() {

    private val disposeOnClear = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposeOnClear.clear()
        Timber.d("onCleared: $this")
    }

    fun Disposable.untilCleared() =
        disposeOnClear.add(this)
}