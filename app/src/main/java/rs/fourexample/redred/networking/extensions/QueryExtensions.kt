package rs.fourexample.redred.networking.extensions

import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

@ExperimentalCoroutinesApi
suspend inline fun <reified T> Query.awaitSingleValueEventList(): Flow<FlowDataState<List<T>>> =
    callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Timber.d("RealtimeDatabase awaitSingleValueEventList snapshot = ${snapshot.children}")
                try {
                    val entityList = mutableListOf<T>()
                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue<T>()?.let {
                            entityList.add(it)
                        }
                    }
                    offer(FlowDataState.Success(entityList))
                } catch (e: DatabaseException) {
                    Timber.e(e)
                    offer(FlowDataState.Error(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                offer(FlowDataState.Error(error.toException()))
            }
        }

        addListenerForSingleValueEvent(valueEventListener)

        awaitClose { removeEventListener(valueEventListener) }
    }

@ExperimentalCoroutinesApi
suspend inline fun <reified T> Query.awaitSingleValueEventItem(): Flow<FlowDataState<T?>> =
    callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Timber.d("RealtimeDatabase awaitSingleValueEventItem snapshot = ${snapshot.children}")
                try {
                    // queries always return list
                    var entity: T? = null
                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue<T>()?.let {
                            entity = it
                        }
                    }
                    offer(FlowDataState.Success(entity))

                } catch (e: DatabaseException) {
                    Timber.e(e)
                    offer(FlowDataState.Error(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                offer(FlowDataState.Error(error.toException()))
            }
        }

        addListenerForSingleValueEvent(valueEventListener)

        awaitClose { removeEventListener(valueEventListener) }
    }
