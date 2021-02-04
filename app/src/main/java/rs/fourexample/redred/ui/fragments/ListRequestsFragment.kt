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
import rs.fourexample.redred.databinding.ListRequestsFragmentBinding
import rs.fourexample.redred.ui.BloodRequestDetailsActivity
import rs.fourexample.redred.ui.MainViewModel
import rs.fourexample.redred.ui.NewRequestActivity
import rs.fourexample.redred.ui.NewRequestActivity.Companion.RQ_NEW_REQUEST
import rs.fourexample.redred.ui.adapters.RequestAdapter
import rs.fourexample.redred.utils.DataState
import java.util.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ListRequestsFragment : Fragment() {

    private var _binding: ListRequestsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: RequestAdapter

    var countryCode: String? = null

    companion object {
        fun newInstance() = ListRequestsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ListRequestsFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonCreateRequest.setOnClickListener {
            if (viewModel.isAnonymousUser()) return@setOnClickListener

            startActivityForResult(Intent(activity, NewRequestActivity::class.java), RQ_NEW_REQUEST)
        }

        adapter = RequestAdapter()
        adapter.callback = object : RequestAdapter.IRequestCallback {
            override fun onItemClick(bloodRequest: BloodRequest) {
                BloodRequestDetailsActivity.start(requireContext(), bloodRequest)
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (countryCode == null) {
                binding.swipeRefreshLayout.isRefreshing = false
                return@setOnRefreshListener
            }
            fetchRequests()
        }

        binding.recyclerViewRequests.adapter = adapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.includedLayoutNoData.textViewDescription.setText(R.string.BloodRequests_Label_NoData)
        subscribeToObservables()
    }

    private fun subscribeToObservables() {
        viewModel.activeRequestsLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is DataState.Success -> {
                    swipeRefreshLayout.isRefreshing = false
                    adapter.submitList(it.data?.reversed())
                    if (it.data?.isEmpty() == true) {
                        binding.includedLayoutNoData.layoutNoData.visibility = View.VISIBLE
                    } else {
                        binding.includedLayoutNoData.layoutNoData.visibility = View.GONE
                    }
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

        viewModel.mutableCountryCodeLiveData.observe(viewLifecycleOwner, {
            viewModel.getCurrentSession().countryCode = it
            countryCode = it
            fetchRequests()
        })
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
        countryCode?.let {
            viewModel.fetchRequestsForCountry(it)
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
