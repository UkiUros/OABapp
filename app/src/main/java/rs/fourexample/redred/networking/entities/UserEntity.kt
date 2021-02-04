package rs.fourexample.redred.networking.entities

data class UserEntity(
    val userId: String = "",
    val name: String = "",
    val email: String? = "" ,
    val phoneNumber: String? = "",
    val photoUrl: String? = "",
    val notificationBloodType: String? = "",
    val notificationCountryCode: String? = ""
)
