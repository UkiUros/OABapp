package rs.fourexample.redred.networking

import rs.fourexample.redred.constants.*
import rs.fourexample.redred.data.BloodType
import rs.fourexample.redred.data.User
import rs.fourexample.redred.networking.entities.UserEntity
import javax.inject.Inject

class UserNetworkMapper
@Inject
constructor() {

    fun mapFromEntity(entity: UserEntity): User {
        return User(
            userId = entity.userId,
            name = entity.name,
            email = entity.email ?: "",
            phoneNumber = entity.phoneNumber ?: "",
            photoUrl = entity.photoUrl ?: "",
            notificationBloodType = getNotificationBloodTypeFromEntity(entity.notificationBloodType),
            notificationCountryCode = entity.notificationCountryCode
        )
    }

    fun mapToEntity(domainModel: User): UserEntity {
        return UserEntity(
            userId = domainModel.userId,
            name = domainModel.name ?: "",
            email = domainModel.email ?: "",
            phoneNumber = domainModel.phoneNumber,
            photoUrl = domainModel.photoUrl ?: "",
            notificationBloodType = getNotificationBloodTypeAsText(domainModel.notificationBloodType),
            notificationCountryCode = domainModel.notificationCountryCode ?: ""
        )
    }

    private fun getNotificationBloodTypeFromEntity(type: String?): BloodType? {
        return when(type) {
            O_POSITIVE -> BloodType.OPositive()
            O_NEGATIVE -> BloodType.ONegative()
            A_POSITIVE -> BloodType.APositive()
            A_NEGATIVE -> BloodType.ABNegative()
            B_POSITIVE -> BloodType.BPositive()
            B_NEGATIVE -> BloodType.BNegative()
            AB_POSITIVE -> BloodType.ABPositive()
            AB_NEGATIVE -> BloodType.ABNegative()
            else -> null
        }
    }

    fun getNotificationBloodTypeAsText(bloodType: BloodType?): String? {
        return when(bloodType) {
            is BloodType.OPositive -> O_POSITIVE
            is BloodType.ONegative -> O_NEGATIVE
            is BloodType.APositive -> A_POSITIVE
            is BloodType.ANegative -> A_NEGATIVE
            is BloodType.BPositive -> B_POSITIVE
            is BloodType.BNegative -> B_NEGATIVE
            is BloodType.ABPositive -> AB_POSITIVE
            is BloodType.ABNegative -> AB_NEGATIVE
            null -> null
        }
    }
}
