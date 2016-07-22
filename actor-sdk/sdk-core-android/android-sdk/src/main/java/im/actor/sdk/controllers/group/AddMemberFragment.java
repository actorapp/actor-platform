package im.actor.sdk.controllers.group;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import im.actor.core.entity.Contact;
import im.actor.core.entity.GroupMember;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.messages.Void;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.contacts.BaseContactFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class AddMemberFragment extends BaseContactFragment {

    public static AddMemberFragment create(int gid) {
        AddMemberFragment res = new AddMemberFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("GROUP_ID", gid);
        res.setArguments(arguments);
        return res;
    }

    private GroupVM groupVM;

    public AddMemberFragment() {
        super(true, true, false);
        setRootFragment(true);
        setTitle(R.string.group_add_title);
        setHomeAsUp(true);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        groupVM = messenger().getGroup(getArguments().getInt("GROUP_ID"));
    }

    @Override
    protected void addFootersAndHeaders() {

        if (groupVM.getIsCanInviteViaLink().get()) {
            addFooterOrHeaderAction(ActorSDK.sharedActor().style.getActionAddContactColor(), R.drawable.ic_person_add_white_24dp, R.string.contacts_invite_via_link, false, new Runnable() {
                @Override
                public void run() {
                    startActivity(Intents.inviteLink(getArguments().getInt("GROUP_ID", 0), getActivity()));
                }
            }, true);
        }
    }

    @Override
    public void onItemClicked(Contact contact) {

        final UserVM userModel = users().get(contact.getUid());

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
                        execute(messenger().inviteMember(groupVM.getId(), userModel.getId()),
                                R.string.progress_common, new CommandCallback<Void>() {
                                    @Override
                                    public void onResult(Void res) {
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