package rs.fourexample.redred.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.fourexample.redred.networking.FirebaseRoutes
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Singleton
    @Provides
    fun provideFirebaseDatabaseReference(): FirebaseDatabase {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.setLogLevel(Logger.Level.DEBUG)
        return firebaseDatabase
    }

    @Singleton
    @Provides
    fun provideFirebaseRoutes(firebaseDatabase: FirebaseDatabase): FirebaseRoutes {
        return FirebaseRoutes.getInstance(firebaseDatabase)
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

}
