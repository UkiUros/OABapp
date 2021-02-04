package rs.fourexample.redred.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.R
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.databinding.ActivityBloodRequestDetailsBinding
import rs.fourexample.redred.ui.fragments.RequestDetailMapFragment
import rs.fourexample.redred.utils.DataState

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class BloodRequestDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBloodRequestDetailsBinding
    private var countryCode: String? = null
    private var requestId: String? = null

    private val viewModel: BloodRequestDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBloodRequestDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""
        if (intent.hasExtra(EXTRA_REQUEST_ID)) {
            subscribeToObservables()
            countryCode = intent.getStringExtra(EXTRA_REQUEST_COUNTRY_CODE)
            requestId = intent.getStringExtra(EXTRA_REQUEST_ID)

            viewModel.fetchRequest(countryCode!!, requestId!!)

        } else {
            val bloodRequest = intent.getParcelableExtra(EXTRA_REQUEST) as BloodRequest? ?: return
            populateFields(bloodRequest)
        }
    }

    private fun subscribeToObservables(){
        viewModel.requestDataState.observe(this, {
            when (it) {
                is DataState.Success -> {
                    binding.includedLayout.progressBar.visibility = View.GONE
                    if (it.data != null) {
                        populateFields(it.data)
                    } else {
                        finish()
                    }
                }
                is DataState.Error -> {
                    binding.includedLayout.progressBar.visibility = View.GONE
                    showServerErrorDialog()

                }
                DataState.Loading -> binding.includedLayout.progressBar.visibility = View.VISIBLE
            }
        })
    }

    private fun populateFields(bloodRequest: BloodRequest) {
        setMapFragment(bloodRequest.latitude, bloodRequest.longitude)
        binding.textViewBloodType.text = bloodRequest.bloodType?.typeValue ?: ""
        binding.includedLayout.textViewName.text = bloodRequest.userName
        binding.includedLayout.textViewDetails.text = bloodRequest.description
        binding.includedLayout.textViewPhone.text = bloodRequest.contactPhone
        binding.includedLayout.textViewAddress.text = bloodRequest.address

        val textStatus = if (bloodRequest.isActive) {
            getString(R.string.RequestDetails_Label_Status_Active)
            binding.includedLayout.textViewStatus.visibility = View.GONE
        } else {
            getString(R.string.RequestDetails_Label_Status_Completed)
            binding.includedLayout.textViewStatus.visibility = View.VISIBLE
        }

        binding.includedLayout.textViewStatus.text = getString(R.string.RequestDetails_Label_Status, textStatus)

        binding.includedLayout.textViewPhone.setOnClickListener { showCallDialog(bloodRequest.contactPhone) }
    }

    private fun setMapFragment(latitude: Double, longitude: Double) {
        val location = Location("")
        location.latitude = latitude
        location.longitude = longitude
        val mapFragment = RequestDetailMapFragment.newInstance(location)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, mapFragment)
            .commit()
    }

    private fun showCallDialog(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }

    }

    private fun showServerErrorDialog() {
        if (countryCode == null || requestId == null) {
            finish()
            return
        }
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.ServerError_Dialog_Title))
            setMessage(getString(R.string.ServerError_Dialog_Message))
            setPositiveButton(R.string.Dialog_Button_Yes) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                viewModel.fetchRequest(countryCode!!, requestId!!)
            }
            setNegativeButton(R.string.Dialog_Button_No) { dialog, _ ->
                dialog.dismiss()
            }
        }.create().show()
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
        const val EXTRA_REQUEST_COUNTRY_CODE = "request_country_code"
        const val EXTRA_REQUEST_ID = "request_id"
        const val EXTRA_REQUEST = "extra_request"

        fun start(context: Context, bloodRequest: BloodRequest) {
            context.startActivity(Intent(context, BloodRequestDetailsActivity::class.java).apply {
                putExtra(EXTRA_REQUEST, bloodRequest)
            })
        }
    }
}
