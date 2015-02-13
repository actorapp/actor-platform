package im.actor.messenger.app.fragment.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.droidkit.engine.uilist.UiListStateListener;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.*;
import im.actor.messenger.app.fragment.dialogs.BaseDialogFragment;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.core.actors.chat.ChatActionsActor;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.storage.scheme.messages.DialogItem;


public class DialogsFragment extends BaseDialogFragment implements UiListStateListener {

    protected void onItemClick(DialogItem item) {
        ((MainActivity) getActivity()).onDialogClicked(item);
    }

    @Override
    protected boolean supportLongClick() {
        return true;
    }

    protected void onItemLongClick(final DialogItem dialog) {
        if (dialog.getType() == DialogType.TYPE_USER) {
            new AlertDialog.Builder(getActivity())
                    .setItems(new CharSequence[]{
                            getString(R.string.dialogs_menu_contact_view),
                            getString(R.string.dialogs_menu_contact_rename),
                            getString(R.string.dialogs_menu_conversation_delete)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            if (which == 0) {
                                // View contact
                                startActivity(Intents.openProfile(dialog.getId(), getActivity()));
                            } else if (which == 1) {
                                startActivity(Intents.editUserName(dialog.getId(), getActivity()));
                            } else if (which == 2) {
                                // Delete conversation
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.alert_delete_chat_message)
                                                .replace("{0}", dialog.getDialogTitle()))
                                        .setPositiveButton(R.string.alert_delete_chat_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog2, int which) {
                                                ChatActionsActor.actions().deleteChat(dialog.getType(), dialog.getId());
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
        } else if (dialog.getType() == DialogType.TYPE_GROUP) {
            new AlertDialog.Builder(getActivity())
                    .setItems(new CharSequence[]{
                            getString(R.string.dialogs_menu_group_view),
                            getString(R.string.dialogs_menu_group_rename),
                            getString(R.string.dialogs_menu_group_delete),
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            if (which == 0) {
                                startActivity(Intents.openGroup(dialog.getId(), getActivity()));
                            } else if (which == 1) {
                                startActivity(Intents.editGroupTitle(dialog.getId(), getActivity()));
                            } else if (which == 2) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.alert_delete_group_title)
                                                .replace("{0}", dialog.getDialogTitle()))
                                        .setPositiveButton(R.string.alert_delete_group_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog2, int which) {
                                                GroupsActor.groupUpdates().leaveChat(dialog.getId());
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
        }
    }
}
