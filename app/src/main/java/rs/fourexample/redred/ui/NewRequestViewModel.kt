package rs.fourexample.redred.ui

import android.location.Location
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.data.CurrentSession
import rs.fourexample.redred.data.User
import rs.fourexample.redred.repository.BloodRequestRepository
import rs.fourexample.redred.repository.UserRepository
import rs.fourexample.redred.utils.DataState


@ExperimentalCoroutinesApi
class NewRequestViewModel
@ViewModelInject
constructor(
    private val firebaseAuth: FirebaseAuth,
    private val requestRepository: BloodRequestRepository,
    private val userRepository: UserRepository,
    private val session: CurrentSession,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mutableCreateBloodRequestState = MutableLiveData<DataState<String>>()

    val createBloodRequestState: LiveData<DataState<String>>
        get() = mutableCreateBloodRequestState

    val isAnonymousUser: Boolean = firebaseAuth.currentUser?.isAnonymous == true

    val currentUser: User? = session.user
    val currentLocation: Location? = session.location
    val currentCountryCode: String? = session.countryCode


    fun createBloodRequest(bloodRequest: BloodRequest) {
        viewModelScope.launch {
            requestRepository.createBloodRequest(bloodRequest)
                .onEach {
                    mutableCreateBloodRequestState.value = it
                }
                .launchIn(viewModelScope)
        }
    }
}
