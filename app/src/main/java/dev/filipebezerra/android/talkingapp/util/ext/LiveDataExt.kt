package dev.filipebezerra.android.talkingapp.util.ext

import androidx.lifecycle.MutableLiveData
import dev.filipebezerra.android.talkingapp.util.event.Event

fun <T> MutableLiveData<Event<T>>.postEvent(content: T) {
    postValue(Event(content))
}