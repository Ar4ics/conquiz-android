package com.gizmodev.conquiz.ui.creategame

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.gizmodev.conquiz.BR
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.databinding.FragmentCreateGameBinding
import com.gizmodev.conquiz.ui.core.AppFragment
import kotlinx.android.synthetic.main.fragment_create_game.*
import me.tatarka.bindingcollectionadapter2.ItemBinding
import timber.log.Timber
import javax.inject.Inject


class CreateGameFragment : AppFragment() {

    @Inject
    lateinit var vm: CreateGameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentCreateGameBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@CreateGameFragment
                state = this@CreateGameFragment.vm.state
                listener = this@CreateGameFragment

                usersbinding = ItemBinding
                    .of<CreateGameViewModel.State.PairUser>(BR.state, com.gizmodev.conquiz.R.layout.view_user_item)

                executePendingBindings()
            }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameX.filters = arrayOf<InputFilter>(InputFilterMinMax(2, 5))
        gameY.filters = arrayOf<InputFilter>(InputFilterMinMax(2, 5))
        users_rv.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        vm.state.gameCreated.observe(this, Observer {
            Timber.d("game created? : $it")
            if (it) {
                if (navController().currentDestination?.id == R.id.create_game) {
                    navController().navigate(CreateGameFragmentDirections.actionCreateGameToListGames())
                }
            }
        })
    }

    inner class InputFilterMinMax(private var min: Int, private var max: Int) : InputFilter {

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(min, max, input))
                    return null
            } catch (nfe: NumberFormatException) {
            }

            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

    fun createGame() {
        val users = vm.state.users.value?.filter { it.checked }?.map { it.user.id }
        val countX = vm.state.countX.value?.toInt()
        val countY = vm.state.countY.value?.toInt()
        val title = vm.state.gameTitle.value

        if (users != null && (users.count() in 1..3) && countX != null && countY != null && title != null) {
            vm.createGame(title, countX, countY, users)
        } else {
            Toast.makeText(context, "Проверьте параметры игры", Toast.LENGTH_SHORT).show()
        }
    }
}