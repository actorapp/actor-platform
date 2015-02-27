package im.actor.messenger.app.fragment.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.droidkit.engine.uilist.UiListStateListener;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.*;
import im.actor.messenger.app.Intents;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.PeerType;
import im.actor.model.viewmodel.GroupVM;

import static im.actor.messenger.core.Core.groups;
import static im.actor.messenger.core.Core.messenger;

public class DialogsFragment extends BaseDialogFragment implements UiListStateListener {

    @Override
    protected boolean supportLongClick() {
        return true;
    }

    protected void onItemClick(Dialog item) {
        ((MainActivity) getActivity()).onDialogClicked(item);
    }

    protected void onItemLongClick(final Dialog dialog) {
        if (dialog.getPeer().getPeerType() == PeerType.PRIVATE) {
            new AlertDialog.Builder(getActivity())
                    .setItems(new CharSequence[]{
                            getString(R.string.dialogs_menu_contact_view),
                            getString(R.string.dialogs_menu_contact_rename),
                            getString(R.string.dialogs_menu_conversation_delete)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            if (which == 0) {
                                startActivity(Intents.openProfile(dialog.getPeer().getPeerId(), getActivity()));
                            } else if (which == 1) {
                                startActivity(Intents.editUserName(dialog.getPeer().getPeerId(), getActivity()));
                            } else if (which == 2) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.alert_delete_chat_message)
                                                .replace("{0}", dialog.getDialogTitle()))
                                        .setPositiveButton(R.string.alert_delete_chat_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog2, int which) {
                                                execute(messenger().clearChat(dialog.getPeer()), R.string.progress_common);
                                            }
                                        })
                                        .setNegativeButton(R.string.dialog_cancel, null)
                                        .show()
                                        .setCanceledOnTouchOutside(true);

                            }
                        }
                    })
                    .show()
                    .setCanceledOnTouchOutside(true);
        } else if (dialog.getPeer().getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = groups().get(dialog.getPeer().getPeerId());

            final boolean isMember = groupVM.isMember().get();
            new AlertDialog.Builder(getActivity())
                    .setItems(new CharSequence[]{
                            getString(R.string.dialogs_menu_group_view),
                            getString(R.string.dialogs_menu_group_rename),
                            isMember ? getString(R.string.dialogs_menu_group_leave)
                                    : getString(R.string.dialogs_menu_group_delete),
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            if (which == 0) {
                                startActivity(Intents.openGroup(dialog.getPeer().getPeerId(), getActivity()));
                            } else if (which == 1) {
                                startActivity(Intents.editGroupTitle(dialog.getPeer().getPeerId(), getActivity()));
                            } else if (which == 2) {
                                if (isMember) {
                                    new AlertDialog.Builder(getActivity())
                                            .setMessage(getString(R.string.alert_leave_group_message)
                                                    .replace("{0}", dialog.getDialogTitle()))
                                            .setPositiveButton(R.string.alert_leave_group_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog2, int which) {
                                                    execute(messenger().leaveGroup(dialog.getPeer().getPeerId()),
                                                            R.string.progress_common);
                                                }
                                            })
                                            .setNegativeButton(R.string.dialog_cancel, null)
                                            .show()
                                            .setCanceledOnTouchOutside(true);
                                } else {
                                    new AlertDialog.Builder(getActivity())
                                            .setMessage(getString(R.string.alert_delete_group_title)
                                                    .replace("{0}", dialog.getDialogTitle()))
                                            .setPositiveButton(R.string.alert_delete_group_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog2, int which) {
                                                    execute(messenger().clearChat(dialog.getPeer()), R.string.progress_common);
                                                }
                                            })
                                            .setNegativeButton(R.string.dialog_cancel, null)
                                            .show()
                                            .setCanceledOnTouchOutside(true);
                                }
                            }
                        }
                    })
                    .show()
                    .setCanceledOnTouchOutside(true);
        }
    }
}
