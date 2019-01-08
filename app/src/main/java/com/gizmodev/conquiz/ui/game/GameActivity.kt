package com.gizmodev.conquiz.ui.game

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.databinding.ActivityGameBinding
import com.gizmodev.conquiz.viewmodel.GameViewModel
import com.gizmodev.conquiz.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import javax.inject.Inject

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var viewModel: GameViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        AndroidInjection.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_game)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameViewModel::class.java)

        binding.viewModel = viewModel
    }
}
