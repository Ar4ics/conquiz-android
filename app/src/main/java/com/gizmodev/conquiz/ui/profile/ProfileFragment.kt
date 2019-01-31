package com.gizmodev.conquiz.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gizmodev.conquiz.databinding.FragmentProfilePageBinding
import com.gizmodev.conquiz.ui.core.AppFragment
import kotlinx.android.synthetic.main.fragment_profile_page.*
import timber.log.Timber
import javax.inject.Inject

class ProfileFragment : AppFragment() {

    @Inject
    lateinit var vm: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentProfilePageBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@ProfileFragment
                state = this@ProfileFragment.vm.state
                executePendingBindings()
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sign_in.setOnClickListener { goLogin() }
        sign_out.setOnClickListener { goLogout() }
        go_to_games.setOnClickListener {
            navController().navigate(ProfileFragmentDirections.actionProfilePageToListGames())
        }
        create_game_btn.setOnClickListener {
            navController().navigate(ProfileFragmentDirections.actionProfilePageToCreateGame())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("view destroyed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("destroyed")
    }

    private fun goLogin() {
        navController().navigate(ProfileFragmentDirections.actionProfilePageToLoginPage())
    }

    private fun goLogout() {
        vm.logout()
        navController().navigate(ProfileFragmentDirections.actionProfilePageToLoginPage())
    }
}