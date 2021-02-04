package rs.fourexample.redred.ui

import android.annotation.SuppressLint
import android.location.Location
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.fourexample.redred.data.CurrentSession
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.data.BloodType
import rs.fourexample.redred.data.User
import rs.fourexample.redred.repository.BloodRequestRepository
import rs.fourexample.redred.repository.UserRepository
import rs.fourexample.redred.utils.DataState
import timber.log.Timber

@ExperimentalCoroutinesApi
class MainViewModel
@ViewModelInject
constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val requestRepository: BloodRequestRepository,
    private val userRepository: UserRepository,
    private val currentSession: CurrentSession,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val locationLiveData: LiveData<Location?>
        get() = mutableLocationData

    val firebaseAuthState: LiveData<FirebaseAuth>
        get() = mutableFirebaseAuthState

    val activeRequestsLiveData: LiveData<DataState<List<BloodRequest>?>>
        get() = mutableActiveRequestsDataState

    val bloodTypeRequestsLiveDataState: LiveData<DataState<List<BloodRequest>?>>
        get() = mutableBloodTypeRequestsDataState

    val subscriptionDataState: LiveData<DataState<BloodType?>>
        get() = mutableSubscriptionDataState

    val userLiveData: LiveData<User>
        get() = mutableUserData

    val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData()
    val mutableCountryCodeLiveData: MutableLiveData<String> = MutableLiveData()


    private val mutableLocationData: MutableLiveData<Location?> = MutableLiveData()
    private val mutableFirebaseAuthState: MutableLiveData<FirebaseAuth> = MutableLiveData()
    private val mutableBloodTypeRequestsDataState: MutableLiveData<DataState<List<BloodRequest>?>> =
        MutableLiveData()
    private val mutableActiveRequestsDataState: MutableLiveData<DataState<List<BloodRequest>?>> =
        MutableLiveData()
    private val mutableSubscriptionDataState: MutableLiveData<DataState<BloodType?>> =
        MutableLiveData()
    private val mutableUserData: MutableLiveData<User> = MutableLiveData()


    init {
        checkFirebaseAuthSession()
    }

    fun getCurrentSession(): CurrentSession {
        return currentSession
    }

    fun getCurrentUser(): User? {
        return currentSession.user
    }

    fun getCurrentLocation(): Location? {
        return currentSession.location
    }

    fun isAnonymousUser(): Boolean {
        return firebaseAuth.currentUser?.isAnonymous == true
    }

    fun isUserSubscribedForNotifications(): Boolean {
        return currentSession.user?.notificationBloodType != null
    }

    fun signOut() {
        val currentUserId = currentSession.user?.userId
        val currentNotificationToken = currentSession.notificationToken
        if (currentUserId != null && currentNotificationToken != null) {
            removeNotificationToken(currentUserId, currentNotificationToken)
        } else {
            firebaseAuth.signOut()
            clearCurrentSessionData()
        }
    }

    private fun clearCurrentSessionData() {
        currentSession.user = null
        currentSession.countryCode = null
        currentSession.location = null
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        mutableLoadingState.value = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                mutableLocationData.value = it
                mutableLoadingState.value = false
            }
            .addOnFailureListener {
                mutableLocationData.value = null
                mutableLoadingState.value = false
            }
    }

    fun checkFirebaseAuthSession() {
        mutableLoadingState.value = true
        firebaseAuth.addAuthStateListener { firebaseAuthInstance ->
            mutableFirebaseAuthState.value = firebaseAuthInstance
            mutableLoadingState.value = false
        }
    }

    fun fetchRequestsForCountry(countryCode: String) {
        viewModelScope.launch {
            requestRepository.getActiveBloodRequests(countryCode)
                .onEach {
                    mutableActiveRequestsDataState.value = it
                }
                .launchIn(viewModelScope)

        }
    }

    fun fetchRequestsForBloodType(countryCode: String, bloodType: BloodType) {
        viewModelScope.launch {
            requestRepository.getBloodRequestsForType(countryCode, bloodType)
                .onEach {
                    mutableBloodTypeRequestsDataState.value = it
                }
                .launchIn(viewModelScope)

        }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
                .onEach {
                    when (it) {
                        is DataState.Success -> {
                            currentSession.user = it.data
                            mutableUserData.value = it.data
                            mutableLoadingState.value = false
                        }
                        DataState.Loading -> mutableLoadingState.value = true
                        is DataState.Error -> mutableLoadingState.value = false
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun checkNotificationToken() {
        val userId: String = currentSession.user?.userId ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e(task.exception)
                return@addOnCompleteListener
            }

            updateNotificationToken(userId, task.result)
        }
    }

    private fun updateNotificationToken(userId: String, token: String) = viewModelScope.launch {
        userRepository.addNotificationToken(userId, token).launchIn(viewModelScope)
    }

    private fun removeNotificationToken(userId: String, token: String) = viewModelScope.launch {
        userRepository.removeNotificationToken(userId, token)
            .onEach {
                when (it) {
                    is DataState.Success,
                    is DataState.Error -> {
                        firebaseAuth.signOut()
                        clearCurrentSessionData()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun subscribeToBloodRequestNotifications(bloodType: BloodType) {
        val user: User = currentSession.user ?: return
        val countryCode: String = currentSession.countryCode ?: return

        viewModelScope.launch {
            userRepository.subscribeToBloodRequests(countryCode, bloodType, user.userId)
                .onEach {
                    if (it is DataState.Success) {
                        currentSession.user?.notificationBloodType = it.data
                        currentSession.user?.notificationCountryCode = if (it.data == null) {
                            ""
                        } else {
                            currentSession.countryCode
                        }
                    }
                    mutableSubscriptionDataState.value = it
                }
                .launchIn(viewModelScope)
        }
    }

    fun unsubscribeFromBloodRequestNotifications(bloodType: BloodType) {
        val user: User = currentSession.user ?: return
        val countryCode: String = user.notificationCountryCode ?: return

        viewModelScope.launch {
            userRepository.unsubscribeForBloodRequests(countryCode, bloodType, user.userId)
                .onEach {
                    if (it is DataState.Success) {
                        currentSession.user?.notificationBloodType = null
                        currentSession.user?.notificationCountryCode = ""
                    }
                    mutableSubscriptionDataState.value = it
                }
                .launchIn(viewModelScope)
        }
    }

}
