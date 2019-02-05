package com.gizmodev.conquiz.ui.core

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.gizmodev.conquiz.ui.core.rx.DisposeOnLifecycleFragment
import com.gizmodev.conquiz.ui.core.rx.LifecycleDisposables
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber

abstract class AppDialogFragment : DialogFragment(),
    DisposeOnLifecycleFragment {

    private var viewBinding: ViewDataBinding? = null

    override val lifecycleDisposables = LifecycleDisposables()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        Timber.d("onCreate: $this $savedInstanceState")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Timber.d("onCreateDialog: $this $savedInstanceState")
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = DataBindingUtil.getBinding(view)
        Timber.d("onViewCreated: $this $view $savedInstanceState")
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Timber.d("onDismiss: $this $dialog")
    }

    override fun onPause() {
        super<DialogFragment>.onPause()
        super<DisposeOnLifecycleFragment>.onPause()
        Timber.d("onPause: $this")
    }

    override fun onStop() {
        super<DialogFragment>.onStop()
        super<DisposeOnLifecycleFragment>.onStop()
        Timber.d("onStop: $this")
    }

    override fun onDestroyView() {
        super<DialogFragment>.onDestroyView()
        super<DisposeOnLifecycleFragment>.onDestroyView()
        viewBinding?.unbind()
        viewBinding = null
        Timber.d("onDestroyView: $this")
    }

    override fun onDestroy() {
        super<DialogFragment>.onDestroy()
        super<DisposeOnLifecycleFragment>.onDestroy()
        Timber.d("onDestroy: $this")
    }

    protected fun <B : ViewDataBinding> viewBinding(): B =
        @Suppress("UNCHECKED_CAST")
        requireNotNull(viewBinding as B) {
            "View is not a data binding layout"
        }

    protected fun navController(): NavController =
        requireNotNull(view?.findNavController()) {
            "NavController not found"
        }
}