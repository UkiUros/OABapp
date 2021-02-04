package rs.fourexample.redred.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BloodRequest(
    var requestId: String?,
    val userName: String,
    val userPhoto: String? = "",
    val userId: String,
    val bloodType: BloodType?,
    val countryCode: String,
    var address: String,
    var contactPhone: String,
    var description: String,
    var latitude: Double,
    var longitude: Double,
    val timestamp: Long,
    var isActive: Boolean
): Parcelable

sealed class BloodType(val typeValue: String): Parcelable {
    @Parcelize data class OPositive(val type: String = "O+") : BloodType(type), Parcelable
    @Parcelize data class ONegative(val type: String = "O-") : BloodType(type), Parcelable
    @Parcelize data class APositive(val type: String = "A+") : BloodType(type), Parcelable
    @Parcelize data class ANegative(val type: String = "A-") : BloodType(type), Parcelable
    @Parcelize data class BPositive(val type: String = "B+") : BloodType(type), Parcelable
    @Parcelize data class BNegative(val type: String = "B-") : BloodType(type), Parcelable
    @Parcelize data class ABPositive(val type: String = "AB+") : BloodType(type), Parcelable
    @Parcelize data class ABNegative(val type: String = "AB-") : BloodType(type), Parcelable
}
