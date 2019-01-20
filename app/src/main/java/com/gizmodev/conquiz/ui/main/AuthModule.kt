package com.gizmodev.conquiz.ui.main

import com.gizmodev.conquiz.network.GameHolder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AuthModule {

    @Provides
    @Singleton
    fun provideAuthHolder(): GameHolder {
        return GameHolder()
    }

}