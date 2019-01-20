package com.gizmodev.conquiz.network

import com.gizmodev.conquiz.utils.Constants.REST_API_URL
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    internal fun provideGameApi(retrofit: Retrofit): GameApi {
        return retrofit.create(GameApi::class.java)
    }

    @Provides
    @Singleton
    internal fun provideLoginApi(retrofit: Retrofit): LoginApi {
        return retrofit.create(LoginApi::class.java)
    }

    @Provides
    @Singleton
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
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return httpLoggingInterceptor
    }

    @Provides
    @Singleton
    internal fun provideRetrofitInterface(client: OkHttpClient): Retrofit {

        return Retrofit.Builder()
            .baseUrl(REST_API_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }


}