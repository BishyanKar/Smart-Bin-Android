package com.example.smartbin.di

import android.content.Context
import android.content.SharedPreferences
import com.example.smartbin.Constants
import com.example.smartbin.R
import com.example.smartbin.api.ApiService
import com.example.smartbin.api.AuthorizationInterceptor
import com.example.smartbin.api.LiveDataCallAdapterFactory
import com.example.smartbin.repos.HomeRepo
import com.example.smartbin.repos.ProfileRepo
import com.example.smartbin.repos.ScannerRepo
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@InstallIn(SingletonComponent::class)
@Module
object CoreModule {

    @Provides
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences = appContext.getSharedPreferences(appContext.getString(R.string.app_name), Context.MODE_PRIVATE)

    @Provides
    fun provideGson(): Gson = Gson()

    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun providesOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor, @ApplicationContext appContext: Context): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(AuthorizationInterceptor(appContext))
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .addCallAdapterFactory(LiveDataCallAdapterFactory())
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)


    @Provides
    fun provideHomeRepository(apiService: ApiService): HomeRepo {
        return HomeRepo(apiService)
    }
    @Provides
    fun provideScannerRepository(): ScannerRepo {
        return ScannerRepo()
    }
    @Provides
    fun provideProfileRepository(apiService: ApiService): ProfileRepo {
        return ProfileRepo(apiService)
    }
}