package im.actor.sdk.controllers.group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.core.entity.GroupType;
import im.actor.core.viewmodel.GroupVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.compose.CreateGroupActivity;
import im.actor.sdk.controllers.compose.GroupUsersFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class GroupTypeFragment extends BaseFragment {

    public static GroupTypeFragment create(int groupId, boolean isCreate) {
        Bundle bundle = new Bundle();
        bundle.putInt("groupId", groupId);
        bundle.putBoolean("isCreate", isCreate);
        GroupTypeFragment editFragment = new GroupTypeFragment();
        editFragment.setArguments(bundle);
        return editFragment;
    }

    private EditText publicShortName;
    private GroupVM groupVM;
    private boolean isPublic;
    private boolean isCreate;

    public GroupTypeFragment() {
        setRootFragment(true);
        setHomeAsUp(true);
        setShowHome(true);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        isCreate = getArguments().getBoolean("isCreate", false);
        groupVM = messenger().getGroup(getArguments().getInt("groupId"));
        setTitle(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.channel_title : R.string.group_title);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_edit_type, container, false);
        res.setBackgroundColor(style.getBackyardBackgroundColor());
        TextView publicTitle = (TextView) res.findViewById(R.id.publicTitle);
        publicTitle.setText(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.group_public_channel_title : R.string.group_public_group_title);
        publicTitle.setTextColor(style.getTextPrimaryColor());
        TextView publicDescription = (TextView) res.findViewById(R.id.publicDescription);
        publicDescription.setText(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.group_public_channel_text : R.string.group_public_group_text);
        publicDescription.setTextColor(style.getTextSecondaryColor());
        TextView privateTitle = (TextView) res.findViewById(R.id.privateTitle);
        privateTitle.setText(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.group_private_channel_title : R.string.group_private_group_title);
        privateTitle.setTextColor(style.getTextPrimaryColor());
        TextView privateDescription = (TextView) res.findViewById(R.id.privateDescription);
        privateDescription.setText(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.group_private_channel_text : R.string.group_private_group_text);
        privateDescription.setTextColor(style.getTextSecondaryColor());
        TextView publicLinkPrefix = (TextView) res.findViewById(R.id.publicLinkPrefix);
        publicLinkPrefix.setTextColor(style.getTextSecondaryColor());
        String prefix = ActorSDK.sharedActor().getGroupInvitePrefix();
        if (prefix == null) {
            prefix = "@";
        }
        publicLinkPrefix.setText(prefix);
        RadioButton publicRadio = (RadioButton) res.findViewById(R.id.publicRadio);
        RadioButton privateRadio = (RadioButton) res.findViewById(R.id.privateRadio);
        View publicSelector = res.findViewById(R.id.publicSelector);
        View privateSelector = res.findViewById(R.id.privateSelector);
        publicShortName = (EditText) res.findViewById(R.id.publicLink);
        View publicLinkContainer = res.findViewById(R.id.publicContainer);
        View publicShadowTop = res.findViewById(R.id.shadowTop);
        View publicShadowBottom = res.findViewById(R.id.shadowBottom);

        if (groupVM.getShortName().get() != null) {
            publicRadio.setChecked(true);
            privateRadio.setChecked(false);
            publicLinkContainer.setVisibility(View.VISIBLE);
            publicShadowTop.setVisibility(View.VISIBLE);
            publicShadowBottom.setVisibility(View.VISIBLE);
            publicShortName.setText(groupVM.getShortName().get());
            isPublic = true;
        } else {
            publicRadio.setChecked(false);
            privateRadio.setChecked(true);
            publicLinkContainer.setVisibility(View.GONE);
            publicShadowTop.setVisibility(View.GONE);
            publicShadowBottom.setVisibility(View.GONE);
            publicShortName.setText(null);
            isPublic = false;
        }
        View.OnClickListener publicClick = view -> {
            if (!isPublic) {
                isPublic = true;
                publicRadio.setChecked(true);
                privateRadio.setChecked(false);
                publicLinkContainer.setVisibility(View.VISIBLE);
                publicShadowTop.setVisibility(View.VISIBLE);
                publicShadowBottom.setVisibility(View.VISIBLE);
                publicShortName.setText(groupVM.getShortName().get());
            }
        };
        View.OnClickListener privateClick = view -> {
            if (isPublic) {
                isPublic = false;
                publicRadio.setChecked(false);
                privateRadio.setChecked(true);
                publicLinkContainer.setVisibility(View.GONE);
                publicShadowTop.setVisibility(View.GONE);
                publicShadowBottom.setVisibility(View.GONE);
                publicShortName.setText(null);
            }
        };
        publicRadio.setOnClickListener(publicClick);
        publicSelector.setOnClickListener(publicClick);
        privateRadio.setOnClickListener(privateClick);
        privateSelector.setOnClickListener(privateClick);

        return res;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.next, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.next) {
            if (isPublic) {
                String nShortName = publicShortName.getText().toString().trim();
                if (nShortName.length() == 0) {
                    Toast.makeText(getActivity(), R.string.group_edit_change_short_name_error, Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (nShortName.equals(groupVM.getShortName().get())) {
                    onEditShortNameSuccess();
                    return true;
                }
                execute(messenger().editGroupShortName(groupVM.getId(), nShortName).then(r -> {
                    onEditShortNameSuccess();
                }).failure(e -> {
                    Toast.makeText(getActivity(), R.string.group_edit_change_short_name_error, Toast.LENGTH_SHORT).show();
                }));
            } else {
                if (groupVM.getShortName().get() == null) {
                    onEditShortNameSuccess();
                    return true;
                } else {
                    execute(messenger().editGroupShortName(groupVM.getId(), null).then(r -> {
                        onEditShortNameSuccess();
                    }).failure(e -> {
                        Toast.makeText(getActivity(), R.string.group_edit_change_short_name_error, Toast.LENGTH_SHORT).show();
                    }));
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onEditShortNameSuccess() {
        if (isCreate) {
            ((CreateGroupActivity) getActivity()).showNextFragment(
                    GroupUsersFragment.createChannel(getArguments().getInt("groupId")), false);
        } else {
            finishActivity();
        }
    }
}
