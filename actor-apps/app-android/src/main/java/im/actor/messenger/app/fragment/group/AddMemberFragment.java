package im.actor.messenger.app.fragment.group;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import im.actor.core.entity.Contact;
import im.actor.core.entity.GroupMember;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.contacts.BaseContactFragment;

import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.users;

public class AddMemberFragment extends BaseContactFragment {

    public static AddMemberFragment create(int gid) {
        AddMemberFragment res = new AddMemberFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("GROUP_ID", gid);
        res.setArguments(arguments);
        return res;
    }

    public AddMemberFragment() {
        super(true, true, false);
    }

    @Override
    protected void addFootersAndHeaders() {
       addFooterOrHeaderAction(R.color.contacts_action_add, R.drawable.ic_person_add_white_24dp, R.string.contacts_invite_via_link, false, new Runnable() {
            @Override
            public void run() {
                startActivity(Intents.inviteLink(getArguments().getInt("GROUP_ID", 0), getActivity()));
            }
        }, true);
    }

    @Override
    public void onItemClicked(Contact contact) {
        final int gid = getArguments().getInt("GROUP_ID");

        final UserVM userModel = users().get(contact.getUid());
        final GroupVM groupVM = groups().get(gid);

        for (GroupMember uid : groupVM.getMembers().get()) {
            if (uid.getUid() == userModel.getId()) {
                Toast.makeText(getActivity(), R.string.toast_already_member, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.alert_group_add_text).replace("{0}", userModel.getName().get()))
                .setPositiveButton(R.string.alert_group_add_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog2, int which) {
                        execute(messenger().inviteMember(gid, userModel.getId()),
                                R.string.progress_common, new CommandCallback<Boolean>() {
                                    @Override
                                    public void onResult(Boolean res) {
                                        getActivity().finish();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(getActivity(), R.string.toast_unable_add, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show()
                .setCanceledOnTouchOutside(true);
    }
}