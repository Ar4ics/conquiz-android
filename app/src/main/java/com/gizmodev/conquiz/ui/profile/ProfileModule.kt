package com.gizmodev.conquiz.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.gizmodev.conquiz.network.AuthenticationInterceptor
import com.gizmodev.conquiz.network.GameHolder
import com.gizmodev.conquiz.network.LoginApi
import com.gizmodev.conquiz.network.PusherHolder
import com.gizmodev.conquiz.ui.core.ViewModelKey
import com.gizmodev.conquiz.utils.SharedPrefStorage
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [
    ProfileModule.ProvideViewModel::class
])
abstract class ProfileModule {

    @ContributesAndroidInjector(modules = [
        InjectViewModel::class
    ])
    abstract fun bind(): ProfileFragment

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(ProfileViewModel::class)
        fun provideProfileViewModel(
            loginApi: LoginApi,
            sharedPrefStorage: SharedPrefStorage,
            authenticationInterceptor: AuthenticationInterceptor,
            gameHolder: GameHolder,
            pusherHolder: PusherHolder
        ): ViewModel = ProfileViewModel(loginApi, sharedPrefStorage, authenticationInterceptor, gameHolder, pusherHolder)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideProfileViewModel(
            factory: ViewModelProvider.Factory,
            target: ProfileFragment
        ) = ViewModelProviders.of(target, factory).get(ProfileViewModel::class.java)
    }

}