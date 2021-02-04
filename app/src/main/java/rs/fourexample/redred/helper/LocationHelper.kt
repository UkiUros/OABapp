package rs.fourexample.redred.helper

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.text.TextUtils
import timber.log.Timber
import java.util.*

fun extractStreetNameFromLocation(context: Context, location: Location?): String {
    if (location == null) return ""

    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    return try {
        val singleAddress = addresses[0]
        val streetName = singleAddress.thoroughfare
        val streetNumber = singleAddress.subThoroughfare
        if (streetNumber.isNullOrEmpty()) {
            streetName
        } else {
            TextUtils.concat(streetName, ", ", streetNumber).toString()
        }
    } catch (e: Exception) {
        Timber.e(e)
        ""
    }
}

fun extractCountryNameFromLocation(context: Context, location: Location?): String? {
    if (location == null) return null

    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    return try {
        val singleAddress = addresses[0]
        singleAddress.countryName
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

fun extractCountryCodeFromLocation(context: Context, location: Location): String {
    val geocoder = Geocoder(context, Locale.ENGLISH)
    return try {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val singleAddress = addresses[0]
        val countryCode = singleAddress.countryCode.toLowerCase()
        countryCode
    } catch (e: Exception) {
        e.printStackTrace()
        // hack
        return listOf("rs", "ba", "hr").random()
    }
}
