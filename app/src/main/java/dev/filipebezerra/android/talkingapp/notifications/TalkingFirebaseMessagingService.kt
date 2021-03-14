package dev.filipebezerra.android.talkingapp.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import timber.log.Timber

class TalkingFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Timber.d("Refreshed token: $newToken")
    }
}