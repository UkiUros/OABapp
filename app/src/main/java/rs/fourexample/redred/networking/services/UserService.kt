package rs.fourexample.redred.networking.services

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import rs.fourexample.redred.networking.FirebaseRoutes
import rs.fourexample.redred.networking.entities.UserEntity
import rs.fourexample.redred.networking.extensions.FlowDataState
import rs.fourexample.redred.networking.extensions.awaitSingleValueEventItem
import timber.log.Timber
import javax.inject.Inject

class UserService
@Inject
constructor(
    private val firebaseRoutes: FirebaseRoutes
) {

    @ExperimentalCoroutinesApi
    suspend fun getUser(userId: String): Flow<FlowDataState<UserEntity?>> {
        return firebaseRoutes.getUsersReference()
            .orderByKey()
            .equalTo(userId)
            .limitToFirst(1)
            .awaitSingleValueEventItem()
    }

    suspend fun createUser(user: UserEntity): Boolean {
        return try {
            var isSuccessful = false
            firebaseRoutes.getUsersReference()
                .child(user.userId)
                .setValue(user)
                .addOnCompleteListener {
                    isSuccessful = it.isSuccessful
                }
                .await()

            isSuccessful
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun updateUser(user: UserEntity): Boolean {
        return try {
            var isSuccessful = false
            firebaseRoutes.getUsersReference()
                .child(user.userId)
                .setValue(user)
                .addOnCompleteListener {
                    isSuccessful = it.isSuccessful
                }
                .await()

            isSuccessful
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun addBloodRequestReference(userId: String, requestId: String): Boolean {
        return try {
            var isSuccessful = false
            firebaseRoutes.getUsersReference()
                .child(userId)
                .child("bloodRequests")
                .child(requestId)
                .setValue(true)
                .addOnCompleteListener {
                    isSuccessful = it.isSuccessful
                }
                .await()

            isSuccessful
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun addNotificationToken(userId: String, notificationToken: String): Boolean {
        return try {
            var isSuccessful = false
            firebaseRoutes.getNotificationTokensReference()
                .child(userId)
                .child(notificationToken)
                .setValue(true)
                .addOnCompleteListener {
                    isSuccessful = it.isSuccessful
                }
                .await()

            isSuccessful
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun removeNotificationToken(userId: String, notificationToken: String): Boolean {
        return try {
            var isSuccessful = false
            firebaseRoutes.getNotificationTokensReference()
                .child(userId)
                .child(notificationToken)
                .removeValue()
                .addOnCompleteListener {
                    isSuccessful = it.isSuccessful
                }
                .await()

            isSuccessful
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun subscribeToBloodRequests(
        countryCode: String,
        bloodType: String,
        userId: String
    ): Boolean {
        return try {
            var isSuccessful = false
            firebaseRoutes.getBloodRequestSubscriptionsReference(countryCode)
                .child(bloodType)
                .child(userId)
                .setValue(true)
                .addOnCompleteListener {
                    isSuccessful = it.isSuccessful
                }
                .await()

            isSuccessful
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    suspend fun unsubscribeForBloodRequests(
        countryCode: String,
        bloodType: String,
        userId: String
    ): Boolean {
        return try {
            var isSuccessful = false
            firebaseRoutes.getBloodRequestSubscriptionsReference(countryCode)
                .child(bloodType)
                .child(userId)
                .removeValue()
                .addOnCompleteListener {
                    isSuccessful = it.isSuccessful
                }
                .await()

            Timber.d("RealtimeDatabase removeSubscriptionsForBloodRequests returning result = $isSuccessful")
            isSuccessful
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

}
