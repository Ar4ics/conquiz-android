package com.gizmodev.conquiz.injection.module

import com.gizmodev.conquiz.MainActivity
import com.gizmodev.conquiz.ui.game.GameActivity
import com.gizmodev.conquiz.ui.game.GameListActivity
import com.gizmodev.conquiz.utils.Constants.GAME_ID
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun contributeGameListActivity(): GameListActivity

    @ContributesAndroidInjector
    internal abstract fun contributeGameActivity(): GameActivity

}
