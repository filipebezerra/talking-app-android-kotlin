package dev.filipebezerra.android.talkingapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.filipebezerra.android.talkingapp.databinding.TalkingMainActivityBinding
import dev.filipebezerra.android.talkingapp.util.event.EventObserver
import dev.filipebezerra.android.talkingapp.util.ext.hideKeyboard

class TalkingMainActivity : AppCompatActivity() {
    private lateinit var viewBinding: TalkingMainActivityBinding

    private val talkingViewModel: TalkingViewModel by viewModels()

    private lateinit var talkingMessageAdapter: TalkingMessageAdapter

    private lateinit var firebaseAuthListener: FirebaseAuth.AuthStateListener
    private val firebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<TalkingMainActivityBinding>(
            this,
            R.layout.talking_main_activity
        ).apply {
            viewBinding = this
            viewModel = talkingViewModel
            lifecycleOwner = this@TalkingMainActivity

            initializeAdapter()
            observeUI()

            textMessageInputLayout.setStartIconOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                }.run { startActivityForResult(this, RC_PHOTO_PICKER) }
            }
        }
    }

    private fun initializeAdapter() {
        talkingMessageAdapter = TalkingMessageAdapter()
            .also { viewBinding.messagesList.adapter = it }
    }

    private fun observeUI() {
        with(talkingViewModel) {
            messageSentEvent.observe(this@TalkingMainActivity, EventObserver {
                viewBinding.textMessageInput.apply {
                    text?.clear()
                    hideKeyboard()
                }
            })
        }

        firebaseAuthListener = FirebaseAuth.AuthStateListener {
            if (firebaseAuth.currentUser != null) {
                talkingViewModel.onUserAuthenticated()
            } else {
                talkingViewModel.onUserSignedOut()
                initializeAdapter()

                // TODO: Fix merging sign in existing account with different provider, for example:
                // User created account with email/password, then used Google sign in to login
                startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(
                            listOf(
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build()
                            )
                        )
                        .setLogo(R.mipmap.ic_launcher_round)
                        .setTheme(R.style.Theme_TalkingApp)
                        .setLockOrientation(true)
                        .setTosAndPrivacyPolicyUrls(
                            "https://policies.google.com/terms",
                            "https://policies.google.com/privacy"
                        )
                        .build(),
                    RC_SIGN_IN
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean =
        menuInflater.inflate(R.menu.menu, menu).run { true }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when(item.itemId) {
            R.id.action_logout -> {
                AuthUI.getInstance().signOut(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            RC_SIGN_IN -> {
                when (resultCode) {
                    Activity.RESULT_CANCELED -> finish()
                    else -> {
                        IdpResponse.fromResultIntent(data).takeIf { it != null }?.run {
                            if (error?.errorCode == ErrorCodes.NO_NETWORK) {
                                Snackbar.make(
                                    viewBinding.mainRootLayout,
                                    getString(R.string.no_internet_connection),
                                    Snackbar.LENGTH_SHORT
                                ).show();
                            }
                        }
                    }
                }
            }
            RC_PHOTO_PICKER -> resultCode.takeIf { it == Activity.RESULT_OK }?.let {
                talkingViewModel.onActivityResultForPhotoPicker(data)
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 199
        private const val RC_PHOTO_PICKER = 200
    }
}