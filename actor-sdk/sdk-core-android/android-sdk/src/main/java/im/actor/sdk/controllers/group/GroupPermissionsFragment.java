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
    private CheckBox canEditInfo;

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
            if (permissions.isMembersCanEditInfo() != canEditInfo.isChecked()) {
                permissions.setMembersCanEditInfo(canEditInfo.isChecked());
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