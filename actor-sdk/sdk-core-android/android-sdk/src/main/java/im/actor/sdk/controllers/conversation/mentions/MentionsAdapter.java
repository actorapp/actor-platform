package im.actor.sdk.controllers.conversation.mentions;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.MentionFilterResult;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.SearchHighlight;
import im.actor.sdk.view.adapters.ViewHolder;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class MentionsAdapter extends HolderAdapter<MentionFilterResult> {

    private int gid;
    private List<MentionFilterResult> membersToShow = new ArrayList<>();
    private String query;
    private int highlightColor;

    public MentionsAdapter(int gid, Context context) {
        super(context);
        highlightColor = context.getResources().getColor(R.color.primary);

        this.gid = gid;
    }

    public void clearQuery() {
        query = null;
        membersToShow = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setQuery(String q) {
        if (q == null || q.equals(query)) {
            return;
        }
        query = q;
        membersToShow = messenger().findMentions(gid, q);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return membersToShow.size();
    }

    @Override
    public MentionFilterResult getItem(int position) {
        return membersToShow.get(position);
    }

    @Override
    public long getItemId(int position) {
        return membersToShow.get(position).getUid();
    }

    @Override
    protected ViewHolder<MentionFilterResult> createHolder(MentionFilterResult obj) {
        return new GroupViewHolder();
    }

    private class GroupViewHolder extends ViewHolder<MentionFilterResult> {

        private MentionFilterResult data;
        private TextView userName;
        private TextView mentionHint;
        private AvatarView avatarView;

        @Override
        public View init(final MentionFilterResult data, ViewGroup viewGroup, Context context) {
            View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_chat_mention_item, viewGroup, false);
            res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
            res.findViewById(R.id.container).setBackgroundResource(R.drawable.selector);
            res.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

            userName = (TextView) res.findViewById(R.id.name);
            userName.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
            mentionHint = (TextView) res.findViewById(R.id.mentionHint);
            mentionHint.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
            avatarView = (AvatarView) res.findViewById(R.id.avatar);
            avatarView.init(Screen.dp(35), 16);
            this.data = data;

            return res;
        }

        @Override
        public void bind(MentionFilterResult data, int position, Context context) {
            UserVM user = users().get(data.getUid());
            this.data = data;
            avatarView.bind(user);
            CharSequence name = data.getMentionString();
            if (name != null && name.length() > 0 && data.getMentionMatches() != null) {
                name = SearchHighlight.highlightMentionsQuery((String) name, data.getMentionMatches(), highlightColor);
            }
            userName.setText(name);

            CharSequence hint = data.getOriginalString();
            if (hint != null && hint.length() > 0 && data.getOriginalMatches() != null) {
                hint = SearchHighlight.highlightMentionsQuery((String) hint, data.getOriginalMatches(), highlightColor);
            }

            mentionHint.setText(hint);
        }


        @Override
        public void unbind(boolean full) {
            if (full) {
                avatarView.unbind();
            }
        }
    }


}
