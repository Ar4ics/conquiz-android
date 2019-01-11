package com.gizmodev.conquiz.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.ui.core.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [
    GameModule.ProvideViewModel::class
])
abstract class GameModule {

    @ContributesAndroidInjector(modules = [
        InjectViewModel::class
    ])
    abstract fun bind(): GameFragment

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(GameViewModel::class)
        fun provideProfileViewModel(
            gameApi: GameApi
        ): ViewModel = GameViewModel(gameApi)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideProfileViewModel(
            factory: ViewModelProvider.Factory,
            target: GameFragment
        ) = ViewModelProviders.of(target, factory).get(GameViewModel::class.java)
    }

}