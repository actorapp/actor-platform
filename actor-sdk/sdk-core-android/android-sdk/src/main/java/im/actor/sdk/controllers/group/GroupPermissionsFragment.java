package im.actor.sdk.controllers.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import im.actor.core.entity.GroupPermissions;
import im.actor.core.entity.GroupType;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class GroupPermissionsFragment extends BaseFragment {

    public static GroupPermissionsFragment create(int chatId) {
        Bundle args = new Bundle();
        args.putInt("groupId", chatId);
        GroupPermissionsFragment res = new GroupPermissionsFragment();
        res.setArguments(args);
        return res;
    }

    private GroupPermissions permissions;
    private int groupId;

    private CircularProgressBar progress;
    private View scrollContainer;

    private TextView canEditInfoTV;
    private CheckBox canEditInfo;

    private TextView canAdminsEditInfoTV;
    private CheckBox canAdminsEditInfo;

    private TextView canSendInvitationsTV;
    private CheckBox canSendInvitations;

    private TextView showLeaveJoinTV;
    private CheckBox showLeaveJoin;

    private TextView showAdminsToMembersTV;
    private CheckBox showAdminsToMembers;

    boolean isChannel = false;

    public GroupPermissionsFragment() {
        setRootFragment(true);
        setHomeAsUp(true);
        setShowHome(true);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        groupId = getArguments().getInt("groupId");
        if (messenger().getGroup(groupId).getGroupType() == GroupType.CHANNEL) {
            setTitle(R.string.channel_admin_title);
            isChannel = true;
        } else {
            setTitle(R.string.group_admin_title);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_edit_permissions, container, false);
        View rootContainer = res.findViewById(R.id.rootContainer);
        rootContainer.setBackgroundColor(style.getBackyardBackgroundColor());

        canEditInfo = (CheckBox) res.findViewById(R.id.canEditValue);
        canEditInfoTV = (TextView) res.findViewById(R.id.canEditTitle);
        canEditInfoTV.setText(isChannel ? R.string.channel_can_edit_info_members : R.string.group_can_edit_info_members);

        canAdminsEditInfo = (CheckBox) res.findViewById(R.id.canAdminsEditValue);
        canAdminsEditInfoTV = (TextView) res.findViewById(R.id.canAdminsEditTitle);
        canAdminsEditInfoTV.setText(isChannel ? R.string.channel_can_edit_info_admins : R.string.group_can_edit_info_admins);

        canSendInvitations = (CheckBox) res.findViewById(R.id.canMembersInviteValue);
        canSendInvitationsTV = (TextView) res.findViewById(R.id.canMembersInviteTitle);
        canSendInvitationsTV.setText(isChannel ? R.string.channel_can_invite_members : R.string.group_can_invite_members);

        if (!isChannel) {
            showLeaveJoin = (CheckBox) res.findViewById(R.id.showJoinLeaveValue);
            showLeaveJoinTV = (TextView) res.findViewById(R.id.showJoinLeaveTitle);
            showLeaveJoinTV.setText(isChannel ? R.string.channel_show_leave_join : R.string.group_show_leave_join);
        } else {
            res.findViewById(R.id.showJoinLeaveContainer).setVisibility(View.GONE);
        }

        showAdminsToMembers = (CheckBox) res.findViewById(R.id.showAdminsToMembersValue);
        showAdminsToMembersTV = (TextView) res.findViewById(R.id.showAdminsToMembersTitle);
        showAdminsToMembersTV.setText(isChannel ? R.string.channel_show_admin_to_members : R.string.group_show_admin_to_members);

        scrollContainer = res.findViewById(R.id.scrollContainer);
        progress = (CircularProgressBar) res.findViewById(R.id.progress);
        progress.setIndeterminate(true);
        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (permissions == null) {
            progress.setVisibility(View.VISIBLE);
            scrollContainer.setVisibility(View.GONE);
            wrap(messenger().loadGroupPermissions(groupId)).then(r -> {
                goneView(progress);
                showView(scrollContainer);
                this.permissions = r;
                bindView();
            });
        } else {
            progress.setVisibility(View.GONE);
            scrollContainer.setVisibility(View.VISIBLE);
        }
    }

    public void bindView() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
        canEditInfo.setChecked(permissions.isMembersCanEditInfo());
        canAdminsEditInfo.setChecked(permissions.isAdminsCanEditGroupInfo());
        canSendInvitations.setChecked(permissions.isMembersCanInvite());
        showLeaveJoin.setChecked(permissions.isShowJoinLeaveMessages());
        showAdminsToMembers.setChecked(permissions.isShowAdminsToMembers());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (permissions != null) {
            inflater.inflate(R.menu.next, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.next) {
            if (permissions.isMembersCanEditInfo() != canEditInfo.isChecked() ||
                    permissions.isAdminsCanEditGroupInfo() != canAdminsEditInfo.isChecked() ||
                    permissions.isMembersCanInvite() != canSendInvitations.isChecked() ||
                    permissions.isShowJoinLeaveMessages() != showLeaveJoin.isChecked() ||
                    permissions.isShowAdminsToMembers() != showAdminsToMembers.isChecked()) {
                permissions.setMembersCanEditInfo(canEditInfo.isChecked());
                permissions.setAdminsCanEditGroupInfo(canAdminsEditInfo.isChecked());
                permissions.setShowJoinLeaveMessages(showLeaveJoin.isChecked());
                permissions.setMembersCanInvite(canSendInvitations.isChecked());
                permissions.setShowAdminsToMembers(showAdminsToMembers.isChecked());
                execute(messenger().saveGroupPermissions(groupId, permissions).then(r -> {
                    finishActivity();
                }));
            } else {
                finishActivity();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}