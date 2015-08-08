package im.actor.messenger.app.fragment.dialogs

import android.view.View

import com.afollestad.materialdialogs.MaterialDialog

import im.actor.messenger.R
import im.actor.messenger.app.Intents
import im.actor.messenger.app.activity.MainActivity
import im.actor.core.entity.Dialog
import im.actor.core.entity.PeerType
import im.actor.core.viewmodel.GroupVM

import im.actor.messenger.app.core.Core.groups
import im.actor.messenger.app.core.Core.messenger

public class DialogsFragment : BaseDialogFragment() {

    override fun onItemClick(item: Dialog) {
        (getActivity() as MainActivity).onDialogClicked(item)
    }

    override fun onItemLongClick(dialog: Dialog): Boolean {
        if (dialog.getPeer().getPeerType() === PeerType.PRIVATE) {
            MaterialDialog.Builder(getActivity()).items(arrayOf<CharSequence>(getString(R.string.dialogs_menu_contact_view), getString(R.string.dialogs_menu_contact_rename), getString(R.string.dialogs_menu_conversation_delete))).itemsCallback(object : MaterialDialog.ListCallback {
                override fun onSelection(materialDialog: MaterialDialog, view: View, which: Int, charSequence: CharSequence) {
                    if (which == 0) {

                        // View profile
                        startActivity(Intents.openProfile(dialog.getPeer().getPeerId(), getActivity()))

                    } else if (which == 1) {

                        // Rename user
                        startActivity(Intents.editUserName(dialog.getPeer().getPeerId(), getActivity()))

                    } else if (which == 2) {

                        // Delete chat
                        MaterialDialog.Builder(getActivity()).content(R.string.alert_delete_chat_message, dialog.getDialogTitle()).positiveText(R.string.alert_delete_chat_yes).negativeText(R.string.dialog_cancel).callback(object : MaterialDialog.ButtonCallback() {
                            override fun onPositive(materialDialog1: MaterialDialog?) {
                                execute(messenger().deleteChat(dialog.getPeer()))
                            }
                        }).show()
                    }
                }
            }).show()
            return true
        } else if (dialog.getPeer().getPeerType() === PeerType.GROUP) {
            val groupVM = groups().get(dialog.getPeer().getPeerId().toLong())
            val isMember = groupVM.isMember().get()!!

            MaterialDialog.Builder(getActivity()).items(arrayOf<CharSequence>(getString(R.string.dialogs_menu_group_view), getString(R.string.dialogs_menu_group_rename), if (isMember)
                getString(R.string.dialogs_menu_group_leave)
            else
                getString(R.string.dialogs_menu_group_delete))).itemsCallback(object : MaterialDialog.ListCallback {
                override fun onSelection(materialDialog: MaterialDialog, view: View, which: Int, charSequence: CharSequence) {
                    if (which == 0) {
                        startActivity(Intents.openGroup(dialog.getPeer().getPeerId(), getActivity()))
                    } else if (which == 1) {
                        startActivity(Intents.editGroupTitle(dialog.getPeer().getPeerId(), getActivity()))
                    } else if (which == 2) {
                        if (isMember) {
                            MaterialDialog.Builder(getActivity()).content(R.string.alert_leave_group_message, dialog.getDialogTitle()).positiveText(R.string.alert_leave_group_yes).negativeText(R.string.dialog_cancel).callback(object : MaterialDialog.ButtonCallback() {
                                override fun onPositive(materialDialog1: MaterialDialog?) {
                                    execute(messenger().leaveGroup(dialog.getPeer().getPeerId()))
                                }
                            }).show()
                        } else {
                            MaterialDialog.Builder(getActivity()).content(R.string.alert_delete_group_title, dialog.getDialogTitle()).positiveText(R.string.alert_delete_group_yes).negativeText(R.string.dialog_cancel).callback(object : MaterialDialog.ButtonCallback() {
                                override fun onPositive(materialDialog: MaterialDialog?) {
                                    execute(messenger().deleteChat(dialog.getPeer()))
                                }
                            }).show()
                        }
                    }
                }
            }).show()
            return true
        }

        return false
    }
}
