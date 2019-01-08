package com.gizmodev.conquiz

import android.app.Activity
import android.app.Application
import com.gizmodev.conquiz.injection.component.AppComponent
import com.gizmodev.conquiz.injection.component.DaggerAppComponent
import com.gizmodev.conquiz.injection.module.NetworkModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import timber.log.Timber
import javax.inject.Inject


class MainApplication : Application(), HasActivityInjector {
    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // initialize Dagger
        component = DaggerAppComponent.builder()
            .application(this)
            .context(this)
            .networkModule(NetworkModule())
            .build()
            .inject(this)
    }

    // this is required to setup Dagger2 for Activity
    override fun activityInjector(): AndroidInjector<Activity> = activityInjector
}