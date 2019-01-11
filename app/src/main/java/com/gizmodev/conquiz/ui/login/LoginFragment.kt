package com.gizmodev.conquiz.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.databinding.FragmentLoginPageBinding
import com.gizmodev.conquiz.ui.core.AppFragment
import com.gizmodev.conquiz.ui.core.rx.toFlowable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject


class LoginFragment : AppFragment() {

    @Inject
    lateinit var vm: LoginViewModel

    lateinit var googleSignInClient: GoogleSignInClient

    private fun onLogin() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GET_AUTH_CODE)
        vm.state.signing.set(true)
    }

    companion object {
        const val RC_GET_AUTH_CODE = 1
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(resources.getString(com.gizmodev.conquiz.R.string.google_client_id))
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_GET_AUTH_CODE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            vm.handleSignInResult(task)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentLoginPageBinding.inflate(inflater, container, false)
            .apply {
                state = this@LoginFragment.vm.state
                executePendingBindings()
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("view created")
        vm.state.signed.toFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("signed=$it")
                navController().navigate(LoginFragmentDirections.actionLoginPageToListGames())
            }
            .untilDestroyView()
        view.findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener { onLogin() }
    }

}
