package im.actor.messenger.app.fragment.dialogs

import android.content.Context
import android.view.ViewGroup

import im.actor.android.view.BindedListAdapter
import im.actor.messenger.app.view.OnItemClickedListener
import im.actor.model.entity.Dialog
import im.actor.model.mvvm.BindedDisplayList

public class DialogsAdapter(displayList: BindedDisplayList<Dialog>, private val itemClicked: OnItemClickedListener<Dialog>, private val context: Context) : BindedListAdapter<Dialog, DialogHolder>(displayList) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DialogHolder {
        return DialogHolder(context, itemClicked)
    }

    override fun onBindViewHolder(dialogHolder: DialogHolder, index: Int, item: Dialog) {
        dialogHolder.bind(item, index == getItemCount() - 1)
    }

    override fun onViewRecycled(holder: DialogHolder?) {
        holder!!.unbind()
    }
}
