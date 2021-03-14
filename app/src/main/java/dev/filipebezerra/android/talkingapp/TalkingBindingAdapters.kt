package dev.filipebezerra.android.talkingapp

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dev.filipebezerra.android.talkingapp.domain.TalkingMessage
import dev.filipebezerra.android.talkingapp.util.ext.applyImprovements

@BindingAdapter("messagesList")
fun RecyclerView.bindMessagesList(messagesList: List<TalkingMessage>?) = messagesList?.let { list ->
    with((adapter as TalkingMessageAdapter)) {
        submitList(list) {
            smoothScrollToPosition(itemCount.dec())
        }
    }
}

@BindingAdapter("visibleIf")
fun View.bindVisibleIf(isVisible: Boolean) = apply { this.isVisible = isVisible }

@BindingAdapter("goneIf")
fun View.bindGoneIf(isGone: Boolean) = apply { isVisible = isGone.not() }

@BindingAdapter("imageUrl")
fun ImageView.bindImageUrl(imageUrl: String?) = imageUrl?.let { url ->
    Glide.with(context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply { applyImprovements() }
        .into(this)
}