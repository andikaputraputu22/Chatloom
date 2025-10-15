package com.nandikacreativestudio.chatloom.injection

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.nandikacreativestudio.chatloom.api.ApiService
import com.nandikacreativestudio.chatloom.repository.ChatRepository
import com.nandikacreativestudio.chatloom.repository.GoogleAuthRepository
import com.nandikacreativestudio.chatloom.utils.SharedPreferencesManager
import com.nandikacreativestudio.chatloom.utils.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideChatRepository(
        apiService: ApiService,
        utils: Utils,
        firebaseAuth: FirebaseAuth,
        sharedPreferencesManager: SharedPreferencesManager
    ): ChatRepository {
        return ChatRepository(
            apiService,
            utils,
            firebaseAuth,
            sharedPreferencesManager
        )
    }

    @Singleton
    @Provides
    fun provideGoogleAuthRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth
    ): GoogleAuthRepository {
        return GoogleAuthRepository(
            context,
            firebaseAuth
        )
    }

    @Singleton
    @Provides
    fun provideUtils(): Utils {
        return Utils()
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideSharedPreferencesManager(
        @ApplicationContext context: Context
    ): SharedPreferencesManager {
        return SharedPreferencesManager(context)
    }
}