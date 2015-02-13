package im.actor.messenger.app.fragment.group;

import android.content.Context;
import im.actor.messenger.app.view.HolderAdapter;
import im.actor.messenger.model.UserModel;

import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 07.10.14.
 */
public abstract class UserAdapter extends HolderAdapter<UserModel> {
    private int[] uids;

    public UserAdapter(int[] uids, Context context) {
        super(context);
        this.uids = uids;
    }

    public void updateUid(int[] uids) {
        this.uids = uids;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return uids.length;
    }

    @Override
    public UserModel getItem(int position) {
        return users().get(uids[position]);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }
}
