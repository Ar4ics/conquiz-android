package com.gizmodev.conquiz.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.gizmodev.conquiz.BR
import com.gizmodev.conquiz.R
import com.gizmodev.conquiz.databinding.FragmentViewGameBinding
import com.gizmodev.conquiz.model.Box
import com.gizmodev.conquiz.model.Game
import com.gizmodev.conquiz.model.UserColor
import com.gizmodev.conquiz.ui.core.AppFragment
import com.gizmodev.conquiz.utils.PusherHelper
import com.pusher.client.channel.PrivateChannelEventListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_view_game.*
import me.tatarka.bindingcollectionadapter2.ItemBinding
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class GameFragment : AppFragment(), OnBoxClickListener {
    override fun onBoxClicked(box: Box) {
        vm.clickBox(box)
    }

    @Inject
    lateinit var vm: GameViewModel

    @Inject
    lateinit var pusherHelper: PusherHelper

    lateinit var game: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        game = GameFragmentArgs.fromBundle(arguments!!).game
        vm.loadGame(game)
        pusherHelper.getPusher()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Timber.d("Pusher loaded")

                    val events = pusherHelper.gameEvents.toTypedArray()

                    Timber.d("events: ${Arrays.toString(events)}")

                    val channel = it.subscribePrivate("private-game.${game.id}", object : PrivateChannelEventListener {
                        override fun onAuthenticationFailure(message: String, e: Exception) {
                            Timber.d("Authentication failure due to $message, exception was ${e.localizedMessage}")
                        }

                        override fun onSubscriptionSucceeded(channelName: String) {
                            Timber.d("Subscribed to channel $channelName")
                        }

                        override fun onEvent(channelName: String, eventName: String, data: String) {
                            Timber.d("Event $eventName on channel $channelName with data $data")
                        }
                    }, *events)
                },
                {
                    Timber.e(it, "Failed to load pusher")
                }
            )
            .untilDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentViewGameBinding.inflate(inflater, container, false)
            .apply {
                listener = this@GameFragment
                state = this@GameFragment.vm.state
                boxesbinding = ItemBinding
                    .of<Box>(BR.box, R.layout.view_box_item)
                    .bindExtra(BR.listener, this@GameFragment)
                usercolorsbinding = ItemBinding
                    .of<UserColor>(BR.user_color, R.layout.view_user_color_item)
                executePendingBindings()
            }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lm = boxes.layoutManager as GridLayoutManager
        lm.spanCount = game.count_x

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
    }
}
