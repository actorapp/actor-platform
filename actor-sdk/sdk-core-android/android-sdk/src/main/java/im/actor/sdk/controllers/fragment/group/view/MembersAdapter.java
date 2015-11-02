package im.actor.sdk.controllers.fragment.group.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.core.entity.GroupMember;
import im.actor.core.viewmodel.UserVM;

import static im.actor.sdk.util.ActorSDKMessenger.users;

public class MembersAdapter extends HolderAdapter<GroupMember> {
    private GroupMember[] members;

    public MembersAdapter(Collection<GroupMember> members, Context context) {
        super(context);
        this.members = members.toArray(new GroupMember[0]);
    }

    public void updateUid(Collection<GroupMember> members) {
        this.members = members.toArray(new GroupMember[0]);
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

        @Override
        public View init(GroupMember data, ViewGroup viewGroup, Context context) {
            View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_group_item, viewGroup, false);
            userName = (TextView) res.findViewById(R.id.name);
            avatarView = (AvatarView) res.findViewById(R.id.avatar);
            avatarView.init(Screen.dp(42), 24);
            admin = res.findViewById(R.id.adminFlag);
            ((TextView) admin).setTextColor(ActorSDK.sharedActor().style.getGroupAdminColor());
            ((TextView) res.findViewById(R.id.name)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
            res.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
            return res;
        }

        @Override
        public void bind(GroupMember data, int position, Context context) {
            UserVM user = users().get(data.getUid());

            avatarView.bind(user);

            userName.setText(user.getName().get());

            if (data.isAdministrator()) {
                admin.setVisibility(View.VISIBLE);
            } else {
                admin.setVisibility(View.GONE);
            }
        }

        @Override
        public void unbind() {
            avatarView.unbind();
        }
    }
}
