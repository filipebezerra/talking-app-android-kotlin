package dev.filipebezerra.android.talkingapp.domain

data class TalkingMessage(
    val userName: String? = null,
    val text: String? = null,
    val photoUrl: String? = null,
) {
    companion object {
        const val MESSAGES_DATABASE = "messages"
    }
}