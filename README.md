# OABapp
This repo contains censored (and slightly changed) source of my OABapp (some initial version).
Play Store link: https://play.google.com/store/apps/details?id=rs.fourexample.oab

This source is what I actually made in **less then 5 days** and there is always a possibility I could have done this better.

#### What does this app do?
The app enables users to enter their blood type and get notified (via push notification) when other users publish blood donation requests nearby for the matching blood type.

#### What I used for the server side?
- Firebase Authorisation
- Firebase Realtime Database - for storing data
- Cloud Functions - for notifications

#### What kind of technologies and architecture did I use in the Android app?
I used the best practices followed by architecture components:
- Kotlin
- Kotlin coroutines
- Dependency Injection with Hilt
- Data class
- MVVM
- Repository
- LiveData
- ViewBinding
- RecyclerView ListAdapter with DiffUtils
- Lottie Animations

#### What is interesting about this project?
It shows how to use Kotlin Coroutines combined with Firebase Realtime Database callbacks and queries. This was quite challenging especially if we take into consideration there are numerous problems with using Coroutine **suspending** functions for awaiting Query callback.

#### Let me show you what I mean
```
val dataSnapshot = firebaseRoutes.getBloodRequestsReference(countryCode)
                .orderByChild("isActive").equalTo(true)
                .limitToFirst(20)
                .get()
                .await()
```

The query above would throw an error **Client is offline** like 3 out of 10 times, for some reason. While exploring the firebase logs, I noticed that firebase SDK would break connection with Realtime Database when calling this.

In this case, *await()* is used to suspend further execution until we get some response.
This is, most likely, because await() might not support *singleValueEvent* listener very well, so I came up with following solution using extensions:

```
@ExperimentalCoroutinesApi
suspend inline fun <reified T> Query.awaitSingleValueEventList(): Flow<FlowDataState<List<T>>> =
    callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
```

And using it like this:
```
suspend fun getActiveBloodRequests(countryCode: String): Flow<FlowDataState<List<BloodRequestEntity>>> {
    return firebaseRoutes.getBloodRequestsReference(countryCode)
        .orderByChild("isActive").equalTo(true)
        .limitToFirst(50)
        .awaitSingleValueEventList()
}
```
