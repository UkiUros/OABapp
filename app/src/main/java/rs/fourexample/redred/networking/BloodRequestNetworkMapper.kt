package rs.fourexample.redred.networking

import rs.fourexample.redred.constants.*
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.data.BloodType
import rs.fourexample.redred.networking.entities.BloodRequestEntity
import javax.inject.Inject

class BloodRequestNetworkMapper
@Inject
constructor() {
    fun mapFromEntity(entity: BloodRequestEntity): BloodRequest {
        return BloodRequest(
            requestId = entity.requestId,
            userName = entity.userName ?: "",
            userPhoto = entity.userPhoto,
            userId = entity.userId ?: "",
            bloodType = getBloodTypeFromEntity(entity.bloodType ?: ""),
            countryCode = entity.countryCode ?: "",
            address = entity.address ?: "",
            contactPhone = entity.contactPhone ?: "",
            description = entity.description ?: "",
            latitude = entity.latitude ?: 0.0,
            longitude = entity.longitude ?: 0.0,
            timestamp = entity.timestamp ?: 0L,
            isActive = entity.isActive ?: false
        )
    }

    fun mapToEntity(domainModel: BloodRequest): BloodRequestEntity {
        return BloodRequestEntity(
            requestId = domainModel.requestId,
            userName = domainModel.userName,
            userPhoto = domainModel.userPhoto,
            userId = domainModel.userId,
            bloodType = getBloodTypeAsText(domainModel.bloodType!!),
            countryCode = domainModel.countryCode,
            address = domainModel.address,
            contactPhone = domainModel.contactPhone,
            description = domainModel.description,
            latitude = domainModel.latitude,
            longitude = domainModel.longitude,
            timestamp = domainModel.timestamp,
            isActive = domainModel.isActive
        )
    }

    private fun getBloodTypeFromEntity(type: String): BloodType? {
        return when (type) {
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

    fun getBloodTypeAsText(bloodType: BloodType): String {
        return when (bloodType) {
            is BloodType.OPositive -> O_POSITIVE
            is BloodType.ONegative -> O_NEGATIVE
            is BloodType.APositive -> A_POSITIVE
            is BloodType.ANegative -> A_NEGATIVE
            is BloodType.BPositive -> B_POSITIVE
            is BloodType.BNegative -> B_NEGATIVE
            is BloodType.ABPositive -> AB_POSITIVE
            is BloodType.ABNegative -> AB_NEGATIVE
        }
    }

    fun mapFromEntityList(entities: List<BloodRequestEntity>): List<BloodRequest> {
        return entities.map { mapFromEntity(it) }
    }
}
