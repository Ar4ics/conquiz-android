package com.gizmodev.conquiz.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.gizmodev.conquiz.BR
import com.gizmodev.conquiz.databinding.FragmentListGamesBinding
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.ui.core.AppFragment
import kotlinx.android.synthetic.main.fragment_list_games.*
import me.tatarka.bindingcollectionadapter2.ItemBinding
import javax.inject.Inject

class GameListFragment : AppFragment(),
    OnGameClickListener {

    @Inject
    lateinit var vm: GameListViewModel

    override fun onStart() {
        super.onStart()
        vm.list()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentListGamesBinding.inflate(inflater, container, false)
            .apply {
                listener = this@GameListFragment
                state = this@GameListFragment.vm.state
                gamesbinding = ItemBinding
                    .of<Game>(BR.game, com.gizmodev.conquiz.R.layout.view_game_item)
                    .bindExtra(BR.listener, this@GameListFragment)
                executePendingBindings()
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        games.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

    }

    override fun onGameClicked(game: Game) {
        navController().navigate(GameListFragmentDirections.ActionListGamesToViewGame(game))
    }
}