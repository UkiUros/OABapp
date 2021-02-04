package rs.fourexample.redred.ui.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import rs.fourexample.redred.R
import rs.fourexample.redred.data.SettingsItem
import rs.fourexample.redred.databinding.*

class SettingsAdapter : RecyclerView.Adapter<SettingsAdapter.SettingsHolder>() {

    private val ITEM_TYPE_LOG_OUT = 1
    private val ITEM_TYPE_TERMS = 2
    private val ITEM_TYPE_PRIVACY = 3
    private val ITEM_TYPE_VERSION = 4

    var settingsList: MutableList<SettingsItem> = mutableListOf()

    interface ISettingsSelector {
        fun onTermsAndConditions()
        fun onPrivacyPolicy()
        fun onLogOutClicked()
    }

    var callback: ISettingsSelector? = null

    abstract class SettingsHolder(private val viewBinding: ViewBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        abstract fun bind(item: SettingsItem)
    }

    inner class LogOutHolder(private val binding: ItemSettingsSimpleBinding) :
        SettingsHolder(binding) {
        override fun bind(item: SettingsItem) {
            if (item !is SettingsItem.LogOut) return

            binding.tvSettingsLabel.setText(R.string.Settings_Item_LogOut)
            itemView.setOnClickListener { callback?.onLogOutClicked() }
        }
    }

    inner class TermsHolder(private val binding: ItemSettingsSimpleBinding) :
        SettingsHolder(binding) {
        override fun bind(item: SettingsItem) {
            if (item !is SettingsItem.TermsAndConditions) return

            binding.tvSettingsLabel.setText(R.string.Settings_Item_Terms)
            itemView.setOnClickListener { callback?.onTermsAndConditions() }
        }
    }

    inner class PrivacyHolder(private val binding: ItemSettingsSimpleBinding) :
        SettingsHolder(binding) {
        override fun bind(item: SettingsItem) {
            if (item !is SettingsItem.PrivacyPolicy) return

            binding.tvSettingsLabel.setText(R.string.Settings_Item_Privacy)
            itemView.setOnClickListener { callback?.onPrivacyPolicy() }
        }
    }

    inner class AppVersionHolder(private val binding: ItemSettingsVersionBinding) :
        SettingsHolder(binding) {
        override fun bind(item: SettingsItem) {
            if (item !is SettingsItem.AppVersionName) return

            val context = itemView.context
            binding.tvAppVersion.text = TextUtils.concat(
                context.getString(
                    R.string.Settings_Item_Version,
                    getAppVersionName(context)
                ),
                " (", getAppVersionCode(context).toString(), ")"
            ).toString()
        }

        private fun getAppVersionName(context: Context): String {
            val packageInfo =
                context.packageManager.getPackageInfo(context.applicationContext.packageName, 0)
            return packageInfo?.versionName ?: "0"
        }

        private fun getAppVersionCode(context: Context): Int {
            val packageInfo =
                context.packageManager.getPackageInfo(context.applicationContext.packageName, 0)

            return packageInfo?.versionCode ?: -1
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsHolder {
        return when (viewType) {
            ITEM_TYPE_LOG_OUT ->
                LogOutHolder(
                    ItemSettingsSimpleBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

           ITEM_TYPE_TERMS ->
                TermsHolder(
                    ItemSettingsSimpleBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            ITEM_TYPE_PRIVACY ->
                PrivacyHolder(
                    ItemSettingsSimpleBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            ITEM_TYPE_VERSION ->
                AppVersionHolder(
                    ItemSettingsVersionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    override fun onBindViewHolder(holder: SettingsHolder, position: Int) {
        holder.bind(settingsList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return when (settingsList[position]) {
            SettingsItem.LogOut -> ITEM_TYPE_LOG_OUT
            SettingsItem.TermsAndConditions -> ITEM_TYPE_TERMS
            SettingsItem.PrivacyPolicy -> ITEM_TYPE_PRIVACY
            SettingsItem.AppVersionName -> ITEM_TYPE_VERSION
        }
    }

}
