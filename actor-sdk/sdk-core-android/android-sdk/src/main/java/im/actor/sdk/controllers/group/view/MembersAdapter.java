package im.actor.sdk.controllers.group.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.ActorBinder;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.core.entity.GroupMember;
import im.actor.core.viewmodel.UserVM;

import static im.actor.sdk.util.ActorSDKMessenger.users;

public class MembersAdapter extends HolderAdapter<GroupMember> {

    private GroupMember[] members = new GroupMember[0];
    private ActorBinder BINDER = new ActorBinder();

    public MembersAdapter(Context context) {
        super(context);
    }

    public void setMembers(Collection<GroupMember> members) {
        this.members = members.toArray(new GroupMember[members.size()]);
        Arrays.sort(this.members, (a, b) -> {
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
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return members.length;
    }

    @Override
    public GroupMember getItem(int position) {
        return members[position];
    }

    @Override
    public long getItemId(int position) {
        return members[position].getUid();
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
            res.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
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
