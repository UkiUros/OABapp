package rs.fourexample.redred.data

data class User(
    var userId: String,
    var name: String?,
    var email: String?,
    var phoneNumber: String?,
    val photoUrl: String?,
    var notificationBloodType: BloodType?,
    var notificationCountryCode: String? = ""
)
