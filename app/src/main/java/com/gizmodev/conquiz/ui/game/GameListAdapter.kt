package com.gizmodev.conquiz.ui.game

import android.content.Intent
import androidx.recyclerview.widget.DiffUtil
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.utils.Constants.GAME_ID
import com.gizmodev.conquiz.utils.DataBindingAdapter
import com.gizmodev.conquiz.utils.DataBindingViewHolder

class GameListAdapter: DataBindingAdapter<Game>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder<Game>, position: Int) {
        super.onBindViewHolder(holder, position)

        val item = getItem(position)

        holder.itemView.setOnClickListener {
            // whatever you want!
            // you could add a Presenter/ViewModel to the Adapter constructor and call a method, for instance:
            // viewModel.onItemClicked()
            val intent = Intent(it.context, GameActivity::class.java)
            intent.putExtra(GAME_ID, item.id)
            it.context.startActivity(intent)
        }
    }

    override fun getItemViewType(position: Int) = R.layout.item_game
}