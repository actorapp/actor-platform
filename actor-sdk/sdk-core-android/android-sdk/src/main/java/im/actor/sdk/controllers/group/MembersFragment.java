package im.actor.sdk.controllers.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.GroupType;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.controllers.group.view.MembersAdapter;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.DividerView;
import im.actor.sdk.view.adapters.RecyclerListView;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class MembersFragment extends BaseFragment {

    protected CircularProgressBar progressView;
    private LinearLayout footer;

    public static MembersFragment create(int groupId) {
        MembersFragment res = new MembersFragment();
        Bundle args = new Bundle();
        args.putInt("groupId", groupId);
        res.setArguments(args);
        return res;
    }

    private MembersAdapter adapter;

    public MembersFragment() {
        setRootFragment(true);
        setTitle(R.string.group_members_header);
        setHomeAsUp(true);
        setShowHome(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_members, container, false);
        int groupId = getArguments().getInt("groupId");
        adapter = new MembersAdapter(getContext(), groupId);

        GroupVM groupVM = groups().get(groupId);
        RecyclerListView list = (RecyclerListView) res.findViewById(R.id.items);


        Boolean canInvite = groupVM.getIsCanInviteMembers().get();
        Boolean canInviteViaLink = groupVM.getIsCanInviteViaLink().get();
        if (canInvite || canInviteViaLink) {
            LinearLayout header = new LinearLayout(getActivity());
            list.addHeaderView(header);

            if (canInvite) {
                TextView addMmemberTV = new TextView(getContext());
                addMmemberTV.setBackgroundResource(R.drawable.selector);
                addMmemberTV.setTextSize(16);
                addMmemberTV.setPadding(Screen.dp(72), 0, 0, 0);
                addMmemberTV.setGravity(Gravity.CENTER_VERTICAL);
                addMmemberTV.setText(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.channel_add_member : R.string.group_add_member);
                addMmemberTV.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
                addMmemberTV.setOnClickListener(view -> {
                    startActivity(new Intent(getActivity(), AddMemberActivity.class)
                            .putExtra(Intents.EXTRA_GROUP_ID, groupId));
                });

                header.addView(addMmemberTV, ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(58));
            }

            if (canInvite && canInviteViaLink) {
                header.addView(new DividerView(getActivity()), ViewGroup.LayoutParams.MATCH_PARENT, 1);
            }

            if (canInviteViaLink) {
                TextView shareLinkTV = new TextView(getContext());
                shareLinkTV.setBackgroundResource(R.drawable.selector);
                shareLinkTV.setTextSize(16);
                shareLinkTV.setPadding(Screen.dp(72), 0, 0, 0);
                shareLinkTV.setGravity(Gravity.CENTER_VERTICAL);
                shareLinkTV.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
                shareLinkTV.setText(R.string.invite_link_action_share);
                shareLinkTV.setOnClickListener(view -> Intents.inviteLink(groupId, getActivity()));

                header.addView(shareLinkTV, ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(58));
            }
        }

        footer = new LinearLayout(getActivity());
        footer.setVisibility(View.INVISIBLE);
        list.addFooterView(footer);
        CircularProgressBar botProgressView = new CircularProgressBar(getActivity());
        int padding = Screen.dp(16);
        botProgressView.setPadding(padding, padding, padding, padding);
        botProgressView.setIndeterminate(true);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Screen.dp(72), Screen.dp(72));
        params.gravity = Gravity.CENTER;
        FrameLayout cont = new FrameLayout(getActivity());
        cont.addView(botProgressView, params);
        footer.addView(cont, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item != null && item instanceof GroupMember) {
                GroupMember groupMember = (GroupMember) item;
                if (groupMember.getUid() != myUid()) {
                    UserVM userVM = users().get(groupMember.getUid());
                    if (userVM != null) {
                        startActivity(Intents.openPrivateDialog(userVM.getId(), true, getActivity()));
                    }
                }
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                if (item != null && item instanceof GroupMember) {
                    GroupMember groupMember = (GroupMember) item;
                    if (groupMember.getUid() != myUid()) {
                        UserVM userVM = users().get(groupMember.getUid());
                        if (userVM != null) {
                            adapter.onMemberClick(groupVM, userVM, groupMember.isAdministrator(), groupMember.getInviterUid() == myUid(), (BaseActivity) getActivity());
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        progressView = (CircularProgressBar) res.findViewById(R.id.loadingProgress);
        progressView.setIndeterminate(true);

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.initLoad(new MembersAdapter.LoadedCallback() {
            @Override
            public void onLoaded() {
                hideView(progressView);
                showView(footer);
            }

            @Override
            public void onLoadedToEnd() {
                hideView(footer);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.dispose();
    }
}
