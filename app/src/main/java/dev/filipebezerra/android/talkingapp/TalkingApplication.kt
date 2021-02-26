package dev.filipebezerra.android.talkingapp

import android.app.Application
import timber.log.Timber

class TalkingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BuildConfig.DEBUG.takeIf { it }?.run { Timber.plant(Timber.DebugTree()) }
    }
}