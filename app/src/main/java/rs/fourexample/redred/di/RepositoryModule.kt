package rs.fourexample.redred.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.data.CurrentSession
import rs.fourexample.redred.networking.BloodRequestNetworkMapper
import rs.fourexample.redred.networking.UserNetworkMapper
import rs.fourexample.redred.networking.services.BloodRequestService
import rs.fourexample.redred.networking.services.UserService
import rs.fourexample.redred.repository.BloodRequestRepository
import rs.fourexample.redred.repository.UserRepository
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(
        userNetworkMapper: UserNetworkMapper,
        service: UserService,
        currentSession: CurrentSession
    ): UserRepository {
        return UserRepository(userNetworkMapper, service, currentSession)
    }

    @Singleton
    @Provides
    fun provideBloodRequestRepository(
        bloodRequestNetworkMapper: BloodRequestNetworkMapper,
        service: BloodRequestService
    ): BloodRequestRepository {
        return BloodRequestRepository(bloodRequestNetworkMapper, service)
    }
}
