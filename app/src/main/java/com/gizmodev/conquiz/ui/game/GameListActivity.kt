package com.gizmodev.conquiz.ui.game

import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.databinding.ActivityGameListBinding
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.viewmodel.GameListViewModel
import com.gizmodev.conquiz.viewmodel.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import javax.inject.Inject



class GameListActivity: AppCompatActivity() {
    private lateinit var binding: ActivityGameListBinding
    private lateinit var viewModel: GameListViewModel
    private var errorSnackbar: Snackbar? = null
    private val adapter = GameListAdapter()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_game_list)
        binding.gameList.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        binding.gameList.adapter = adapter
        val itemDecoration = DividerItemDecoration(this, HORIZONTAL)
        binding.gameList.addItemDecoration(itemDecoration)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameListViewModel::class.java)
        viewModel.errorMessage.observe(this, Observer {
                errorMessage -> if(errorMessage != null) showError(errorMessage) else hideError()
        })
        viewModel.gameList.observe(this, Observer {
                setGames(it)
        })

        binding.viewModel = viewModel
    }

    private fun showError(@StringRes errorMessage:Int){
        errorSnackbar = Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_INDEFINITE)
        errorSnackbar?.setAction(R.string.retry, viewModel.errorClickListener)
        errorSnackbar?.show()
    }

    private fun setGames(games: List<Game>){
        adapter.submitList(games)
    }

    private fun hideError(){
        errorSnackbar?.dismiss()
    }
}