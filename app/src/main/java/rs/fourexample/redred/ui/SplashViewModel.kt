package rs.fourexample.redred.ui

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.fourexample.redred.data.CurrentSession
import rs.fourexample.redred.data.User
import rs.fourexample.redred.repository.UserRepository
import rs.fourexample.redred.utils.DataState

@ExperimentalCoroutinesApi
class SplashViewModel
@ViewModelInject
constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val session: CurrentSession,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val mutableDataState: MutableLiveData<DataState<User?>> = MutableLiveData()
    private val mutableFirebaseUserState: MutableLiveData<FirebaseUser?> = MutableLiveData()

    val dataState: LiveData<DataState<User?>>
        get() = mutableDataState

    val firebaseUserState: LiveData<FirebaseUser?>
        get() = mutableFirebaseUserState

    val currentSession: CurrentSession = session

    fun fetchUser(userId: String) {
        viewModelScope.launch {

            userRepository.getUser(userId)
                .onEach {
                    mutableDataState.value = it
                }.launchIn(viewModelScope)

        }
    }

    fun checkFirebaseUserSession() {
        firebaseAuth.addAuthStateListener { firebaseAuthInstance ->
            mutableFirebaseUserState.value = firebaseAuthInstance.currentUser
        }
    }

    fun createUser(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            userRepository.createUser(firebaseUser)
                .onEach {
                    mutableDataState.value = it
                }.launchIn(viewModelScope)

        }
    }

}
