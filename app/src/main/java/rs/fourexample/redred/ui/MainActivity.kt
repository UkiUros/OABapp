package rs.fourexample.redred.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.R
import rs.fourexample.redred.databinding.ActivityMainBinding
import rs.fourexample.redred.helper.extractCountryCodeFromLocation
import rs.fourexample.redred.ui.fragments.ListRequestsFragment
import rs.fourexample.redred.ui.fragments.NotificationRequestsFragment
import rs.fourexample.redred.ui.fragments.ProfileFragment
import rs.fourexample.redred.ui.fragments.SettingsFragment

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private val activeRequestsFragment = ListRequestsFragment.newInstance()
    private val notificationRequestsFragment = NotificationRequestsFragment.newInstance()
    private val profileFragment = ProfileFragment.newInstance()
    private val settingsFragment = SettingsFragment.newInstance()
    private var activeFragment: Fragment = activeRequestsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.elevation = 0f


        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, profileFragment, getString(R.string.Navigation_Home_Profile)).hide(profileFragment)
            add(R.id.fragmentContainer, settingsFragment, getString(R.string.Navigation_Home_Settings)).hide(settingsFragment)
            add(R.id.fragmentContainer, notificationRequestsFragment, getString(R.string.Navigation_Home_BloodTypeRequests)).hide(notificationRequestsFragment)
            add(R.id.fragmentContainer, activeRequestsFragment, getString(R.string.Navigation_Home_Requests))
        }.commit()

        settUpBottomNav()

        subscribeToObservables()
        fetchLocation()
        viewModel.checkNotificationToken()
    }

    private fun settUpBottomNav() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigationList -> {
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(activeRequestsFragment)
                        .commit()
                    activeFragment = activeRequestsFragment
                    setTitle(R.string.app_name)
                    true
                }
                R.id.navigationBloodTypeList -> {
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(notificationRequestsFragment)
                        .commit()
                    activeFragment = notificationRequestsFragment
                    setTitle(R.string.Navigation_Home_BloodTypeRequests)
                    true
                }
                R.id.navigationProfile -> {
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(profileFragment)
                        .commit()
                    activeFragment = profileFragment
                    setTitle(R.string.Navigation_Home_Profile)
                    true
                }
                R.id.navigationSettings -> {
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(settingsFragment)
                        .commit()
                    activeFragment = settingsFragment
                    setTitle(R.string.Navigation_Home_Settings)
                    true
                }
                else -> false
            }
        }
    }

    private fun subscribeToObservables() {
        viewModel.locationLiveData.observe(this, {
            if (it == null) {
                showCountryPicker()
            } else {
                viewModel.mutableCountryCodeLiveData.value = extractCountryCodeFromLocation(this, it)
                viewModel.getCurrentSession().location = it
            }
        })

        viewModel.mutableLoadingState.observe(this, {
            if (it) {
                //show progress
            } else {
                // hide progress
            }
        })

        viewModel.firebaseAuthState.observe(this, {
            if (it.currentUser == null) {
                userSignedOut()
            }
        })
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSIONS_REQUEST
            )
            return
        }

        viewModel.fetchLocation()
    }

    private fun showCountryPicker() {
        val items = arrayOf<CharSequence>(
            getString(R.string.Dialog_CountryPicker_Item_Serbia),
            getString(R.string.Dialog_CountryPicker_Item_Bosnia))
        val supportedCountryCodes = arrayOf("rs", "ba")

        MaterialAlertDialogBuilder(this).apply {
            setCancelable(false)
            setTitle(getString(R.string.Dialog_CountryPicker_Title))
            setSingleChoiceItems(items, -1) { dialog, item ->
                dialog.dismiss()
                viewModel.mutableCountryCodeLiveData.value = supportedCountryCodes[item]
            }
        }.create().show()
    }

    private fun userSignedOut() {
        finish()
        SplashActivity.start(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSIONS_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation()
                } else {
                    showCountryPicker()
                }
            }
        }
    }

    companion object {

        const val LOCATION_PERMISSIONS_REQUEST: Int = 100

        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
