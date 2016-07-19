package im.actor.sdk.controllers.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.core.entity.GroupType;
import im.actor.core.viewmodel.GroupVM;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.util.Fonts;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class GroupAdminFragment extends BaseFragment {

    public static GroupAdminFragment create(int groupId) {
        Bundle bundle = new Bundle();
        bundle.putInt("groupId", groupId);
        GroupAdminFragment editFragment = new GroupAdminFragment();
        editFragment.setArguments(bundle);
        return editFragment;
    }

    private GroupVM groupVM;

    public GroupAdminFragment() {
        setRootFragment(true);
        setHomeAsUp(true);
        setShowHome(true);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        groupVM = messenger().getGroup(getArguments().getInt("groupId"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_edit_admin, container, false);
        res.findViewById(R.id.rootContainer).setBackgroundColor(style.getBackyardBackgroundColor());

        // Group Type
        TextView groupTypeTitle = (TextView) res.findViewById(R.id.groupTypeTitle);
        TextView groupTypeValue = (TextView) res.findViewById(R.id.groupTypeValue);
        groupTypeTitle.setTextColor(style.getTextPrimaryColor());
        groupTypeValue.setTextColor(style.getListActionColor());
        if (groupVM.getGroupType() == GroupType.CHANNEL) {
            groupTypeTitle.setText(R.string.channel_type);
        } else {
            groupTypeTitle.setText(R.string.group_type);
        }
        if (groupVM.getShortName().get() == null) {
            groupTypeValue.setText(R.string.group_type_private);
        } else {
            groupTypeValue.setText(R.string.group_type_pubic);
        }
        if (groupVM.getIsCanEditShortName().get()) {
            res.findViewById(R.id.groupTypeContainer).setOnClickListener(v -> {
                startActivity(new Intent(getContext(), GroupTypeActivity.class)
                        .putExtra(Intents.EXTRA_GROUP_ID, groupVM.getId()));
            });
        }
        // Group Admins

        // Share History
        View shareContainer = res.findViewById(R.id.shareHistoryContainer);
        View shareHint = res.findViewById(R.id.shareHistoryHint);
        TextView shareHistory = (TextView) res.findViewById(R.id.shareHistory);
        shareHistory.setTextColor(style.getTextPrimaryColor());
        TextView shareHistoryValue = (TextView) res.findViewById(R.id.shareHistoryValue);
        shareHistoryValue.setTextColor(style.getListActionColor());
        shareHistoryValue.setTypeface(Fonts.medium());
        if (groupVM.getGroupType() == GroupType.GROUP) {
            bind(groupVM.getIsHistoryShared(), isShared -> {
                if (isShared) {
                    shareHistoryValue.setVisibility(View.VISIBLE);
                    shareHistory.setOnClickListener(null);
                } else {
                    shareHistoryValue.setVisibility(View.GONE);
                    shareHistory.setOnClickListener(v -> {
                        // TODO: Implement
                    });
                }
            });
        } else {
            // Hide for channels
            shareContainer.setVisibility(View.GONE);
            shareHint.setVisibility(View.GONE);
        }

        // Permissions
        View permissionsContainer = res.findViewById(R.id.permissionsContainer);
        TextView permissions = (TextView) res.findViewById(R.id.permissions);
        permissions.setTextColor(style.getTextPrimaryColor());
        if (groupVM.getIsCanEditAdministration().get()) {
            permissions.setOnClickListener(v -> {

            });
        } else {
            permissionsContainer.setVisibility(View.GONE);
        }

        // Group Deletion
        TextView delete = (TextView) res.findViewById(R.id.delete);
        delete.setTextColor(style.getTextDangerColor());
        if (groupVM.getGroupType() == GroupType.CHANNEL) {
            delete.setText(R.string.channel_delete);
        } else {
            delete.setText(R.string.group_delete);
        }
        delete.setOnClickListener(v -> {
            // TODO: Delete
        });

        return res;
    }
}
