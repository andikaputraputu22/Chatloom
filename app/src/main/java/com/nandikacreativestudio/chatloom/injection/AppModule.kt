package com.nandikacreativestudio.chatloom.injection

import com.nandikacreativestudio.chatloom.utils.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideUtils(): Utils {
        return Utils()
    }
}