package im.actor.messenger.app.fragment.group.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.model.entity.PublicGroup;

/**
 * Created by korka on 30.06.15.
 */
public class PublicGroupCardView extends CardView {
    public static final int COUNTER_TYPE_FRIENDS = 0;
    public static final int COUNTER_TYPE_MEMBERS = 1;
    private AvatarView avatarView;
    private TextView title;
    private TextView counter;
    private LinearLayout container;
    private LinearLayout.LayoutParams llParams;

    public PublicGroupCardView(Context context) {
        super(context);
    }

    public PublicGroupCardView(Context context, PublicGroup data, int counterType) {
        super(context);
        llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        container = new LinearLayout(context);
        addView(container);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.TOP);

        FrameLayout avatarContainer = new FrameLayout(context);
        avatarView = new AvatarView(context);
        avatarContainer.addView(avatarView, new FrameLayout.LayoutParams(Screen.dp(58), Screen.dp(58), Gravity.CENTER));
        avatarView.init(Screen.dp(58), 24);
        avatarView.bind(data);
        container.addView(avatarContainer, new LinearLayout.LayoutParams(Screen.dp(74), Screen.dp(74)));

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setGravity(Gravity.TOP | Gravity.LEFT);
        container.addView(textContainer, llParams);
        title = new TextView(context);
        title.setText(data.getTitle());
        title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        title.setTextColor(context.getResources().getColor(R.color.text_primary));
        title.setTextSize(15);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setMaxWidth(Screen.dp(300));
        title.setPadding(0, Screen.dp(8), Screen.dp(8), 0);
        textContainer.addView(title, llParams);

        counter = new TextView(context);
        counter.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        counter.setSingleLine();
        counter.setTextColor(context.getResources().getColor(R.color.text_secondary));
        counter.setTextSize(15);
        counter.setMaxWidth(Screen.dp(300));
        counter.setEllipsize(TextUtils.TruncateAt.END);
        counter.setPadding(0, 0, Screen.dp(8), 0);
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
        textContainer.addView(counter, llParams);
    }


}
