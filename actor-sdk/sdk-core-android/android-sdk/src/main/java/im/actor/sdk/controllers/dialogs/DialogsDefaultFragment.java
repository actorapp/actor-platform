package im.actor.sdk.controllers.dialogs;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import im.actor.core.entity.Dialog;
import im.actor.core.entity.GroupType;
import im.actor.core.entity.PeerType;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.actors.messages.Void;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKLauncher;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class DialogsDefaultFragment extends BaseDialogFragment {

    @Override
    protected void onItemClick(Dialog item) {
        Activity activity = getActivity();
        if (activity != null) {
            startActivity(Intents.openDialog(item.getPeer(), false, activity));
        }
    }

    @Override
    protected boolean onItemLongClick(Dialog dialog) {
        if (dialog.getPeer().getPeerType() == PeerType.PRIVATE) {
            new AlertDialog.Builder(getActivity())
                    .setItems(new CharSequence[]{
                            getString(R.string.dialogs_menu_contact_view),
                            getString(R.string.dialogs_menu_contact_rename),
                            getString(R.string.dialogs_menu_conversation_delete)
                    }, (d, which) -> {
                        if (which == 0) {
                            // View profile
                            ActorSDKLauncher.startProfileActivity(getActivity(), dialog.getPeer().getPeerId());
                        } else if (which == 1) {
                            // Rename user
                            startActivity(Intents.editUserName(dialog.getPeer().getPeerId(), getActivity()));
                        } else if (which == 2) {
                            // Delete chat
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.alert_delete_chat_message, dialog.getDialogTitle()))
                                    .setNegativeButton(R.string.dialog_cancel, null)
                                    .setPositiveButton(R.string.alert_delete_chat_yes, (d1, which1) -> {
                                        execute(messenger().deleteChat(dialog.getPeer()), R.string.progress_common,
                                                new CommandCallback<Void>() {
                                                    @Override
                                                    public void onResult(Void res) {

                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        Toast.makeText(getActivity(), R.string.toast_unable_delete_chat, Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    })
                                    .show();
                        }
                    })
                    .show();

            return true;
        } else if (dialog.getPeer().getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = groups().get(dialog.getPeer().getPeerId());
            CharSequence[] items;
            int dialogs_menu_view = groupVM.getGroupType() == GroupType.CHANNEL ? R.string.dialogs_menu_channel_view : R.string.dialogs_menu_group_view;
            int dialogs_menu_rename = groupVM.getGroupType() == GroupType.CHANNEL ? R.string.dialogs_menu_channel_rename : R.string.dialogs_menu_group_rename;
            int dialogs_menu_leave = groupVM.getGroupType() == GroupType.CHANNEL ? R.string.dialogs_menu_channel_leave : R.string.dialogs_menu_group_leave;
            int dialogs_menu_delete = groupVM.getGroupType() == GroupType.CHANNEL ? R.string.dialogs_menu_channel_delete : R.string.dialogs_menu_group_delete;
            items = new CharSequence[]{
                    getString(dialogs_menu_view),
                    getString(dialogs_menu_rename),
                    getString(groupVM.getIsCanLeave().get() ? dialogs_menu_leave :
                            groupVM.getIsCanDelete().get() ? dialogs_menu_delete :
                                    dialogs_menu_leave),
            };
            new AlertDialog.Builder(getActivity())
                    .setItems(items, (d, which) -> {
                        if (which == 0) {
                            ActorSDK.sharedActor().startGroupInfoActivity(getActivity(), dialog.getPeer().getPeerId());
                        } else if (which == 1) {
                            startActivity(Intents.editGroupTitle(dialog.getPeer().getPeerId(), getActivity()));
                        } else if (which == 2) {
                            int alert_delete_title = groupVM.getGroupType() == GroupType.CHANNEL ? R.string.alert_delete_channel_title : R.string.alert_delete_group_title;
                            int alert_leave_message = groupVM.getGroupType() == GroupType.CHANNEL ? R.string.alert_leave_channel_message : R.string.alert_leave_group_message;
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(groupVM.getIsCanLeave().get() ? alert_leave_message :
                                            groupVM.getIsCanDelete().get() ? alert_delete_title :
                                                    alert_leave_message, dialog.getDialogTitle()))
                                    .setNegativeButton(R.string.dialog_cancel, null)
                                    .setPositiveButton(groupVM.getIsCanLeave().get() ? R.string.alert_leave_group_yes : R.string.alert_delete_group_yes, (d1, which1) -> {
                                        if (groupVM.getIsCanLeave().get()) {
                                            execute(messenger().leaveAndDeleteGroup(dialog.getPeer().getPeerId()), R.string.progress_common).failure(e -> {
                                                Toast.makeText(getActivity(), R.string.toast_unable_leave, Toast.LENGTH_LONG).show();
                                            });
                                        } else if (groupVM.getIsCanDelete().get()) {
                                            execute(messenger().deleteGroup(dialog.getPeer().getPeerId()), R.string.progress_common).failure(e -> {
                                                Toast.makeText(getActivity(), R.string.toast_unable_delete_chat, Toast.LENGTH_LONG).show();
                                            });
                                        } else {
                                            execute(messenger().deleteChat(dialog.getPeer()), R.string.progress_common, new CommandCallback<Void>() {
                                                @Override
                                                public void onResult(Void res) {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Toast.makeText(getActivity(), R.string.toast_unable_delete_chat, Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    })
                                    .show();
                        }
                    }).show();
            return true;
        }
        return false;
    }
}
