package com.gizmodev.conquiz.injection.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gizmodev.conquiz.ui.core.AppViewModelFactory
import dagger.Provides
import javax.inject.Provider

class UiModule {

    @Provides
    fun provideViewModelFactory(
        providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory =
        AppViewModelFactory(providers)
}