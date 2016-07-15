package im.actor.sdk.controllers.group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import im.actor.core.entity.GroupMember;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.group.view.MembersAdapter;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class MembersFragment extends BaseFragment {

    public static MembersFragment create(int groupId) {
        MembersFragment res = new MembersFragment();
        Bundle args = new Bundle();
        args.putInt("groupId", groupId);
        res.setArguments(args);
        return res;
    }

    private static final int LIMIT = 20;

    private int groupId;
    private boolean isInitiallyLoaded;
    private byte[] nextMembers;
    private ArrayList<Integer> members = new ArrayList<>();
    private MembersAdapter adapter;

    public MembersFragment() {
        setRootFragment(true);
        setTitle(R.string.group_members_header);
        setHomeAsUp(true);
        setShowHome(true);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        groupId = getArguments().getInt("groupId");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_members, container, false);
        adapter = new MembersAdapter(getContext());

        ((ListView) res.findViewById(R.id.items)).setAdapter(adapter);

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInitiallyLoaded) {
            wrap(messenger().loadMembers(groupId, LIMIT, nextMembers)).then(groupMembersSlice -> {
                isInitiallyLoaded = true;
                members.addAll(groupMembersSlice.getUids());
                nextMembers = groupMembersSlice.getNext();
                ArrayList<GroupMember> nMembers = new ArrayList<>();
                for (Integer uid : members) {
                    nMembers.add(new GroupMember(uid, 0, 0, false));
                }
                adapter.setMembers(nMembers, false);
            });
        }
    }
}
