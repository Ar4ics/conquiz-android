package com.gizmodev.conquiz.ui.main

import android.content.Context
import com.gizmodev.conquiz.network.NetworkModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Component(modules = [
    NetworkModule::class,
    AppModule::class,
    AndroidSupportInjectionModule::class,
    UiModule::class,
    AuthModule::class
])
@Singleton
interface AppComponent {

    fun inject(app: App)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: App): Builder

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}