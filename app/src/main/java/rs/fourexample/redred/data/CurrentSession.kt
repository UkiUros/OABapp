package rs.fourexample.redred.data

import android.location.Location

class CurrentSession private constructor(
    var user: User? = null,
    var countryCode: String? = null,
    var notificationToken: String? = null,
    var location: Location? = null,
) {


    companion object {

        @Volatile
        private var INSTANCE: CurrentSession? = null

        @Synchronized
        fun getInstance(): CurrentSession =
            INSTANCE ?: CurrentSession().also { INSTANCE = it }
    }
}
