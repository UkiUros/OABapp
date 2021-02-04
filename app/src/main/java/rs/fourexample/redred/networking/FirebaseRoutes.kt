package rs.fourexample.redred.networking

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseRoutes private constructor(
    private val firebaseDatabase: FirebaseDatabase
) {

    fun getUsersReference(): DatabaseReference {
        return firebaseDatabase.getReference(USERS)
    }

    fun getBloodRequestsReference(countryCode: String): DatabaseReference {
        return firebaseDatabase.getReference(BLOOD_REQUESTS).child(countryCode)
    }

    fun getNotificationTokensReference(): DatabaseReference {
        return firebaseDatabase.getReference(NOTIFICATION_TOKENS)
    }

    fun getBloodRequestSubscriptionsReference(countryCode: String): DatabaseReference {
        return firebaseDatabase.getReference(BLOOD_REQUEST_SUBSCRIPTIONS).child(countryCode)
    }

    companion object {
        private const val USERS = "users_table"
        private const val BLOOD_REQUESTS = "blood_requests"
        private const val NOTIFICATION_TOKENS = "notification_tokens"
        private const val BLOOD_REQUEST_SUBSCRIPTIONS = "request_subscriptions"

        @Volatile
        private var INSTANCE: FirebaseRoutes? = null

        @Synchronized
        fun getInstance(firebaseDatabase: FirebaseDatabase): FirebaseRoutes =
            INSTANCE ?: FirebaseRoutes(firebaseDatabase).also { INSTANCE = it }
    }
}
