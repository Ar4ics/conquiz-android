package com.gizmodev.conquiz.injection.module

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gizmodev.conquiz.ui.core.ViewModelKey
import com.gizmodev.conquiz.ui.game.GameActivity
import com.gizmodev.conquiz.utils.Constants
import com.gizmodev.conquiz.viewmodel.GameListViewModel
import com.gizmodev.conquiz.viewmodel.GameViewModel
import com.gizmodev.conquiz.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @Binds
    abstract fun activity(activity: GameActivity): Activity

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory):
            ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(GameListViewModel::class)
    abstract fun bindGameListViewModel(viewModel : GameListViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameViewModel::class)
    abstract fun bindGameViewModel(viewModel : GameViewModel) : ViewModel


    @Module
    companion object {

        @JvmStatic
        @Provides
        fun provideGameId(activity: GameActivity): Int {
            return activity.intent.getIntExtra(Constants.GAME_ID, 0)
        }
    }
}