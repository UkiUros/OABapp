package rs.fourexample.redred.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.fourexample.redred.data.CurrentSession
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionProvider {

    @Singleton
    @Provides
    fun provideCurrentSession(): CurrentSession {
        return CurrentSession.getInstance()
    }

}
