package dev.filipebezerra.android.talkingapp

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.filipebezerra.android.talkingapp.domain.TalkingMessage

@BindingAdapter("messagesList")
fun RecyclerView.bindMessagesList(messagesList: List<TalkingMessage>?) = messagesList?.run {
    (adapter as TalkingMessageAdapter).submitList(this)
}

@BindingAdapter("visibleIf")
fun View.bindVisibleIf(isVisible: Boolean) = apply { this.isVisible = isVisible }

@BindingAdapter("goneIf")
fun View.bindGoneIf(isGone: Boolean) = apply { isVisible = isGone.not() }