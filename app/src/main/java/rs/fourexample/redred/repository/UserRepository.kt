package rs.fourexample.redred.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import rs.fourexample.redred.data.CurrentSession
import rs.fourexample.redred.data.BloodType
import rs.fourexample.redred.data.User
import rs.fourexample.redred.networking.UserNetworkMapper
import rs.fourexample.redred.networking.entities.UserEntity
import rs.fourexample.redred.networking.extensions.FlowDataState
import rs.fourexample.redred.networking.services.UserService
import rs.fourexample.redred.utils.DataState

class UserRepository constructor(
    private val userNetworkMapper: UserNetworkMapper,
    private val userService: UserService,
    private val currentSession: CurrentSession
) {
    @ExperimentalCoroutinesApi
    suspend fun getUser(userId: String): Flow<DataState<User?>> = flow {
        emit(DataState.Loading)
        // calling the suspend function
        userService.getUser(userId)
            .collect {
                when (it) {
                    is FlowDataState.Success -> {
                        if (it.data == null) {
                            emit(DataState.Success(null))
                        } else {
                            emit(DataState.Success(userNetworkMapper.mapFromEntity(it.data)))
                        }
                    }
                    is FlowDataState.Error -> {
                        emit(DataState.Error(it.exception))
                    }
                }
            }
    }

    suspend fun createUser(firebaseUser: FirebaseUser): Flow<DataState<User>> = flow {
        emit(DataState.Loading)

        try {
            val userEntity = UserEntity(
                userId = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                phoneNumber = firebaseUser.phoneNumber ?: "",
                photoUrl = if (firebaseUser.photoUrl != null) firebaseUser.photoUrl.toString() else "",
                notificationBloodType = ""
            )

            // suspend function call
            val result = userService.createUser(userEntity)

            if (result) {
                emit(DataState.Success(userNetworkMapper.mapFromEntity(userEntity)))
            } else {
                throw Exception("failed to create user")
            }

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun updateUser(user: User): Flow<DataState<User>> = flow {
        emit(DataState.Loading)

        try {
            // suspend function call
            val result = userService.updateUser(userNetworkMapper.mapToEntity(user))

            if (result) {
                emit(DataState.Success(user))
            } else {
                throw Exception("failed to update user")
            }

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }

    }

    suspend fun addBloodRequestReference(
        userId: String,
        requestId: String
    ): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading)
        try {
            // suspend function call
            val result = userService.addBloodRequestReference(userId, requestId)
            emit(DataState.Success(result))

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun addNotificationToken(
        userId: String,
        notificationToken: String
    ): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading)
        try {

            // suspend function call
            val result = userService.addNotificationToken(userId, notificationToken)

            if (result) {
                currentSession.notificationToken = notificationToken
            }

            emit(DataState.Success(result))

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun removeNotificationToken(
        userId: String,
        notificationToken: String
    ): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading)
        try {

            // suspend function call
            val result = userService.removeNotificationToken(userId, notificationToken)
            emit(DataState.Success(result))

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun subscribeToBloodRequests(
        countryCode: String,
        bloodType: BloodType,
        userId: String
    ): Flow<DataState<BloodType?>> = flow {
        emit(DataState.Loading)
        try {

            // suspend function call
            val result = userService.subscribeToBloodRequests(
                countryCode,
                userNetworkMapper.getNotificationBloodTypeAsText(bloodType)!!,
                userId
            )

            val returningBloodType = if (result) {
                bloodType
            } else {
                null
            }

            emit(DataState.Success(returningBloodType))

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun unsubscribeForBloodRequests(
        countryCode: String,
        bloodType: BloodType,
        userId: String
    ): Flow<DataState<BloodType?>> = flow {
        emit(DataState.Loading)
        try {
            // suspend function call
            val result = userService.unsubscribeForBloodRequests(
                countryCode,
                userNetworkMapper.getNotificationBloodTypeAsText(bloodType)!!,
                userId
            )

            val returningBloodType = if (result) {
                null
            } else {
                bloodType
            }

            emit(DataState.Success(returningBloodType))

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

}
