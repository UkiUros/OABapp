package rs.fourexample.redred.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import rs.fourexample.redred.R
import rs.fourexample.redred.constants.*
import rs.fourexample.redred.data.BloodType
import rs.fourexample.redred.data.User
import rs.fourexample.redred.databinding.ProfileFragmentBinding
import rs.fourexample.redred.ui.MainViewModel
import rs.fourexample.redred.ui.SplashActivity
import rs.fourexample.redred.utils.DataState

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: ProfileFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.swipeRefreshLayout.isEnabled = false
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        viewModel.mutableLoadingState.observe(viewLifecycleOwner, {
            binding.swipeRefreshLayout.isRefreshing = it
        })

        viewModel.userLiveData.observe(viewLifecycleOwner, {
            populateProfile(it)
        })

        viewModel.subscriptionDataState.observe(viewLifecycleOwner, {
            when (it) {
                is DataState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    viewModel.getCurrentUser()?.let { user ->
                        viewModel.updateUserProfile(user)
                    }
                }
                is DataState.Error -> binding.swipeRefreshLayout.isRefreshing = false
                DataState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
            }
        })

        populateProfile(viewModel.getCurrentUser())
    }

    private fun populateProfile(user: User?) {
        if (user == null) {
            // it is anon user
            binding.textViewName.text = "Guest"
            binding.textViewName.text = "-----"
            binding.textViewEmail.text = "----"
            binding.textViewPhone.text = "----"

            binding.buttonLogin.visibility = View.VISIBLE
            binding.buttonEdit.visibility = View.GONE
            binding.buttonLogin.setOnClickListener {
                if (viewModel.isAnonymousUser()) {
                    requireActivity().finish()
                    viewModel.signOut()
                    SplashActivity.start(requireContext())
                }
            }
            return
        }
        binding.textViewName.text = user.name ?: "-----"
        binding.textViewEmail.text = user.email ?: "----"
        binding.textViewPhone.text = user.phoneNumber ?: "----"

        if (user.photoUrl?.isEmpty() == false) {
            Picasso.get()
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_person_outline)
                .into(binding.imageViewProfile)
        }

        binding.buttonLogin.visibility = View.GONE
        binding.buttonEdit.visibility = View.VISIBLE
        binding.buttonEdit.setOnClickListener {
            showEditDialogDialog(user)
        }

        if (user.notificationBloodType == null) {
            binding.chipSelectBloodType.visibility = View.VISIBLE
            binding.chipBloodType.visibility = View.GONE
        } else {
            binding.chipSelectBloodType.visibility = View.GONE
            binding.chipBloodType.visibility = View.VISIBLE
            binding.chipBloodType.text = getString(
                R.string.Profile_Label_SelectedBloodType,
                user.notificationBloodType?.typeValue,
                user.notificationCountryCode?.toUpperCase()
            )
        }

        binding.chipSelectBloodType.setOnClickListener {
            showBloodTypeDialog()
        }

        binding.chipBloodType.setOnCloseIconClickListener {
            if (user.notificationBloodType == null) return@setOnCloseIconClickListener

            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.Profile_Dialog_Unsubscribe_Title)
                setMessage(R.string.Profile_Dialog_Unsubscribe_Message)
                setNegativeButton(R.string.Profile_Dialog_Unsubscribe_ButtonNegative) { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton(R.string.Profile_Dialog_Unsubscribe_ButtonPositive) { _, _ ->
                    viewModel.unsubscribeFromBloodRequestNotifications(user.notificationBloodType!!)
                }
            }.create().show()
        }
    }

    private fun showBloodTypeDialog() {
        val bloodTypeStrings = arrayOf("0-", "0+", "A-", "A+",
            "B-", "B+", "AB-", "AB+")

        val bloodTypes = arrayOf(
            BloodType.ONegative(), BloodType.OPositive(), BloodType.ANegative(), BloodType.APositive(),
            BloodType.BNegative(), BloodType.BPositive(), BloodType.ABNegative(), BloodType.ABPositive())

        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.Profile_Dialog_Subscribe_Title)

            setSingleChoiceItems(bloodTypeStrings, -1) { dialog, index ->
                dialog.dismiss()
                viewModel.subscribeToBloodRequestNotifications(bloodTypes[index])
            }
        }.create().show()
    }

    private fun showEditDialogDialog(user: User) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            val editDialogLayoutView =
                layoutInflater.inflate(R.layout.dialog_layout_edit_profile, null)

            val editTextName: EditText =
                editDialogLayoutView.findViewById(R.id.editTextName)

            val editTextEmail: EditText =
                editDialogLayoutView.findViewById(R.id.editTextEmail)

            val editTextPhone: EditText =
                editDialogLayoutView.findViewById(R.id.editTextPhone)

            editTextName.setText(user.name)
            editTextEmail.setText(user.email)
            editTextPhone.setText(user.phoneNumber)

            setTitle(R.string.Profile_Button_Edit)
            setPositiveButton(R.string.Profile_Button_Save) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                val name = editTextName.text.toString()
                val phone = editTextPhone.text.toString()
                val bio = editTextEmail.text.toString()

                validateForm(name, phone, bio)
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }

            setView(editDialogLayoutView)
        }.create().show()
    }

    private fun validateForm(name: String, phone: String, email: String) {
        if (name.isEmpty()) return

        val currentUser = viewModel.getCurrentUser()
        // check if no changes
        if (name == currentUser?.name && phone == currentUser.phoneNumber && email == currentUser.email) return

        currentUser?.name = name
        currentUser?.phoneNumber = phone
        currentUser?.email = email

        if (currentUser != null) {
            viewModel.updateUserProfile(currentUser)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
