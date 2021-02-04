package rs.fourexample.redred.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.networking.FirebaseRoutes
import rs.fourexample.redred.networking.services.BloodRequestService
import rs.fourexample.redred.networking.services.UserService
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun provideUserService(firebaseRoutes: FirebaseRoutes): UserService {
        return UserService(firebaseRoutes)
    }

    @Singleton
    @Provides
    fun provideBloodRequestService(firebaseRoutes: FirebaseRoutes): BloodRequestService {
        return BloodRequestService(firebaseRoutes)
    }

}
