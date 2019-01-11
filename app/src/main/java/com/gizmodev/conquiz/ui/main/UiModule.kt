package com.gizmodev.conquiz.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gizmodev.conquiz.ui.core.AppViewModelFactory
import com.gizmodev.conquiz.ui.game.GameModule
import com.gizmodev.conquiz.ui.games.GameListModule
import com.gizmodev.conquiz.ui.login.LoginModule
import com.gizmodev.conquiz.ui.profile.ProfileModule
import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module(includes = [
    GameListModule::class,
    GameModule::class,
    LoginModule::class,
    ProfileModule::class
])
class UiModule {

    @Provides
    fun provideViewModelFactory(
        providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory =
        AppViewModelFactory(providers)
}