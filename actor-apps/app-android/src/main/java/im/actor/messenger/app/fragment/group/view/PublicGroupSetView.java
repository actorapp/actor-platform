package im.actor.messenger.app.fragment.group.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;
import im.actor.model.entity.PublicGroup;

/**
 * Created by korka on 30.06.15.
 */
public class PublicGroupSetView extends FrameLayout {
    PublicGroupSet data;
    TextView title;
    TextView subTitle;
    GroupClickListener callback;

    public PublicGroupSetView(Context context) {
        super(context);
    }

    public PublicGroupSetView(Context context, PublicGroupSet data) {
        super(context);
        this.data = data;
        setPadding(Screen.dp(8), Screen.dp(8), Screen.dp(8), Screen.dp(8));

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        addView(ll);

        title = new TextView(context);
        title.setText(data.getTitle());
        title.setTextColor(context.getResources().getColor(R.color.text_primary));
        title.setTextSize(17);
        ll.addView(title);

        subTitle = new TextView(context);
        subTitle.setText(data.getSubtitle());
        ll.addView(subTitle);

        LinearLayout groupsCards = new LinearLayout(context);
        for (final PublicGroup group : data.getGroups()) {
            final PublicGroupCardView card = new PublicGroupCardView(context, group);
            card.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onClick(group);
                    }
                }
            });
            groupsCards.addView(card);
        }

        ll.addView(groupsCards);

        View separator = new View(context);
        separator.setBackgroundColor(context.getResources().getColor(R.color.chats_divider));
        FrameLayout.LayoutParams divLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelSize(R.dimen.div_size));
        divLayoutParams.gravity = Gravity.BOTTOM;
        ll.addView(separator, divLayoutParams);

    }

    public void setOnGroupClickListener(GroupClickListener callback) {
        this.callback = callback;
    }


    public interface GroupClickListener {
        void onClick(PublicGroup group);
    }
}
