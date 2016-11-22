package im.actor.sdk.controllers.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.core.entity.GroupType;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.Intents;

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
        setTitle(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.channel_admin_title : R.string.group_admin_title);
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

        bind(groupVM.getShortName(), new ValueChangedListener<String>() {
            @Override
            public void onChanged(String val, Value<String> valueModel) {
                if (val == null) {
                    groupTypeValue.setText(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.channel_type_private : R.string.group_type_private);
                } else {
                    groupTypeValue.setText(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.channel_type_pubic : R.string.group_type_pubic);
                }
            }
        });

        if (groupVM.getIsCanEditAdministration().get()) {
            res.findViewById(R.id.groupTypeContainer).setOnClickListener(v -> {
                startActivity(new Intent(getContext(), GroupTypeActivity.class)
                        .putExtra(Intents.EXTRA_GROUP_ID, groupVM.getId()));
            });
        }

        // Share History
        View shareContainer = res.findViewById(R.id.shareHistoryContainer);
        TextView shareHint = (TextView) res.findViewById(R.id.shareHistoryHint);
        shareHint.setTextColor(style.getTextSecondaryColor());
        TextView shareHistory = (TextView) res.findViewById(R.id.shareHistory);
        shareHistory.setTextColor(style.getTextPrimaryColor());
        TextView shareHistoryValue = (TextView) res.findViewById(R.id.shareHistoryValue);
        shareHistoryValue.setTextColor(style.getListActionColor());
        if (groupVM.getGroupType() == GroupType.GROUP &&
                groupVM.getIsCanEditAdministration().get()) {
            bind(groupVM.getIsHistoryShared(), isShared -> {
                if (isShared) {
                    shareHistoryValue.setVisibility(View.VISIBLE);
                    shareHistory.setOnClickListener(null);
                    shareHistory.setClickable(false);
                } else {
                    shareHistoryValue.setVisibility(View.GONE);
                    shareHistory.setOnClickListener(v -> {
                        execute(messenger().shareHistory(groupVM.getId()));
                    });
                }
            });
        } else {
            // Hide for channels
            shareContainer.setVisibility(View.GONE);
            shareHint.setVisibility(View.GONE);
        }

        // Permissions
        TextView permissions = (TextView) res.findViewById(R.id.permissions);
        permissions.setTextColor(style.getTextPrimaryColor());
        TextView permissionsHint = (TextView) res.findViewById(R.id.permissionsHint);
        permissionsHint.setTextColor(style.getTextSecondaryColor());
        View permissionsDiv = res.findViewById(R.id.permissionsDiv);
        if (groupVM.getGroupType() == GroupType.CHANNEL) {
            permissionsHint.setText(R.string.channel_permissions_hint);
        } else {
            permissionsHint.setText(R.string.group_permissions_hint);
        }
        if (groupVM.getIsCanEditAdministration().get() && groupVM.getGroupType() == GroupType.GROUP) {
            permissions.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), GroupPermissionsActivity.class)
                        .putExtra(Intents.EXTRA_GROUP_ID, groupVM.getId()));
            });
        } else {
            permissions.setVisibility(View.GONE);
            permissionsDiv.setVisibility(View.GONE);
            permissionsHint.setVisibility(View.GONE);
        }

        // Group Deletion
        View deleteContainer = res.findViewById(R.id.deleteContainer);
        TextView delete = (TextView) res.findViewById(R.id.delete);
        delete.setTextColor(style.getTextDangerColor());
        TextView deleteHint = (TextView) res.findViewById(R.id.deleteHint);
        deleteHint.setTextColor(style.getTextSecondaryColor());

        if (groupVM.getGroupType() == GroupType.CHANNEL) {
            delete.setText(R.string.channel_delete);
            deleteHint.setText(R.string.channel_delete_hint);
        } else {
            delete.setText(R.string.group_delete);
            deleteHint.setText(R.string.group_delete_hint);
        }

        bind(groupVM.getIsCanLeave(), groupVM.getIsCanDelete(), (canLeave, canDelete) -> {
            if (canLeave || canDelete) {
                deleteContainer.setVisibility(View.VISIBLE);
                delete.setOnClickListener(v -> {
                    int alert_delete_title = groupVM.getGroupType() == GroupType.CHANNEL ? R.string.alert_delete_channel_title : R.string.alert_delete_group_title;
                    int alert_leave_message = groupVM.getGroupType() == GroupType.CHANNEL ? R.string.alert_leave_channel_message : R.string.alert_leave_group_message;
                    new AlertDialog.Builder(getActivity())
                            .setMessage(getString(groupVM.getIsCanLeave().get() ? alert_leave_message :
                                    groupVM.getIsCanDelete().get() ? alert_delete_title :
                                            alert_leave_message, groupVM.getName().get()))
                            .setNegativeButton(R.string.dialog_cancel, null)
                            .setPositiveButton(R.string.alert_delete_group_yes, (d1, which1) -> {
                                if (groupVM.getIsCanLeave().get()) {
                                    execute(messenger().leaveAndDeleteGroup(groupVM.getId()), R.string.progress_common)
                                            .then(aVoid -> ActorSDK.returnToRoot(getActivity()))
                                            .failure(e -> {
                                                Toast.makeText(getActivity(), R.string.toast_unable_leave, Toast.LENGTH_LONG).show();
                                            });
                                } else if (groupVM.getIsCanDelete().get()) {
                                    execute(messenger().deleteGroup(groupVM.getId()), R.string.progress_common)
                                            .then(aVoid -> ActorSDK.returnToRoot(getActivity()))
                                            .failure(e -> {
                                                Toast.makeText(getActivity(), R.string.toast_unable_delete_chat, Toast.LENGTH_LONG).show();
                                            });
                                }
                            })
                            .show();

                });

            } else {
                deleteContainer.setVisibility(View.GONE);
                delete.setOnClickListener(null);
                delete.setClickable(false);
            }
        });


        return res;
    }
}
