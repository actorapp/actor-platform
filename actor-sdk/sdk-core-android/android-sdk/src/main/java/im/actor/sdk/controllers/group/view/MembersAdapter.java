package im.actor.sdk.controllers.group.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.ActorBinder;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.core.entity.GroupMember;
import im.actor.core.viewmodel.UserVM;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class MembersAdapter extends HolderAdapter<GroupMember> {

    public static final int LOAD_GAP = 10;
    private static final int LIMIT = 20;
    private ArrayList<GroupMember> members = new ArrayList<GroupMember>();
    private ActorBinder BINDER = new ActorBinder();
    private boolean loadInProgress = false;
    private boolean loaddedToEnd = false;

    public MembersAdapter(Context context, int groupId) {
        super(context);
        this.groupId = groupId;
    }

    public void setMembers(Collection<GroupMember> members) {
        setMembers(members, true);
    }

    public void setMembers(Collection<GroupMember> members, boolean sort) {
        if (sort) {
            GroupMember[] membersArray = members.toArray(new GroupMember[members.size()]);
            Arrays.sort(membersArray, (a, b) -> {
                if (a.isAdministrator() && !b.isAdministrator()) {
                    return -1;
                }
                if (b.isAdministrator() && !a.isAdministrator()) {
                    return 1;
                }
                String an = users().get(a.getInviterUid()).getName().get();
                String bn = users().get(b.getInviterUid()).getName().get();
                return an.compareTo(bn);
            });
            this.members.addAll(Arrays.asList(membersArray));
        } else {
            this.members.addAll(members);
        }
        notifyDataSetChanged();
    }

    @Override
    protected void onBindViewHolder(ViewHolder<GroupMember> holder, GroupMember obj, int position, Context context) {
        super.onBindViewHolder(holder, obj, position, context);
        if (position >= getCount() - LOAD_GAP) {
            loadMore();
        }

    }

    private int groupId;
    private boolean isInitiallyLoaded;
    private byte[] nextMembers;
    private ArrayList<Integer> rawMembers = new ArrayList<>();

    public void initLoad() {
        if (!isInitiallyLoaded) {
            loadMore();
        }
    }

    private void loadMore() {
        if (!loadInProgress && !loaddedToEnd) {
            loadInProgress = true;
            messenger().loadMembers(groupId, LIMIT, nextMembers).then(groupMembersSlice -> {
                isInitiallyLoaded = true;
                rawMembers.clear();
                rawMembers.addAll(groupMembersSlice.getUids());
                nextMembers = groupMembersSlice.getNext();
                loaddedToEnd = nextMembers == null;
                ArrayList<GroupMember> nMembers = new ArrayList<>();
                for (Integer uid : rawMembers) {
                    nMembers.add(new GroupMember(uid, 0, 0, false));
                }
                loadInProgress = false;
                setMembers(nMembers, false);
            });
        }
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public GroupMember getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return members.get(position).getUid();
    }

    @Override
    protected ViewHolder<GroupMember> createHolder(GroupMember obj) {
        return new GroupViewHolder();
    }

    private class GroupViewHolder extends ViewHolder<GroupMember> {

        private TextView userName;
        private View admin;
        private AvatarView avatarView;
        private TextView online;
        private ActorBinder.Binding onlineBinding;
        private UserVM user;

        @Override
        public View init(GroupMember data, ViewGroup viewGroup, Context context) {
            View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_group_item, viewGroup, false);
            userName = (TextView) res.findViewById(R.id.name);
            avatarView = (AvatarView) res.findViewById(R.id.avatar);
            avatarView.init(Screen.dp(42), 18);
            admin = res.findViewById(R.id.adminFlag);
            online = (TextView) res.findViewById(R.id.online);
            ((TextView) admin).setTextColor(ActorSDK.sharedActor().style.getGroupAdminColor());
            ((TextView) res.findViewById(R.id.name)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
            // res.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
            return res;
        }

        @Override
        public void bind(GroupMember data, int position, Context context) {
            user = users().get(data.getUid());
            ActorSDK.sharedActor().getMessenger().onUserVisible(data.getUid());
            onlineBinding = BINDER.bindOnline(online, user);

            avatarView.bind(user);

            userName.setText(user.getName().get());

            if (data.isAdministrator()) {
                admin.setVisibility(View.VISIBLE);
            } else {
                admin.setVisibility(View.GONE);
            }
        }

        @Override
        public void unbind(boolean full) {
            if (full) {
                avatarView.unbind();
            }
            if (onlineBinding != null) {
                BINDER.unbind(onlineBinding);
            }
        }

    }

    @Override
    public void dispose() {
        super.dispose();
        BINDER.unbindAll();
    }
}
