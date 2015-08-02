package im.actor.messenger.app.fragment.dialogs

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import im.actor.android.view.BindedListAdapter
import im.actor.messenger.R
import im.actor.messenger.app.Intents
import im.actor.messenger.app.fragment.DisplayListFragment
import im.actor.messenger.app.util.Screen
import im.actor.messenger.app.view.Fonts
import im.actor.messenger.app.view.OnItemClickedListener
import im.actor.model.concurrency.CommandCallback
import im.actor.model.entity.Dialog
import im.actor.model.mvvm.BindedDisplayList
import im.actor.model.mvvm.ValueChangedListener
import im.actor.model.mvvm.ValueModel

import im.actor.messenger.app.core.Core.messenger
import org.jetbrains.anko.*

/**
 * Created by ex3ndr on 22.11.14.
 */
public abstract class BaseDialogFragment : DisplayListFragment<Dialog, DialogHolder>() {

    private var emptyDialogs: View? = null

    init {
        setRetainInstance(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val res = inflate(inflater, container, R.layout.fragment_dialogs, messenger().getDialogsGlobalList())

        // Footer
        var footer = UI {
            frameLayout {
                imageView(R.drawable.card_shadow_bottom) {
                    scaleType = ImageView.ScaleType.FIT_XY
                }.layoutParams(width = matchParent, height = dip(4))

                textView(R.string.dialogs_hint) {
                    textSize = 15f
                    typeface = Fonts.regular()
                    textColor = getResources().getColor(R.color.text_subheader)
                    gravity = Gravity.CENTER

                    paddingLeft = dip(16)
                    paddingTop = dip(8)
                    paddingRight = dip(16)
                }.layoutParams(width = matchParent, height = wrapContent)

                layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip(160))
            }
        }.toView()
        addFooterView(footer)

        // Header
        val header = UI {
            view() {
                backgroundColor = getActivity().getResources().getColor(R.color.bg_main)

                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip(2))
            }
        }.toView()
        addHeaderView(header)

        // Empty View
        emptyDialogs = res.findViewById(R.id.emptyDialogs)
        bind(messenger().getAppState().getIsDialogsEmpty(), object : ValueChangedListener<Boolean> {
            override fun onChanged(`val`: Boolean?, valueModel: ValueModel<Boolean>) {
                if (`val`!!) {
                    emptyDialogs!!.setVisibility(View.VISIBLE)
                } else {
                    emptyDialogs!!.setVisibility(View.GONE)
                }
            }
        })

        return res
    }

    override fun onCreateAdapter(displayList: BindedDisplayList<Dialog>, activity: Activity): BindedListAdapter<Dialog, DialogHolder> {
        return DialogsAdapter(displayList, object : OnItemClickedListener<Dialog> {
            override fun onClicked(item: Dialog) {
                onItemClick(item)
            }

            override fun onLongClicked(item: Dialog): Boolean {
                return onItemLongClick(item)
            }
        }, activity)
    }

    protected open fun onItemClick(item: Dialog) {
    }

    protected open fun onItemLongClick(dialog: Dialog): Boolean {
        return false
    }
}
