package im.actor.sdk.controllers.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import im.actor.core.entity.GroupMember;
import im.actor.core.viewmodel.GroupVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.group.view.MembersAdapter;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.DividerView;
import im.actor.sdk.view.adapters.HeaderViewRecyclerAdapter;
import im.actor.sdk.view.adapters.RecyclerListView;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class MembersFragment extends BaseFragment {

    protected CircularProgressBar progressView;

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
                addMmemberTV.setText(R.string.group_add_member);
                addMmemberTV.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
                addMmemberTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getActivity(), AddMemberActivity.class)
                                .putExtra(Intents.EXTRA_GROUP_ID, groupId));
                    }
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
                shareLinkTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intents.inviteLink(groupId, getActivity());
                    }
                });

                header.addView(shareLinkTV, ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(58));
            }
        }

        list.setAdapter(adapter);

        progressView = (CircularProgressBar) res.findViewById(R.id.loadingProgress);
        progressView.setIndeterminate(true);

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.initLoad(() -> hideView(progressView));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.dispose();
    }
}
