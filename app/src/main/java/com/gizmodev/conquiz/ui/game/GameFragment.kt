package com.gizmodev.conquiz.ui.game

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gizmodev.conquiz.BR
import com.gizmodev.conquiz.databinding.FragmentViewGameBinding
import com.gizmodev.conquiz.model.Box
import com.gizmodev.conquiz.model.GameMessage
import com.gizmodev.conquiz.model.Question
import com.gizmodev.conquiz.model.UserColor
import com.gizmodev.conquiz.ui.core.AppFragment
import com.gizmodev.conquiz.ui.question.QuestionFragment
import kotlinx.android.synthetic.main.fragment_view_game.*
import me.tatarka.bindingcollectionadapter2.ItemBinding
import timber.log.Timber
import javax.inject.Inject




class GameFragment : AppFragment(), OnBoxClickListener {
    override fun onBoxClicked(box: Box) {
        vm.clickBox(box)
    }

    @Inject
    lateinit var vm: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("savedInstanceState = $savedInstanceState")
        if (savedInstanceState != null) return
        val game = GameFragmentArgs.fromBundle(arguments!!).game
        vm.loadGame(game)
        vm.loadPusher()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentViewGameBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@GameFragment
                listener = this@GameFragment
                state = this@GameFragment.vm.state
                boxesbinding = ItemBinding
                    .of<Box>(BR.box, com.gizmodev.conquiz.R.layout.view_box_item)
                    .bindExtra(BR.listener, this@GameFragment)

                usercolorsbinding = ItemBinding
                    .of<UserColor>(BR.user_color, com.gizmodev.conquiz.R.layout.view_user_color_item)

                messagesbinding = ItemBinding
                    .of<GameMessage>(BR.message, com.gizmodev.conquiz.R.layout.view_message_item)
                executePendingBindings()
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lm = boxes.layoutManager as GridLayoutManager

        Timber.d("view = $view, savedInstanceState = $savedInstanceState")

        lm.spanCount = vm.gameHolder.game?.count_x ?: 0

        user_colors.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        boxes.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        boxes.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL
            )
        )

        game_messages.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        game_messages.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                val pos = game_messages.adapter?.itemCount
                Timber.d("game_messages pos: $pos")

                if (pos != null && pos > 0) {
                    game_messages.smoothScrollToPosition(pos - 1)
                }
            }
        })

        open_question.setOnClickListener {
            val q = vm.state.question.value
            if (q != null) {
                showDialog(q)
            }
        }

        message_input.setOnKeyListener { v, keyCode, event ->
            Timber.d("$v - $keyCode - $event")
            if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        Timber.d("text = ${message_input.text}")
                        val message = message_input.text.toString().trim()
                        vm.sendMessage(message)
                        message_input.text?.clear()
                    }
                }
            }
            return@setOnKeyListener false
        }

        vm.state.question.observe(viewLifecycleOwner, Observer {
            Timber.d("question=$it")
            if (it != null) {
                if (!it.is_exact_answer) {
                    showDialog(it)
                }
            } else {
                hideDialog()
            }
        })

        vm.state.exactQuestion.observe(viewLifecycleOwner, Observer {
            Timber.d("exactQuestion=$it")
            if (it) {
                val q = vm.state.question.value
                if (q != null) {
                    showDialog(q)
                }
            }
        })

        vm.state.answerResults.observe(viewLifecycleOwner, Observer {
            val prev = fragmentManager?.findFragmentByTag("question")
            if (prev != null) {
                val df = prev as QuestionFragment
                df.vm.state.setAnswerResults(it)
            }
        })

        vm.state.competitiveAnswerResults.observe(viewLifecycleOwner, Observer {
            val prev = fragmentManager?.findFragmentByTag("question")
            if (prev != null) {
                val df = prev as QuestionFragment
                df.vm.state.setCompetitiveAnswerResults(it)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        val prev = fragmentManager?.findFragmentByTag("question")
        if (prev != null) {
            val df = prev as QuestionFragment
            Timber.d("QuestionFragment closing")
            df.dismiss()
        }
    }

    private fun hideDialog() {
        val prev = fragmentManager?.findFragmentByTag("question")
        if (prev != null) {
            val df = prev as QuestionFragment
            Timber.d("QuestionFragment hiding")
            df.dialog?.hide()
        }
    }

    private fun showDialog(question: Question) {
        val prev = fragmentManager?.findFragmentByTag("question")
        if (prev != null) {
            val df = prev as QuestionFragment
            df.vm.state.setQuestion(question)
            Timber.d("QuestionFragment refreshing")
            df.dialog?.show()
        } else {
            Timber.d("QuestionFragment creating")
            val questionFragment = QuestionFragment()
            val args = Bundle()
            args.putParcelable("question", question)
            questionFragment.arguments = args
            questionFragment.show(fragmentManager!!, "question")
        }
    }
}
