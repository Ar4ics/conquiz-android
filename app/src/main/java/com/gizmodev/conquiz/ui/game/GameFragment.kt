package com.gizmodev.conquiz.ui.game

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
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

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        if (savedInstanceState != null) return
//        val fm = fragmentManager ?: return
//        vm.gameHolder.question = vm.game?.current_question
//    }

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

        lm.spanCount = vm.game?.count_x ?: 0

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

        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_END
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 250.0f / displayMetrics.densityDpi
            }
        }

        vm.state.messages.observe(viewLifecycleOwner, Observer {
            val pos = it.count()
            if (pos > 0) {
                smoothScroller.targetPosition = pos - 1
                game_messages.layoutManager?.startSmoothScroll(smoothScroller)
            }
        })

        message_input.setOnKeyListener { v, keyCode, event ->
            Timber.d("$v - $keyCode - $event")
            if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        Timber.d("text = ${message_input.text}")
                        val message = message_input.text.toString().trim()
                        vm.sendMessage(message)
                        message_input.text?.clear()

                        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm?.hideSoftInputFromWindow(message_input.windowToken, 0)
                    }
                }
            }
            return@setOnKeyListener false
        }

        open_question.setOnClickListener {
            val q = vm.state.question.value
            if (q != null) {
                showDialog(q)
            }
        }

        vm.state.question.observe(viewLifecycleOwner, Observer {
            Timber.d("question=$it")
            if (it != null) {
                showDialog(it)
            } else {
                hideDialog()
            }
        })

        vm.state.answerResults.observe(viewLifecycleOwner, Observer {
            val prev = fragmentManager?.findFragmentByTag("question")
            if (prev != null) {
                val df = prev as QuestionFragment
                df.vm.state.setAnswerResults(it)
                df.dialog?.show()
            }
        })

        vm.state.competitiveAnswerResults.observe(viewLifecycleOwner, Observer {
            val prev = fragmentManager?.findFragmentByTag("question")
            if (prev != null) {
                val df = prev as QuestionFragment
                df.vm.state.setCompetitiveAnswerResults(it)
                df.dialog?.show()
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
        val fm = fragmentManager ?: return
        val prev = fm.findFragmentByTag("question")
        vm.gameHolder.question = question
        if (prev != null) {
            val df = prev as QuestionFragment
            df.vm.state.setQuestion(question)
            Timber.d("QuestionFragment refreshing")
            df.dialog?.show()
        } else {
            Timber.d("QuestionFragment creating")
            val questionFragment = QuestionFragment()
            questionFragment.show(fm, "question")
        }
    }
}
