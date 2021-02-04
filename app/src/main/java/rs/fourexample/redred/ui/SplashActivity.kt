package rs.fourexample.redred.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.R
import rs.fourexample.redred.constants.URL_PRIVACY_POLICY
import rs.fourexample.redred.constants.URL_TERMS_CONDITIONS
import rs.fourexample.redred.data.User
import rs.fourexample.redred.databinding.ActivitySplashBinding
import rs.fourexample.redred.utils.DataState
import timber.log.Timber

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()
    private lateinit var binding: ActivitySplashBinding
    private var currentFirebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservables()
        viewModel.checkFirebaseUserSession()
    }

    private fun subscribeToObservables() {
        viewModel.dataState.observe(this, { dataState ->
            when (dataState) {
                is DataState.Success -> {
                    val userData = dataState.data
                    if (userData == null) { // new user, does not exist in db
                        // save to db
                        saveUserToServer()
                    } else {
                        onLoginSuccess(userData)
                    }
                }
                is DataState.Error -> {
                    displayFetchUserError()
                }
                DataState.Loading -> {
                    if (binding.lottieLoadingView.visibility != View.VISIBLE) {
                        binding.lottieLoadingView.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.firebaseUserState.observe(this, {
            currentFirebaseUser = it
            if (it == null) {
                Timber.d("FirebaseAuth no session, must log in manually")
                fireSignInIntent()
            } else {
                Timber.d("FirebaseAuth authenticated")
                if (it.isAnonymous) {
                    onLoginSuccess(null) // continue using app as anon user
                } else {
                    viewModel.fetchUser(it.uid)
                }
            }
        })
    }

    private fun onLoginSuccess(user: User?) {
        viewModel.currentSession.user = user
        MainActivity.start(this)
        finish()
    }

    private fun saveUserToServer() {
        currentFirebaseUser?.let {
            viewModel.createUser(it)
        }
    }

    private fun fireSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build()
        )
        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAlwaysShowSignInMethodScreen(true)
                .setTheme(R.style.AuthTheme)
                .setLogo(R.mipmap.ic_launcher_round)
                .setTosAndPrivacyPolicyUrls(
                    URL_TERMS_CONDITIONS,
                    URL_PRIVACY_POLICY
                )
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                currentFirebaseUser = FirebaseAuth.getInstance().currentUser

                // not need, handled in viewModel.firebaseUserState.observe
//                viewModel.fetchUser(currentFirebaseUser!!.uid)
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Timber.e("Sign in failed. Response = $response")
                Toast.makeText(this, "Sign in cancelled.", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun displayError() {
        Toast.makeText(
            applicationContext,
            "Ooops, some error occured!",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayFetchUserError() {
        if (currentFirebaseUser == null) {
            displayError()
            finish()
            return
        }

        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.ServerError_Dialog_Title))
            setMessage(getString(R.string.ServerError_Dialog_Message))
            setPositiveButton(R.string.Dialog_Button_Yes) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                currentFirebaseUser?.let {
                    viewModel.fetchUser(it.uid)
                }
            }
            setNegativeButton(R.string.Dialog_Button_No) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
        }.create().show()
    }

    companion object {

        const val RC_SIGN_IN = 105

        fun start(context: Context) {
            context.startActivity(Intent(context, SplashActivity::class.java))
        }
    }
}
