package rs.fourexample.redred.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.R
import rs.fourexample.redred.constants.URL_PRIVACY_POLICY
import rs.fourexample.redred.constants.URL_TERMS_CONDITIONS
import rs.fourexample.redred.data.SettingsItem
import rs.fourexample.redred.databinding.SettingsFragmentBinding
import rs.fourexample.redred.ui.MainViewModel
import rs.fourexample.redred.ui.adapters.SettingsAdapter
import rs.fourexample.redred.ui.adapters.SimpleDividerItemDecoration
import timber.log.Timber

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SettingsFragment : Fragment(), SettingsAdapter.ISettingsSelector {

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    private val adapter = SettingsAdapter()

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.recyclerViewSettings.addItemDecoration(
            SimpleDividerItemDecoration(
                requireContext(),
                true
            )
        )

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        viewModel.userLiveData.observe(viewLifecycleOwner, {
            adapter.settingsList = getSettingsList()
            adapter.notifyDataSetChanged()
        })

        adapter.settingsList = getSettingsList()
        adapter.callback = this
        binding.recyclerViewSettings.adapter = adapter
    }

    private fun getSettingsList(): MutableList<SettingsItem> {
        return if (viewModel.getCurrentUser() == null) {
            mutableListOf(
                SettingsItem.TermsAndConditions,
                SettingsItem.PrivacyPolicy,
                SettingsItem.AppVersionName
            )
        } else {
            mutableListOf(
                SettingsItem.LogOut,
                SettingsItem.TermsAndConditions,
                SettingsItem.PrivacyPolicy,
                SettingsItem.AppVersionName
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTermsAndConditions() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_TERMS_CONDITIONS)))
    }

    override fun onPrivacyPolicy() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRIVACY_POLICY)))
    }

    override fun onLogOutClicked() {
        showLogOutDialog()
    }

    private fun showLogOutDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(getString(R.string.Settings_Dialog_Message_LogOut))
            setPositiveButton(R.string.Dialog_Button_Yes) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                logOut()
            }
            setNegativeButton(R.string.Dialog_Button_No) { dialog, _ ->
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun logOut() {
        Timber.d("Logging out...")
        viewModel.mutableLoadingState.value = true
        Credentials.getClient(requireActivity()).disableAutoSignIn();
        viewModel.signOut()

    }

}
