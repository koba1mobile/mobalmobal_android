package com.mashup.mobalmobal.ui.sign.signin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.funin.base.funinbase.base.BaseViewBindingFragment
import com.funin.base.funinbase.extension.rx.observeOnMain
import com.funin.base.funinbase.extension.rx.subscribeWithErrorLogger
import com.funin.base.funinbase.extension.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.mashup.mobalmobal.R
import com.mashup.mobalmobal.databinding.FragmentSignInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : BaseViewBindingFragment<FragmentSignInBinding>() {

    companion object {
        private const val EMAIL = "email"
        private const val PUBLIC_PROFILE = "public_profile"
    }

    private val viewModel by viewModels<SignInViewModel>()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleLoginLauncher: ActivityResultLauncher<Intent>

    private lateinit var callbackManager: CallbackManager

    override fun setBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSignInBinding = FragmentSignInBinding.inflate(inflater, container, false)

    override fun onSetupViews(view: View) {
        setGoogleLogin()
        setFacebookLogin()

        binding.signInGoogle.setOnClickListener {
            onGoogleClicked()
        }
    }

    override fun onBindViewModels() {
        viewModel.toastSubject.observeOnMain()
            .subscribeWithErrorLogger { context?.showToast(it, Toast.LENGTH_SHORT) }
            .addToDisposables()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun setGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleLoginLauncher = registerForActivityResult(GoogleLoginResultContract()) { idToken ->
            idToken?.let {
                viewModel.handleGoogleAccessToken(it)
            }
        }
    }

    private fun setFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()

        binding.signInFacebook.setPermissions(EMAIL, PUBLIC_PROFILE)
        binding.signInFacebook.fragment = this
        binding.signInFacebook.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    result?.let {
                        viewModel.handleFacebookAccessToken(it.accessToken)
                    }
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    error?.message?.let {
                        showToast(it)
                    }
                }
            })
    }

    private fun onGoogleClicked() {
        googleLoginLauncher.launch(googleSignInClient.signInIntent)
    }

    fun navigateSignInToSignUp() =
        findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)

    inner class GoogleLoginResultContract : ActivityResultContract<Intent, String>() {
        override fun createIntent(context: Context, input: Intent): Intent = input

        override fun parseResult(resultCode: Int, intent: Intent?): String {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)

            return account?.idToken ?: throw IllegalStateException("Account token must be not null")
        }
    }
}
