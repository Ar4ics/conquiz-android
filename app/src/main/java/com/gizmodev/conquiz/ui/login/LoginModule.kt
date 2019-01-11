package com.gizmodev.conquiz.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.gizmodev.conquiz.network.AuthenticationInterceptor
import com.gizmodev.conquiz.network.LoginApi
import com.gizmodev.conquiz.ui.core.ViewModelKey
import com.gizmodev.conquiz.utils.SharedPrefStorage
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [
    LoginModule.ProvideViewModel::class
])
abstract class LoginModule {

    @ContributesAndroidInjector(modules = [
        InjectViewModel::class
    ])
    abstract fun bind(): LoginFragment

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(LoginViewModel::class)
        fun provideLoginViewModel(
            loginApi: LoginApi,
            sharedPrefStorage: SharedPrefStorage,
            authenticationInterceptor: AuthenticationInterceptor
        ): ViewModel = LoginViewModel(loginApi, sharedPrefStorage, authenticationInterceptor)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideLoginViewModel(
            factory: ViewModelProvider.Factory,
            target: LoginFragment
        ) = ViewModelProviders.of(target, factory).get(LoginViewModel::class.java)
    }

}