package dev.filipebezerra.android.talkingapp

import android.content.Intent
import android.net.Uri
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import dev.filipebezerra.android.talkingapp.domain.TalkingMessage
import dev.filipebezerra.android.talkingapp.domain.TalkingMessage.Companion.MESSAGES_DATABASE
import dev.filipebezerra.android.talkingapp.util.event.Event
import dev.filipebezerra.android.talkingapp.util.ext.postEvent
import timber.log.Timber

class TalkingViewModel : ViewModel() {

    private val database = Firebase.database.reference.child(MESSAGES_DATABASE)

    private val chatPhotosStorage = Firebase.storage.reference.child("chat_photos")

    private var userName: String = ANONYMOUS

    private val _messages = MutableLiveData<List<TalkingMessage>>().apply { value = emptyList() }
    val messages: LiveData<List<TalkingMessage>>
        get() = _messages

    val noMessages = Transformations.map(_messages) { it.isEmpty() }
    val currentTextMessage = MutableLiveData<String>()

    private val _messageSentEvent = MutableLiveData<Event<Unit>>()
    val messageSentEvent: LiveData<Event<Unit>>
        get() = _messageSentEvent

    private var databaseListener: ValueEventListener? = null

    private fun loadMessages() {
        if (databaseListener == null) {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Timber.d("Received data. Count: ${snapshot.childrenCount}")
                        val list = mutableListOf<TalkingMessage>()
                        snapshot.children.forEach { child ->
                            child.getValue<TalkingMessage>()?.let {
                                list.add(it)
                            }
                        }
                        list.takeIf { it.isNotEmpty() }?.let {
                            _messages.value = it
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(
                        error.toException(),
                        "An error occurred when listening for value events"
                    )
                }
            }).also { databaseListener = it }
        }
    }

    fun onSendTextMessageInputAction(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            currentTextMessage.value.takeIf { it.isNullOrBlank().not() }?.let { textMessage ->
                TalkingMessage(
                    userName = userName,
                    text = textMessage
                ).also {
                    sendMessageToFirebase(it)
                }
            }
            return true
        }
        return false
    }

    private fun sendMessageToFirebase(message: TalkingMessage) {
        database.push().setValue(message)
            .addOnSuccessListener {
                Timber.d("Message sent to Firebase with success")
                _messageSentEvent.postEvent(Unit)
            }
            .addOnFailureListener { error -> Timber.e(
                error,
                "Failed to send message to Firebase"
            ) }
    }

    override fun onCleared() {
        super.onCleared()
        databaseListener?.let { database.removeEventListener(it) }
    }

    fun onUserAuthenticated() {
        userName = Firebase.auth.currentUser?.displayName!!
        loadMessages()
    }

    fun onUserSignedOut() {
        userName = ANONYMOUS
        databaseListener?.let {
            database.removeEventListener(it)
            databaseListener = null
        }
    }

    fun onActivityResultForPhotoPicker(resultIntent: Intent?) {
        val selectedImageUri = resultIntent?.data ?: return
        uploadImageToFirebase(selectedImageUri)
    }

    private fun uploadImageToFirebase(selectedImageUri: Uri) {
        val uploadReference = chatPhotosStorage.child(selectedImageUri.lastPathSegment!!)
        uploadReference.putFile(selectedImageUri).apply {
            trackImageUploadProgress()
            handleWhenImageUploadPaused()
            handleWhenImageUploadCancelled()
            handleWhenImageUploadFailed()
            handleWhenImageUploadSucceed(uploadReference)
        }
    }

    private val jpegMetadata = storageMetadata { contentType = "image/jpeg" }

    private fun UploadTask.trackImageUploadProgress() =
        addOnProgressListener { (bytesTransferred, totalByteCount) ->
            val progress = (100.0 * bytesTransferred) / totalByteCount
            Timber.d("Image upload is $progress done")
        }

    private fun UploadTask.handleWhenImageUploadPaused() = addOnPausedListener {
        Timber.d("Image upload is paused")
    }

    private fun UploadTask.handleWhenImageUploadCancelled() = addOnCanceledListener {
        Timber.d("Image upload is cancelled")
    }

    private fun UploadTask.handleWhenImageUploadFailed() = addOnFailureListener {
        Timber.e(it, "Image upload failed")
    }

    private fun UploadTask.handleWhenImageUploadSucceed(uploadReference: StorageReference) =
        addOnSuccessListener {
            Timber.d("Image upload succeed")
            uploadReference.downloadUrl.addOnSuccessListener { uri ->
                TalkingMessage(
                    userName = userName,
                    photoUrl = uri.toString()
                ).also {
                    sendMessageToFirebase(it)
                }
            }
        }

    companion object {
        private const val ANONYMOUS = "Anonymous"
    }
}