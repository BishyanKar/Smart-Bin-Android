package com.example.smartbin.di

import com.example.smartbin.repos.HomeRepo
import com.example.smartbin.repos.ProfileRepo
import com.example.smartbin.repos.ScannerRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object CorModule {

    @Provides
    fun provideHomeRepository(): HomeRepo {
        return HomeRepo()
    }
    @Provides
    fun provideScannerRepository(): ScannerRepo {
        return ScannerRepo()
    }
    @Provides
    fun provideProfileRepository(): ProfileRepo {
        return ProfileRepo()
    }
}