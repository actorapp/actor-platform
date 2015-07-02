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
import im.actor.messenger.app.view.Fonts;
import im.actor.model.entity.PublicGroup;

/**
 * Created by korka on 30.06.15.
 */
public class PublicGroupSetView extends LinearLayout {
    PublicGroupSet data;
    TextView title;
    TextView subTitle;
    GroupClickListener callback;
    LinearLayout ll;

    public PublicGroupSetView(Context context) {
        super(context);
    }

    public PublicGroupSetView(Context context, PublicGroupSet data, int counterType) {
        super(context);
        this.data = data;
        setOrientation(VERTICAL);
        ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(Screen.dp(8), Screen.dp(8), Screen.dp(8), Screen.dp(8));
        addView(ll);

        if (data.getTitle() != null && !data.getTitle().isEmpty()) {
            title = new TextView(context);
            title.setPadding(Screen.dp(7), 0, 0, 0);
            title.setText(data.getTitle());
            title.setTextColor(context.getResources().getColor(R.color.chats_title));
            title.setTypeface(Fonts.medium());
            title.setTextSize(17);
            ll.addView(title);
        }

        if (data.getSubtitle() != null && !data.getSubtitle().isEmpty()) {
            subTitle = new TextView(context);
            subTitle.setPadding(Screen.dp(7), 0, 0, 0);
            subTitle.setTextColor(context.getResources().getColor(R.color.text_secondary));
            subTitle.setText(data.getSubtitle());
            subTitle.setTextSize(15);
            ll.addView(subTitle);
        }

        if (data.getGroups() != null && data.getGroups().size() > 0) {
            LinearLayout groupsCards = new LinearLayout(context);
            for (final PublicGroup group : data.getGroups()) {
                final PublicGroupCardView card = new PublicGroupCardView(context, group, counterType);
                card.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null) {
                            callback.onClick(group);
                        }
                    }
                });
                groupsCards.addView(card, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            }

            ll.addView(groupsCards);
        }


        View separator = new View(context);
        separator.setBackgroundColor(context.getResources().getColor(R.color.chats_divider));
        FrameLayout.LayoutParams divLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelSize(R.dimen.div_size));
        divLayoutParams.gravity = Gravity.BOTTOM;
        addView(separator, divLayoutParams);

    }

    public PublicGroupSetView addChain(View v) {
        addView(v);
        return this;
    }

    public void setOnGroupClickListener(GroupClickListener callback) {
        this.callback = callback;
    }


    public interface GroupClickListener {
        void onClick(PublicGroup group);
    }
}
