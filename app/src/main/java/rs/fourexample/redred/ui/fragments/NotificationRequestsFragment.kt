package rs.fourexample.redred.ui.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.list_requests_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.R
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.data.BloodType
import rs.fourexample.redred.databinding.NotificationRequestsFragmentBinding
import rs.fourexample.redred.ui.BloodRequestDetailsActivity
import rs.fourexample.redred.ui.MainViewModel
import rs.fourexample.redred.ui.adapters.RequestAdapter
import rs.fourexample.redred.utils.DataState

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotificationRequestsFragment : Fragment() {

    private var _binding: NotificationRequestsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: RequestAdapter

    var countryCode: String? = null

    companion object {
        fun newInstance() = NotificationRequestsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NotificationRequestsFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        adapter = RequestAdapter()
        adapter.callback = object : RequestAdapter.IRequestCallback {
            override fun onItemClick(bloodRequest: BloodRequest) {
                BloodRequestDetailsActivity.start(requireContext(), bloodRequest)
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchRequests()
        }

        binding.recyclerViewRequests.adapter = adapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservables()

        binding.includedLayoutNoData.textViewDescription.setText(R.string.NotificationSubscriptions_Label_NoData)
        setSubscriptionViewsVisibilities()
        if (viewModel.isUserSubscribedForNotifications()) {
            fetchRequests()
        }
    }

    private fun subscribeToObservables() {
        viewModel.bloodTypeRequestsLiveDataState.observe(viewLifecycleOwner, {
            when (it) {
                is DataState.Success -> {
                    swipeRefreshLayout.isRefreshing = false
                    adapter.submitList(it.data?.reversed())
                    if (it.data?.isEmpty() == true) {
                        binding.includedLayoutNoData.layoutNoData.visibility = View.VISIBLE
                    } else {
                        binding.includedLayoutNoData.layoutNoData.visibility = View.GONE
                    }

                    setSubscriptionViewsVisibilities()
                }
                is DataState.Error -> {
                    showServerErrorDialog()
                    swipeRefreshLayout.isRefreshing = false
                }
                DataState.Loading -> {
                    swipeRefreshLayout.isRefreshing = true
                }
            }
        })

        viewModel.subscriptionDataState.observe(viewLifecycleOwner, {
            if (it is DataState.Success) {
                onNotificationSubChanged(it.data)
            }
        })
    }

    private fun onNotificationSubChanged(bloodType: BloodType?) {
        setSubscriptionViewsVisibilities()
        if (bloodType != null) {
            fetchRequests()
        }
    }

    private fun setSubscriptionViewsVisibilities() {
        if (viewModel.isUserSubscribedForNotifications()) {
            binding.swipeRefreshLayout.visibility = View.VISIBLE
            binding.textViewSubscriptionInfo.visibility = View.GONE
        } else {
            binding.swipeRefreshLayout.visibility = View.GONE
            binding.includedLayoutNoData.layoutNoData.visibility = View.GONE
            binding.textViewSubscriptionInfo.visibility = View.VISIBLE
        }
    }

    private fun showServerErrorDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.ServerError_Dialog_Title))
            setMessage(getString(R.string.ServerError_Dialog_Message))
            setPositiveButton(R.string.Dialog_Button_Yes) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                fetchRequests()
            }
            setNegativeButton(R.string.Dialog_Button_No) { dialog, _ ->
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun fetchRequests() {
        val bloodType = viewModel.getCurrentUser()?.notificationBloodType
        val notificationCountryCode = viewModel.getCurrentUser()?.notificationCountryCode

        if (bloodType != null && notificationCountryCode != null) {
            viewModel.fetchRequestsForBloodType(notificationCountryCode, bloodType)
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            fetchRequests()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
