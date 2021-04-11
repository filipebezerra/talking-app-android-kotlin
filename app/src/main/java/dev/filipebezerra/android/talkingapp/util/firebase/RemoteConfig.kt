package dev.filipebezerra.android.talkingapp.util.firebase

import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import timber.log.Timber

object RemoteConfig {

    private val remoteConfig by lazy { Firebase.remoteConfig }

    private const val TALKING_MESSAGE_LENGTH = "talking_message_length"

    fun init() {
        remoteConfig.setDefaultsAsync(mapOf(
            TALKING_MESSAGE_LENGTH to 140
        ))
    }

    fun fetchConfig(notifyWhenReady: (remoteConfig: FirebaseRemoteConfig) -> Unit) {
        remoteConfig.fetch(when (BuildConfig.DEBUG) {
            true -> 0
            false -> 3600
        }).apply {
            addOnSuccessListener {
                remoteConfig.activate()
                notifyWhenReady(remoteConfig)
            }
            addOnFailureListener { Timber.e(it, "Firebase failed to fetch Remote Config") }
        }
    }

    fun talkingMessageLength(): Int = remoteConfig.getLong(TALKING_MESSAGE_LENGTH).toInt()
}