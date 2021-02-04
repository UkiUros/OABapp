package rs.fourexample.redred.helper

import android.text.format.DateUtils
import java.text.ParseException

fun calculateTimeAgo(time: Long): String {
    return try {
        val now = System.currentTimeMillis()
        DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString()
    } catch (e: ParseException) {
        e.printStackTrace()
        time.toString()
    }
}