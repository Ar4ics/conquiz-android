package com.gizmodev.conquiz.injection.component

import android.app.Application
import android.content.Context
import com.gizmodev.conquiz.MainApplication
import com.gizmodev.conquiz.injection.module.ActivityModule
import com.gizmodev.conquiz.injection.module.AppModule
import com.gizmodev.conquiz.injection.module.NetworkModule
import com.gizmodev.conquiz.injection.module.UiModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    UiModule::class,
    NetworkModule::class,
    ActivityModule::class
])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun networkModule(networkModule: NetworkModule): Builder

        fun build(): AppComponent
    }

    fun inject(mainApplication: MainApplication)
}
