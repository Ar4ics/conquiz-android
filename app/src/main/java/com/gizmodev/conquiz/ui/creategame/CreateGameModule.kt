package com.gizmodev.conquiz.ui.creategame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.network.GameHolder
import com.gizmodev.conquiz.network.PusherHolder
import com.gizmodev.conquiz.ui.core.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [
    CreateGameModule.ProvideViewModel::class
])
abstract class CreateGameModule {

    @ContributesAndroidInjector(modules = [
        InjectViewModel::class
    ])
    abstract fun bind(): CreateGameFragment

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(CreateGameViewModel::class)
        fun provideProfileViewModel(
            gameApi: GameApi,
            pusherHolder: PusherHolder,
            gameHolder: GameHolder
        ): ViewModel = CreateGameViewModel(gameApi, pusherHolder, gameHolder)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideProfileViewModel(
            factory: ViewModelProvider.Factory,
            target: CreateGameFragment
        ) = ViewModelProviders.of(target, factory).get(CreateGameViewModel::class.java)
    }

}