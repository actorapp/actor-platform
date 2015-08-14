package im.actor.messenger.app.fragment.dialogs;

import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import im.actor.core.entity.Dialog;
import im.actor.core.entity.PeerType;
import im.actor.core.viewmodel.GroupVM;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.MainActivity;

import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.messenger;

public class DialogsFragment extends BaseDialogFragment {

    protected void onItemClick(Dialog item) {
        ((MainActivity) getActivity()).onDialogClicked(item);
    }

    protected boolean onItemLongClick(final Dialog dialog) {
        if (dialog.getPeer().getPeerType() == PeerType.PRIVATE) {
            new MaterialDialog.Builder(getActivity())
                    .items(new CharSequence[]{
                            getString(R.string.dialogs_menu_contact_view),
                            getString(R.string.dialogs_menu_contact_rename),
                            getString(R.string.dialogs_menu_conversation_delete)
                    })
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int which,
                                                CharSequence charSequence) {
                            if (which == 0) {

                                // View profile
                                startActivity(Intents.openProfile(dialog.getPeer().getPeerId(), getActivity()));

                            } else if (which == 1) {

                                // Rename user
                                startActivity(Intents.editUserName(dialog.getPeer().getPeerId(), getActivity()));

                            } else if (which == 2) {

                                // Delete chat
                                new MaterialDialog.Builder(getActivity())
                                        .content(R.string.alert_delete_chat_message, dialog.getDialogTitle())
                                        .positiveText(R.string.alert_delete_chat_yes)
                                        .negativeText(R.string.dialog_cancel)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog materialDialog1) {
                                                execute(messenger().deleteChat(dialog.getPeer()));
                                            }
                                        })
                                        .show();
                            }
                        }
                    })
                    .show();
            return true;
        } else if (dialog.getPeer().getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = groups().get(dialog.getPeer().getPeerId());
            final boolean isMember = groupVM.isMember().get();

            new MaterialDialog.Builder(getActivity())
                    .items(new CharSequence[]{
                            getString(R.string.dialogs_menu_group_view),
                            getString(R.string.dialogs_menu_group_rename),
                            isMember ? getString(R.string.dialogs_menu_group_leave)
                                    : getString(R.string.dialogs_menu_group_delete),
                    })
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int which,
                                                CharSequence charSequence) {
                            if (which == 0) {
                                startActivity(Intents.openGroup(dialog.getPeer().getPeerId(), getActivity()));
                            } else if (which == 1) {
                                startActivity(Intents.editGroupTitle(dialog.getPeer().getPeerId(), getActivity()));
                            } else if (which == 2) {
                                if (isMember) {
                                    new MaterialDialog.Builder(getActivity())
                                            .content(R.string.alert_leave_group_message, dialog.getDialogTitle())
                                            .positiveText(R.string.alert_leave_group_yes)
                                            .negativeText(R.string.dialog_cancel)
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog materialDialog1) {
                                                    execute(messenger().leaveGroup(dialog.getPeer().getPeerId()));
                                                }
                                            }).show();
                                } else {
                                    new MaterialDialog.Builder(getActivity())
                                            .content(R.string.alert_delete_group_title, dialog.getDialogTitle())
                                            .positiveText(R.string.alert_delete_group_yes)
                                            .negativeText(R.string.dialog_cancel)
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog materialDialog) {
                                                    execute(messenger().deleteChat(dialog.getPeer()));
                                                }
                                            })
                                            .show();
                                }
                            }
                        }
                    })
                    .show();
            return true;
        }

        return false;
    }
}