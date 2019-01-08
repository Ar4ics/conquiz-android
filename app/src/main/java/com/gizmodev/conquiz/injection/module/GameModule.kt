package com.gizmodev.conquiz.injection.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.gizmodev.conquiz.ui.game.GameActivity
import com.gizmodev.conquiz.viewmodel.GameViewModel
import com.gizmodev.conquiz.ui.core.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [
    GameModule.ProvideViewModel::class
])
abstract class GameModule {

    /* Install module into subcomponent to have access to bound fragment instance */
    @ContributesAndroidInjector(modules = [
        InjectViewModel::class
    ])
    abstract fun bind(): GameActivity

    /* Module that uses bound fragment and provided factory uses ViewModelProviders
        to provide instance of FeatureViewModel */
    @Module
    class InjectViewModel {

        @Provides
        fun provideGameViewModel(
            factory: ViewModelProvider.Factory,
            target: GameActivity
        ) = ViewModelProviders.of(target, factory).get(GameViewModel::class.java)

    }

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(GameViewModel::class)
        fun provideGameViewModel(expensive: Expensive): ViewModel =
            GameViewModel(expensive)

    }

}