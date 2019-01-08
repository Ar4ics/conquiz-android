package com.gizmodev.conquiz.injection.module

import com.gizmodev.conquiz.network.GameApi
import com.gizmodev.conquiz.network.LoginApi
import com.gizmodev.conquiz.utils.AuthenticationInterceptor
import com.gizmodev.conquiz.utils.Constants.REST_API_URL
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Reusable
    internal fun provideGameApi(retrofit: Retrofit): GameApi {
        return retrofit.create(GameApi::class.java)
    }

    @Provides
    @Reusable
    internal fun provideLoginApi(retrofit: Retrofit): LoginApi {
        return retrofit.create(LoginApi::class.java)
    }

    @Provides
    @Reusable
    fun provideOkHttpClient(authenticationInterceptor: AuthenticationInterceptor, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val client = OkHttpClient.Builder()
        client.addInterceptor(authenticationInterceptor)
        client.addInterceptor(loggingInterceptor)
        return client.build()
    }

    @Provides
    @Singleton
    fun provideAuthenticationInterceptor(): AuthenticationInterceptor {
        return AuthenticationInterceptor()
    }

    @Provides
    @Reusable
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return httpLoggingInterceptor
    }

    @Provides
    @Reusable
    internal fun provideRetrofitInterface(client: OkHttpClient): Retrofit {

        return Retrofit.Builder()
            .baseUrl(REST_API_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }


}