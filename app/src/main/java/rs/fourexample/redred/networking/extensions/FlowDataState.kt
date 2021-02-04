package rs.fourexample.redred.networking.extensions

import java.lang.Exception

sealed class FlowDataState<out R> {
    data class Success<out T>(val data: T) : FlowDataState<T>()

    data class Error(val exception: Exception) : FlowDataState<Nothing>()
}
