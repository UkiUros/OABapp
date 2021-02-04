package rs.fourexample.redred.ui

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.repository.BloodRequestRepository
import rs.fourexample.redred.utils.DataState

@ExperimentalCoroutinesApi
class BloodRequestDetailsViewModel
@ViewModelInject
constructor(
    private val requestRepository: BloodRequestRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val requestDataState: LiveData<DataState<BloodRequest?>>
        get() = mutableRequestDataState

    private val mutableRequestDataState: MutableLiveData<DataState<BloodRequest?>> =
        MutableLiveData()

    fun fetchRequest(countryCode: String, requestId: String) {
        viewModelScope.launch {
            requestRepository.getBloodRequest(countryCode, requestId)
                .onEach {
                    mutableRequestDataState.value = it
                }
                .launchIn(viewModelScope)
        }
    }
}
