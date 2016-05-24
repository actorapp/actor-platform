package im.actor.sdk.controllers.settings;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.User;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;

import static im.actor.sdk.util.ActorSDKMessenger.users;

public class BlockedAdapter extends RecyclerView.Adapter<BlockedAdapter.BlockedUserHolder> {

    List<User> blockedList;
    OnBlockedClickListener onBlockedClickListener;

    public BlockedAdapter(List<User> blockedList, OnBlockedClickListener onBlockedClickListener) {
        this.blockedList = blockedList;
        this.onBlockedClickListener = onBlockedClickListener;
    }

    @Override
    public BlockedUserHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new BlockedUserHolder(((Activity) parent.getContext()).getLayoutInflater().inflate(R.layout.fragment_blocked_item, parent, false));

    }

    @Override
    public void onBindViewHolder(BlockedUserHolder holder, int position) {
        holder.bind(blockedList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return blockedList.size();
    }

    public void setBlockedList(List<User> newBlockedList) {
        List<User> blockedSearch = new ArrayList<User>(blockedList);
        for (int i = blockedSearch.size() - 1; i >= 0; i--) {
            User ou = blockedSearch.get(i);
            boolean contains = false;
            for (User u : newBlockedList) {
                if (u.getUid() == ou.getUid()) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                blockedList.remove(ou);
                notifyItemRemoved(i);
            }
        }

        blockedSearch = new ArrayList<User>(blockedList);
        for (User u : newBlockedList) {
            boolean contains = false;
            for (User uo : blockedSearch) {
                if (u.getUid() == uo.getUid()) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                blockedList.add(u);
                notifyItemInserted(getItemCount());
            }
        }




    }


    public class BlockedUserHolder extends RecyclerView.ViewHolder {

        private TextView userName;
        private TextView unblock;
        private AvatarView avatarView;
        private UserVM user;
        private View divider;
        private View footer;

        public BlockedUserHolder(View res) {
            super(res);
            res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
            userName = (TextView) res.findViewById(R.id.name);
            avatarView = (AvatarView) res.findViewById(R.id.avatar);
            avatarView.init(Screen.dp(42), 22);
            unblock = (TextView) res.findViewById(R.id.unblock);
            unblock.setTextColor(ActorSDK.sharedActor().style.getListActionColor());

            ((TextView) res.findViewById(R.id.name)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
            divider = res.findViewById(R.id.divider);
            divider.setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
            footer = res.findViewById(R.id.footer);
            footer.setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
            res.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBlockedClickListener.onClick(user);
                }
            });
        }

        public void bind(User data, int position) {
            user = users().get(data.getUid());
            ActorSDK.sharedActor().getMessenger().onUserVisible(data.getUid());

            avatarView.bind(user);

            userName.setText(user.getName().get());
            divider.setVisibility(position == getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);
            footer.setVisibility(!(position == getItemCount() - 1) ? View.GONE : View.VISIBLE);


        }

        public void unbind() {
            avatarView.unbind();
        }
    }

    @Override
    public void onViewRecycled(BlockedUserHolder holder) {
        holder.unbind();
    }

    public interface OnBlockedClickListener {
        void onClick(UserVM u);
    }
}
