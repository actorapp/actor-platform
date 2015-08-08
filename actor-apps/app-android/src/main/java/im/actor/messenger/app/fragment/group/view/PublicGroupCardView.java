package im.actor.messenger.app.fragment.group.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.core.entity.PublicGroup;

/**
 * Created by korka on 30.06.15.
 */
public class PublicGroupCardView extends LinearLayout {
    public static final int COUNTER_TYPE_FRIENDS = 0;
    public static final int COUNTER_TYPE_MEMBERS = 1;
    AvatarView avatarView;
    TextView title;
    TextView counter;

    public PublicGroupCardView(Context context) {
        super(context);
    }

    public PublicGroupCardView(Context context, PublicGroup data, int counterType) {
        super(context);
        setPadding(Screen.dp(8), 0, Screen.dp(8), 0);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        avatarView = new AvatarView(context);
        avatarView.init(Screen.dp(52), 24);
        avatarView.bind(data);
        addView(avatarView, new LinearLayout.LayoutParams(Screen.dp(58), Screen.dp(58)));

        title = new TextView(context);
        title.setText(data.getTitle());
        title.setGravity(Gravity.CENTER);
        title.setTextColor(context.getResources().getColor(R.color.text_secondary));
        title.setTextSize(15);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setMaxWidth(Screen.dp(100));
        addView(title, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        counter = new TextView(context);
        counter.setGravity(Gravity.CENTER);
        counter.setSingleLine();
        counter.setTextColor(context.getResources().getColor(R.color.text_secondary));
        counter.setTextSize(13);
        counter.setMaxWidth(Screen.dp(100));
        counter.setEllipsize(TextUtils.TruncateAt.END);
        String counterString;
        switch (counterType) {
            case COUNTER_TYPE_FRIENDS:
                counterString = context.getString(R.string.join_public_group_friends_count).concat(Integer.toString(data.getFriends()));
                break;

            case COUNTER_TYPE_MEMBERS:
                counterString = context.getString(R.string.join_public_group_members_count).concat(Integer.toString(data.getMembers()));
                break;

            default:
                counterString = "";
                break;
        }
        counter.setText(counterString);
        addView(counter, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }


}
