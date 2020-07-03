package com.funnyvo.android.di

import android.content.Context
import com.funnyvo.android.customview.ActivityIndicator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Singleton

@InstallIn(ActivityComponent::class)
@Module
object AppModule {

    @Provides
    fun provideIndicator(@ActivityContext activity: Context): ActivityIndicator {
        return ActivityIndicator(activity)
    }

}