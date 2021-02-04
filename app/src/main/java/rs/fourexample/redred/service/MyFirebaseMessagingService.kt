package rs.fourexample.redred.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import rs.fourexample.redred.data.CurrentSession
import rs.fourexample.redred.R
import rs.fourexample.redred.constants.*
import rs.fourexample.redred.repository.UserRepository
import rs.fourexample.redred.ui.BloodRequestDetailsActivity
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var currentSession: CurrentSession

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {

            val requestId = remoteMessage.data["request_id"]
            val address = remoteMessage.data["address"] ?: ""
            val bloodType = remoteMessage.data["blood_type"] ?: ""
            val countryCode = remoteMessage.data["country_code"]

            if (requestId.isNullOrEmpty() || countryCode.isNullOrEmpty()
            ) return

            sendBloodRequestNotification(countryCode, requestId, bloodType, address)

            //
        }
    }

    override fun onNewToken(token: String) {
        val userId = currentSession.user?.userId ?: return
        Timber.d("FCM New token received for user Id = $userId")
        //update notification tokes in db

        saveNotificationTokenToFirebase(userId, token)
    }

    private fun saveNotificationTokenToFirebase(userId: String, token: String) =
        GlobalScope.launch(Dispatchers.Main) {
            userRepository.addNotificationToken(userId, token)
        }

    private fun sendBloodRequestNotification(
        countryCode: String,
        requestId: String,
        bloodType: String,
        address: String
    ) {
        val intent = Intent(this, BloodRequestDetailsActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            it.putExtra(BloodRequestDetailsActivity.EXTRA_REQUEST_COUNTRY_CODE, countryCode)
            it.putExtra(BloodRequestDetailsActivity.EXTRA_REQUEST_ID, requestId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val messageBody: String = getString(
            R.string.default_notification_donation_message,
            convertBloodTypeToReadable(bloodType),
            address
        )
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_droplet_red)
            .setContentTitle(getString(R.string.default_notification_donation_title))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun convertBloodTypeToReadable(bloodType: String): String {
        return when (bloodType) {
            O_POSITIVE -> "0+"
            O_NEGATIVE -> "0-"
            A_POSITIVE -> "A+"
            A_NEGATIVE -> "A-"
            B_POSITIVE -> "B+"
            B_NEGATIVE -> "B-"
            AB_POSITIVE -> "AB+"
            AB_NEGATIVE -> "AB-"
            else -> "--"
        }
    }
}
