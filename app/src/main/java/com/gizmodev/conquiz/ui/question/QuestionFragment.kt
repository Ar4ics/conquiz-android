package com.gizmodev.conquiz.ui.question

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DividerItemDecoration
import com.gizmodev.conquiz.BR
import com.gizmodev.conquiz.databinding.FragmentViewQuestionBinding
import com.gizmodev.conquiz.model.AnswerVariant
import com.gizmodev.conquiz.ui.core.AppDialogFragment
import kotlinx.android.synthetic.main.fragment_view_question.*
import me.tatarka.bindingcollectionadapter2.ItemBinding
import timber.log.Timber
import javax.inject.Inject




class QuestionFragment : AppDialogFragment(), OnVariantClickListener {

    override fun onVariantClick(answerVariant: AnswerVariant) {
        Timber.d("answerVariant = $answerVariant")
        vm.answerToQuestion(answerVariant)
    }

    @Inject
    lateinit var vm: QuestionViewModel

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        if (savedInstanceState != null) return
//        val question = QuestionFragmentArgs.fromBundle(arguments!!).question
//        vm.state.setQuestion(question)
//    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
////        dialog.setTitle("Вопрос")
//        dialog.hide()
//        return dialog
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = FragmentViewQuestionBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@QuestionFragment
                listener = this@QuestionFragment
                state = this@QuestionFragment.vm.state
                variantsbinding = ItemBinding
                    .of<AnswerVariant>(BR.variant, com.gizmodev.conquiz.R.layout.view_variant_item)
                    .bindExtra(BR.listener, this@QuestionFragment)
                executePendingBindings()
            }
            .root

        return view
    }

    override fun onResume() {
        super.onResume()
        setWindow()
    }

    private fun setWindow() {
        val window = dialog!!.window!!
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)

//        val window = dialog!!.window!!
//        val lp = WindowManager.LayoutParams()
//        lp.copyFrom(window.attributes)
//        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
//        lp.gravity = Gravity.BOTTOM
//        lp.horizontalMargin = 0.0f
//        lp.horizontalMargin = 0.0f
//        window.attributes = lp
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        variants.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        exact_answer.setOnKeyListener { v, keyCode, event ->
            Timber.d("$v - $keyCode - $event")
            if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        Timber.d("text = ${exact_answer.text}")
                        val answer = exact_answer.text.toString()
                        onVariantClick(AnswerVariant(title = "Ваш ответ: $answer", value = answer.toInt()))
                        exact_answer.text?.clear()
                    }
                }
            }
            return@setOnKeyListener false
        }
    }

}