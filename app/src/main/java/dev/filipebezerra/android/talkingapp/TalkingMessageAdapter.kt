package dev.filipebezerra.android.talkingapp

import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.filipebezerra.android.talkingapp.MessageViewHolder.Companion.createFrom
import dev.filipebezerra.android.talkingapp.databinding.MessageListItemBinding
import dev.filipebezerra.android.talkingapp.databinding.MessageListItemBinding.inflate
import dev.filipebezerra.android.talkingapp.domain.TalkingMessage

class TalkingMessageAdapter : ListAdapter<TalkingMessage, MessageViewHolder>(MessageItemCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        createFrom(parent)

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) =
        holder.bindTo(getItem(position))
}

class MessageViewHolder private constructor(
    private val itemViewBinding: MessageListItemBinding
) : RecyclerView.ViewHolder(itemViewBinding.root) {
    fun bindTo(item: TalkingMessage) = with(itemViewBinding) {
        message = item
        executePendingBindings()
    }

    companion object {
        fun createFrom(parent: ViewGroup) = MessageViewHolder(
            inflate(
                from(parent.context),
                parent,
                false
            )
        )
    }
}

object MessageItemCallback : DiffUtil.ItemCallback<TalkingMessage>() {
    override fun areItemsTheSame(oldItem: TalkingMessage, newItem: TalkingMessage) = oldItem === newItem

    override fun areContentsTheSame(oldItem: TalkingMessage, newItem: TalkingMessage) = oldItem == newItem
}