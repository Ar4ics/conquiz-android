package com.gizmodev.conquiz.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.databinding.FragmentProfilePageBinding
import com.gizmodev.conquiz.ui.core.AppFragment
import com.google.android.material.button.MaterialButton
import javax.inject.Inject

class ProfileFragment : AppFragment() {

    @Inject
    lateinit var vm: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentProfilePageBinding.inflate(inflater, container, false)
            .apply {
                setLifecycleOwner(this@ProfileFragment)
                state = this@ProfileFragment.vm.state
                executePendingBindings()
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialButton>(R.id.sign_in).setOnClickListener { goLogin() }
        view.findViewById<MaterialButton>(R.id.sign_out).setOnClickListener { goLogout() }
        view.findViewById<MaterialButton>(R.id.go_to_games).setOnClickListener {
            navController().navigate(ProfileFragmentDirections.actionProfilePageToListGames())
        }
    }

    private fun goLogin() {
        navController().navigate(ProfileFragmentDirections.actionProfilePageToLoginPage())
    }

    private fun goLogout() {
        vm.logout()
        navController().navigate(ProfileFragmentDirections.actionProfilePageToLoginPage())
    }
}