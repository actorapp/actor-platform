package im.actor.messenger.app.fragment.group;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.core.entity.PublicGroup;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.util.TextUtils;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.HolderAdapter;
import im.actor.messenger.app.view.SearchHighlight;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.runtime.files.FileSystemReference;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by ex3ndr on 07.10.14.
 */
public class JoinPublicGroupAdapter extends HolderAdapter<PublicGroup> {
    private PublicGroup[] groupsToShow;
    private PublicGroup[] allGroups;
    private HashMap<String, PublicGroup> searchMap;
    private String query;
    private int highlightColor;

    public JoinPublicGroupAdapter(List<PublicGroup> groups, Context context) {
        super(context);
        highlightColor = context.getResources().getColor(R.color.primary);

        this.allGroups = groups.toArray(new PublicGroup[groups.size()]);
        this.groupsToShow = allGroups;
        searchMap = new HashMap<String, PublicGroup>();
        String groupTitle;
        for (PublicGroup m : groups) {
            groupTitle = m.getTitle();

            searchMap.put(groupTitle.toLowerCase(), m);

            searchMap.put(TextUtils.transliterate(groupTitle.toLowerCase()), m);
        }
    }


    public void updateGroups(Collection<PublicGroup> groups) {
        this.groupsToShow = groups.toArray(new PublicGroup[groups.size()]);
        notifyDataSetChanged();
    }

    public void setQuery(String q) {
        query = q;
        if (q.isEmpty()) {
            if (Arrays.equals(this.groupsToShow, allGroups)) {
                return;
            }
            this.groupsToShow = allGroups;
        } else {
            HashSet<PublicGroup> foundMembers = new HashSet<PublicGroup>();
            for (String s : searchMap.keySet()) {
                if (s.startsWith(q))
                    foundMembers.add(searchMap.get(s));
            }
            this.groupsToShow = foundMembers.toArray(new PublicGroup[foundMembers.size()]);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return groupsToShow.length;
    }

    @Override
    public PublicGroup getItem(int position) {
        return groupsToShow[position];
    }

    @Override
    public long getItemId(int position) {
        return groupsToShow[position].getId();
    }

    @Override
    protected ViewHolder<PublicGroup> createHolder(PublicGroup obj) {
        return new PublicGroupHolder();
    }

    private class PublicGroupHolder extends ViewHolder<PublicGroup> {

        private TextView title;
        private TextView description;
        private TextView membersCount;
        private TextView friendsCount;
        private TextView friendsCountText;
        private AvatarView avatarView;
        PublicGroup publicGroup;

        @Override
        public View init(final PublicGroup data, ViewGroup viewGroup, Context context) {
            View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_join_public_group_item, viewGroup, false);
            title = (TextView) res.findViewById(R.id.title);
            title.setTextColor(context.getResources().getColor(R.color.chats_title));
            title.setTypeface(Fonts.medium());
            title.setTextSize(17);
            title.setPadding(0, Screen.dp(1), 0, 0);
            title.setSingleLine();
            title.setEllipsize(android.text.TextUtils.TruncateAt.END);

            description = (TextView) res.findViewById(R.id.description);
            membersCount = (TextView) res.findViewById(R.id.membersCount);
            friendsCountText = (TextView) res.findViewById(R.id.friendsCountText);
            friendsCountText.setTypeface(Fonts.medium());
            friendsCount = (TextView) res.findViewById(R.id.friendsCount);
            friendsCount.setTypeface(Fonts.medium());

            avatarView = (AvatarView) res.findViewById(R.id.avatar);
            avatarView.init(Screen.dp(52), 24);
            publicGroup = data;

            return res;
        }

        @Override
        public void bind(PublicGroup data, int position, Context context) {

            publicGroup = data;
            avatarView.bind(data);
            CharSequence title = data.getTitle();
            if (query != null && !query.isEmpty()) {
                title = SearchHighlight.highlightMentionsQuery((String) title, query, highlightColor);
            }
            this.title.setText(title);
            this.description.setText(data.getDescription());
            this.description.setVisibility(data.getDescription().length() > 0 ? View.VISIBLE : View.GONE);
            this.membersCount.setText(Integer.toString(data.getMembers()));
            this.friendsCount.setText(Integer.toString(data.getFriends()));

            if (data.getAvatar() != null && data.getAvatar().getFullImage() != null) {
                messenger().bindFile(data.getAvatar().getFullImage().getFileReference(), true, new FileVMCallback() {
                    @Override
                    public void onNotDownloaded() {
                    }

                    @Override
                    public void onDownloading(float progress) {
                    }

                    @Override
                    public void onDownloaded(FileSystemReference reference) {

                    }
                });
            }

        }


        @Override
        public void unbind() {
            avatarView.unbind();
        }
    }


}
