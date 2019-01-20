package com.gizmodev.conquiz.ui.question

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
    QuestionModule.ProvideViewModel::class
])
abstract class QuestionModule {

    @ContributesAndroidInjector(modules = [
        InjectViewModel::class
    ])
    abstract fun bind(): QuestionFragment

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(QuestionViewModel::class)
        fun provideQuestionViewModel(
            gameApi: GameApi,
            pusherHolder: PusherHolder,
            gameHolder: GameHolder
        ): ViewModel = QuestionViewModel(gameApi, pusherHolder, gameHolder)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideQuestionViewModel(
            factory: ViewModelProvider.Factory,
            target: QuestionFragment
        ) = ViewModelProviders.of(target, factory).get(QuestionViewModel::class.java)
    }

}