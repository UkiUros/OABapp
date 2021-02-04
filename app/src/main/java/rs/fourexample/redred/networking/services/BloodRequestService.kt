package rs.fourexample.redred.networking.services

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import rs.fourexample.redred.networking.FirebaseRoutes
import rs.fourexample.redred.networking.entities.BloodRequestEntity
import rs.fourexample.redred.networking.extensions.FlowDataState
import rs.fourexample.redred.networking.extensions.awaitSingleValueEventItem
import rs.fourexample.redred.networking.extensions.awaitSingleValueEventList
import timber.log.Timber
import javax.inject.Inject


@ExperimentalCoroutinesApi
class BloodRequestService
@Inject
constructor(
    private val firebaseRoutes: FirebaseRoutes
) {

    suspend fun getBloodRequest(
        countryCode: String,
        requestId: String
    ): Flow<FlowDataState<BloodRequestEntity?>> {
        return firebaseRoutes.getBloodRequestsReference(countryCode)
            .orderByKey()
            .equalTo(requestId)
            .limitToFirst(1)
            .awaitSingleValueEventItem()
    }

    suspend fun getActiveBloodRequests(countryCode: String): Flow<FlowDataState<List<BloodRequestEntity>>> {
        return firebaseRoutes.getBloodRequestsReference(countryCode)
            .orderByChild("isActive").equalTo(true)
            .limitToFirst(20)
            .awaitSingleValueEventList()
    }


    suspend fun getBloodRequestsForType(
        countryCode: String,
        bloodType: String
    ): Flow<FlowDataState<List<BloodRequestEntity>>> {
        return firebaseRoutes.getBloodRequestsReference(countryCode)
            .orderByChild("bloodType").equalTo(bloodType)
            .limitToFirst(20)
            .awaitSingleValueEventList()
    }


    suspend fun getMyBloodRequests(
        userId: String,
        countryCode: String
    ): Flow<FlowDataState<List<BloodRequestEntity>>> {
        return firebaseRoutes.getBloodRequestsReference(countryCode)
            .orderByChild("userId").equalTo(userId)
            .limitToFirst(20)
            .awaitSingleValueEventList()
    }

    suspend fun postBloodRequest(
        request: BloodRequestEntity
    ): String? { // must return requestID in order to update other references depending on this key
        return try {

            val countryReference = firebaseRoutes.getBloodRequestsReference(request.countryCode!!)
            var generatedRequestKey = countryReference.push().key
            request.requestId = generatedRequestKey

            if (generatedRequestKey != null) {
                countryReference
                    .child(generatedRequestKey)
                    .setValue(request)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) {
                            generatedRequestKey = null
                        }
                    }
                    .await()
            }

            generatedRequestKey
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun updateBloodRequest(
        request: BloodRequestEntity
    ): Boolean {
        return try {
            var isSuccessful = false
            val requestKey = request.requestId

            if (requestKey != null) {
                firebaseRoutes.getBloodRequestsReference(request.countryCode!!)
                    .child(requestKey)
                    .setValue(request)
                    .addOnCompleteListener {
                        isSuccessful = it.isSuccessful
                    }
                    .await()
            }

            isSuccessful
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }
}
