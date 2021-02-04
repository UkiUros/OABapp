package rs.fourexample.redred.data

sealed class SettingsItem {
    object LogOut: SettingsItem()
    object TermsAndConditions: SettingsItem()
    object PrivacyPolicy: SettingsItem()
    object AppVersionName: SettingsItem()
}
