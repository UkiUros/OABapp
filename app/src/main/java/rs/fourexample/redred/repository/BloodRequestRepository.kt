package rs.fourexample.redred.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.data.BloodType
import rs.fourexample.redred.networking.BloodRequestNetworkMapper
import rs.fourexample.redred.networking.extensions.FlowDataState
import rs.fourexample.redred.networking.services.BloodRequestService
import rs.fourexample.redred.utils.DataState

@ExperimentalCoroutinesApi
class BloodRequestRepository constructor(
    private val mapper: BloodRequestNetworkMapper,
    private val service: BloodRequestService
) {

    suspend fun getBloodRequest(
        countryCode: String,
        requestId: String
    ): Flow<DataState<BloodRequest?>> = flow {
        emit(DataState.Loading)

        // calling the suspend function
        service.getBloodRequest(countryCode, requestId)
            .collect {
                when (it) {
                    is FlowDataState.Success -> {
                        if (it.data == null) {
                            emit(DataState.Success(null))
                        } else {
                            emit(DataState.Success(mapper.mapFromEntity(it.data)))
                        }

                    }
                    is FlowDataState.Error -> {
                        emit(DataState.Error(it.exception))
                    }
                }
            }
    }

    suspend fun getActiveBloodRequests(
        countryCode: String
    ): Flow<DataState<List<BloodRequest>?>> = flow {
        emit(DataState.Loading)

        // calling the suspend function
        service.getActiveBloodRequests(countryCode).collect {
            when (it) {
                is FlowDataState.Success -> {
                    emit(DataState.Success(mapper.mapFromEntityList(it.data)))
                }
                is FlowDataState.Error -> {
                    emit(DataState.Error(it.exception))
                }
            }
        }
    }

    suspend fun getBloodRequestsForType(
        countryCode: String,
        bloodType: BloodType
    ): Flow<DataState<List<BloodRequest>?>> = flow {
        emit(DataState.Loading)
        // calling the suspend function
        service.getBloodRequestsForType(countryCode, mapper.getBloodTypeAsText(bloodType))
            .collect {
                when (it) {
                    is FlowDataState.Success -> {
                        emit(DataState.Success(mapper.mapFromEntityList(it.data)))
                    }
                    is FlowDataState.Error -> {
                        emit(DataState.Error(it.exception))
                    }
                }
            }
    }

    suspend fun getMyBloodRequests(
        userId: String,
        countryCode: String
    ): Flow<DataState<List<BloodRequest>?>> = flow {
        emit(DataState.Loading)

        // calling the suspend function
        service.getMyBloodRequests(userId, countryCode)
            .collect {
                when (it) {
                    is FlowDataState.Success -> {
                        emit(DataState.Success(mapper.mapFromEntityList(it.data)))
                    }
                    is FlowDataState.Error -> {
                        emit(DataState.Error(it.exception))
                    }
                }
            }
    }

    suspend fun createBloodRequest(bloodRequest: BloodRequest): Flow<DataState<String>> = flow {
        emit(DataState.Loading)

        try {
            // suspend function call
            val result = service.postBloodRequest(mapper.mapToEntity(bloodRequest))
            if (result != null) {
                emit(DataState.Success(result))
            } else {
                emit(DataState.Error(IllegalStateException("requestId is null")))
            }

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun updateBloodRequest(bloodRequest: BloodRequest): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading)

        try {
            // suspend function call
            val result = service.updateBloodRequest(mapper.mapToEntity(bloodRequest))
            emit(DataState.Success(result))

        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }
}
