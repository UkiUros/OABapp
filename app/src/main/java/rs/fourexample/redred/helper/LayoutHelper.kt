package rs.fourexample.redred.helper

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

fun dpToPx(dp: Int, context: Context): Int {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}
