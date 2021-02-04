package rs.fourexample.redred.networking.entities

data class BloodRequestEntity(
    var requestId: String? = "",
    val userName: String? = "",
    val userPhoto: String? = "",
    val userId: String? = "",
    val bloodType: String? = "",
    val countryCode: String? = "",
    val address: String? = "",
    val contactPhone: String? = "",
    val description: String? = "",
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val timestamp: Long? = 0L,
    @field:JvmField val isActive: Boolean? = false
)
