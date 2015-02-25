package im.actor.messenger.app.fragment.group;

import android.content.Context;

import java.util.Collection;

import im.actor.messenger.app.view.HolderAdapter;
import im.actor.model.entity.GroupMember;

/**
 * Created by ex3ndr on 07.10.14.
 */
public abstract class UserAdapter extends HolderAdapter<GroupMember> {
    private GroupMember[] members;

    public UserAdapter(Collection<GroupMember> members, Context context) {
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
}
