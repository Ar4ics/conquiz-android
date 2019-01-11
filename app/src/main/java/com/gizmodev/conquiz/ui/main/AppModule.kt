package com.gizmodev.conquiz.ui.main

import android.content.Context
import com.gizmodev.conquiz.network.AuthenticationInterceptor
import com.gizmodev.conquiz.utils.PusherHelper
import com.gizmodev.conquiz.utils.SharedPrefStorage
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class AppModule {

    @Provides
    @Singleton
    fun providesSharedPrefStorage(context: Context): SharedPrefStorage {
        return SharedPrefStorage(context);
    }

    @Provides
    @Singleton
    fun providesPusherHelper(context: Context, authenticationInterceptor: AuthenticationInterceptor): PusherHelper {
        return PusherHelper(context, authenticationInterceptor)
    }
}