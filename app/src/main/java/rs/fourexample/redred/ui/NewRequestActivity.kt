package rs.fourexample.redred.ui

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.R
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.data.BloodType
import rs.fourexample.redred.databinding.ActivityNewRequestBinding
import rs.fourexample.redred.helper.extractCountryCodeFromLocation
import rs.fourexample.redred.helper.extractStreetNameFromLocation
import rs.fourexample.redred.ui.fragments.LocationPickerMapFragment
import rs.fourexample.redred.utils.DataState

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewRequestActivity : AppCompatActivity(),
    LocationPickerMapFragment.OnFragmentInteractionListener {

    private val viewModel: NewRequestViewModel by viewModels()
    private var selectedBloodType: BloodType? = null
    private var selectedLatLng: LatLng? = null

    private lateinit var binding: ActivityNewRequestBinding

    private var editBloodRequest: BloodRequest? = null

    private var isEditing = false

    // todo add button in layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.swipeRefreshLayout.isEnabled = false
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.chipGroupBloodType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipTypeOPositive -> selectedBloodType = BloodType.OPositive()
                R.id.chipTypeONegative -> selectedBloodType = BloodType.ONegative()
                R.id.chipTypeAPositive -> selectedBloodType = BloodType.APositive()
                R.id.chipTypeANegative -> selectedBloodType = BloodType.ANegative()
                R.id.chipTypeBPositive -> selectedBloodType = BloodType.BPositive()
                R.id.chipTypeBNegative -> selectedBloodType = BloodType.BNegative()
                R.id.chipTypeABPositive -> selectedBloodType = BloodType.ABPositive()
                R.id.chipTypeABNegative -> selectedBloodType = BloodType.ABNegative()
            }
        }

        if (viewModel.currentUser == null || viewModel.currentCountryCode == null) return
        setMapFragment()

        if (intent.hasExtra(EXTRA_REQUEST)) {
            editBloodRequest = intent.getParcelableExtra(EXTRA_REQUEST)
            isEditing = true
        }


        subscribeToObservables()

        if (isEditing) {
            selectedBloodType = editBloodRequest?.bloodType
            binding.chipGroupBloodType.isEnabled = false
            editBloodRequest?.let {
                binding.editTextAddress.setText(it.address)
                binding.editTextPhone.setText(it.contactPhone)
                binding.editTextDescription.setText(it.description)
                when (it.bloodType) {
                    is BloodType.OPositive -> binding.chipTypeOPositive.isChecked = true
                    is BloodType.ONegative -> binding.chipTypeONegative.isChecked = true
                    is BloodType.APositive -> binding.chipTypeAPositive.isChecked = true
                    is BloodType.ANegative -> binding.chipTypeANegative.isChecked = true
                    is BloodType.BPositive -> binding.chipTypeBPositive.isChecked = true
                    is BloodType.BNegative -> binding.chipTypeBNegative.isChecked = true
                    is BloodType.ABPositive -> binding.chipTypeABPositive.isChecked = true
                    is BloodType.ABNegative -> binding.chipTypeABNegative.isChecked = true
                    null -> throw IllegalStateException("wrong blood type")
                }
            }
        } else {
            binding.editTextPhone.setText(viewModel.currentUser?.phoneNumber)
            val tempStreetName = extractStreetNameFromLocation(this, getMarkerLocation())
            binding.editTextAddress.setText(tempStreetName)
            binding.editTextAddress.setSelection(tempStreetName.length)
        }

        binding.buttonDone.setOnClickListener {
            if (viewModel.isAnonymousUser) return@setOnClickListener
            validateFormAndSend()
        }
    }

    private fun setMapFragment() {
        val locationPickerMapFragment =
            LocationPickerMapFragment.newInstance(getMarkerLocation())
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, locationPickerMapFragment)
            .commit()
    }

    private fun getMarkerLocation(): Location? {
        return if (isEditing) {
            Location("").apply {
                latitude = editBloodRequest!!.latitude
                longitude = editBloodRequest!!.longitude
            }
        } else {
            viewModel.currentLocation
        }
    }

    private fun subscribeToObservables() {
        viewModel.createBloodRequestState.observe(this, {
            when (it) {
                is DataState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.buttonDone.isEnabled = true
                    clearTheForm()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                is DataState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                }
                DataState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                    binding.buttonDone.isEnabled = false
                }
            }
        })
    }

    private fun validateFormAndSend() {
        val address = binding.editTextAddress.text.toString()
        val phone = binding.editTextPhone.text.toString()
        val description = binding.editTextDescription.text.toString()

        if (viewModel.currentUser?.name.isNullOrEmpty()) {
            Toast.makeText(this, "Name in your profile cannot be empty!", Toast.LENGTH_LONG).show()
            return
        }

        if (selectedLatLng == null) return

        if (address.isNotEmpty() && phone.isNotEmpty() && description.isNotEmpty() && selectedBloodType != null) {
            if (isEditing) {
                editBloodRequest?.let {
                    it.address = address
                    it.contactPhone = phone
                    it.description
                    it.latitude = selectedLatLng!!.latitude
                    it.longitude = selectedLatLng!!.longitude
                    updateBloodRequest(it)
                }
            } else {
                val location = Location("")
                location.latitude = selectedLatLng!!.latitude
                location.longitude = selectedLatLng!!.longitude
                val extractedCountryCode = extractCountryCodeFromLocation(this, location)
                var tempCountryCode = viewModel.currentCountryCode
                if (tempCountryCode != extractedCountryCode) {
                    tempCountryCode = extractedCountryCode
                }

                createBloodRequest(
                    BloodRequest(
                        null,
                        viewModel.currentUser?.name!!,
                        viewModel.currentUser?.photoUrl,
                        userId = viewModel.currentUser!!.userId,
                        bloodType = selectedBloodType!!,
                        countryCode = tempCountryCode,
                        address = address,
                        contactPhone = phone,
                        description = description,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        System.currentTimeMillis(),
                        true
                    )
                )
            }
        }
    }

    private fun clearTheForm() {
        binding.editTextAddress.text.clear()
        binding.editTextPhone.text.clear()
        binding.editTextDescription.text.clear()
        binding.chipGroupBloodType.clearCheck()
    }

    private fun createBloodRequest(bloodRequest: BloodRequest) {
        viewModel.createBloodRequest(bloodRequest)
    }

    private fun updateBloodRequest(bloodRequest: BloodRequest) {
//        todo
//        viewModel.updateBloodRequest(bloodRequest)
    }

    override fun onLocationSelected(latLng: LatLng) {
        selectedLatLng = latLng
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_REQUEST = "blood_request"
        const val RQ_NEW_REQUEST = 101
        const val RC_EDIT_REQUEST = 108

        fun startForResult(activity: Activity) {
            val intent = Intent(activity, NewRequestActivity::class.java)
            activity.startActivityForResult(intent, RQ_NEW_REQUEST)
        }

        fun startForResult(activity: Activity, bloodRequest: BloodRequest) {
            val intent = Intent(activity, NewRequestActivity::class.java)
            intent.putExtra(EXTRA_REQUEST, bloodRequest)
            activity.startActivityForResult(intent, RC_EDIT_REQUEST)
        }
    }
}
