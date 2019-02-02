package com.gizmodev.conquiz.ui.games

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
    GameListModule.ProvideViewModel::class
])
abstract class GameListModule {

    @ContributesAndroidInjector(modules = [
        InjectViewModel::class
    ])
    abstract fun bind(): GameListFragment

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(GameListViewModel::class)
        fun provideGameListViewModel(
            gameApi: GameApi,
            gameHolder: GameHolder,
            pusherHolder: PusherHolder
        ): ViewModel =
            GameListViewModel(gameApi, gameHolder, pusherHolder)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideGameListViewModel(
            factory: ViewModelProvider.Factory,
            target: GameListFragment
        ) = ViewModelProviders.of(target, factory).get(GameListViewModel::class.java)
    }

}