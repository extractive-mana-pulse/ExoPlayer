package com.example.exoplayertr.di

import android.content.Context
import com.example.exoplayertr.data.repoImpl.SpeechRecognitionManager
import com.example.exoplayertr.data.repoImpl.VideoPlayerManager
import com.example.exoplayertr.domain.repo.SpeechRecognitionManagerRepo
import com.example.exoplayertr.domain.repo.VideoPlayerManagerRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideVideoPlayerManager(
        @ApplicationContext context: Context
    ): VideoPlayerManagerRepo {
        return VideoPlayerManager(context)
    }

    @Provides
    fun provideSpeechRecognitionManager(
        @ApplicationContext context: Context,
    ): SpeechRecognitionManagerRepo {
        return SpeechRecognitionManager(context)
    }
}