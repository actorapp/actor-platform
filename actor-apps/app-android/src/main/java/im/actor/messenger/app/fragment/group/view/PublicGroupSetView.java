package im.actor.messenger.app.fragment.group.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
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
    private static final int MAX_GROUPS_IN_SET_LANDSCAPE = 3;
    private static final int MAX_GROUPS_IN_SET_PORTRAIT = 5;
    PublicGroupSet data;
    TextView title;
    TextView subTitle;
    GroupClickListener callback;
    LinearLayout ll;
    HorizontalScrollView sv;
    LinearLayout groupsCards;
    int counterType;
    boolean divaiderEnabled = false;
    int titleColorResource = R.color.text_secondary;
    int subTitleColorResource = R.color.text_secondary;
    int backgroundColorResource = R.color.bg_backyard;
    Context context;

    public PublicGroupSetView(Context context) {
        super(context);
        this.context = context;
    }

    public PublicGroupSetView(Context context, PublicGroupSet data, int counterType) {
        super(context);
        this.context = context;
        this.data = data;
        setOrientation(VERTICAL);
        this.counterType = counterType;
        ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, Screen.dp(8), 0, Screen.dp(8));
        addView(ll);

        if (data.getTitle() != null && !data.getTitle().isEmpty()) {
            title = new TextView(context);
            title.setPadding(Screen.dp(15), 0, Screen.dp(15), 0);
            title.setText(data.getTitle());
            title.setTypeface(Fonts.medium());
            title.setTextSize(17);
            ll.addView(title);
        }

        if (data.getSubtitle() != null && !data.getSubtitle().isEmpty()) {
            subTitle = new TextView(context);
            subTitle.setPadding(Screen.dp(15), 0, Screen.dp(15), 0);
            subTitle.setText(data.getSubtitle());
            subTitle.setTextSize(15);
            ll.addView(subTitle);
        }
        drawCards(context.getResources().getConfiguration());

        if (divaiderEnabled) {
            View separator = new View(context);
            separator.setBackgroundColor(context.getResources().getColor(R.color.chats_divider));
            FrameLayout.LayoutParams divLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    context.getResources().getDimensionPixelSize(R.dimen.div_size));
            divLayoutParams.gravity = Gravity.BOTTOM;
            addView(separator, divLayoutParams);
        }

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

    @Override
    protected void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawCards(newConfig);

    }

    private void drawCards(android.content.res.Configuration config) {
        Context context = getContext();
        if (sv != null) {
            sv.removeAllViews();
            ll.removeView(sv);
        }
        if (groupsCards != null) {
            groupsCards.removeAllViews();
            ll.removeView(groupsCards);
        }
        if (data.getGroups() != null && data.getGroups().size() > 0) {
            if (groupsCards == null) {
                groupsCards = new LinearLayout(context);
                groupsCards.setPadding(0, Screen.dp(8), 0, 0);
            }
            boolean isPortrait = config.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT;
            boolean useScrollView = data.getGroups().size() > (isPortrait ? MAX_GROUPS_IN_SET_LANDSCAPE : MAX_GROUPS_IN_SET_PORTRAIT);

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
                groupsCards.addView(card, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            }

            if (useScrollView) {
                if (sv == null) {
                    sv = new HorizontalScrollView(context);
                    sv.setHorizontalScrollBarEnabled(false);
                    sv.setOverScrollMode(OVER_SCROLL_NEVER);
                }
                sv.addView(groupsCards);
                ll.addView(sv);
            } else {
                ll.addView(groupsCards);
            }

        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (ll != null)
            ll.setBackgroundColor(context.getResources().getColor(backgroundColorResource));
        if (subTitle != null)
            subTitle.setTextColor(context.getResources().getColor(subTitleColorResource));
        if (title != null) title.setTextColor(context.getResources().getColor(titleColorResource));

    }

    public void setBackgroundColorResource(int backgroundColorResource) {
        this.backgroundColorResource = backgroundColorResource;
        invalidate();
        requestLayout();
    }

    public void setSubTitleColorResource(int subTitleColorResource) {
        this.subTitleColorResource = subTitleColorResource;
        invalidate();
        requestLayout();
    }

    public void setTitleColorResource(int titleColorResource) {
        this.titleColorResource = titleColorResource;
        invalidate();
        requestLayout();
    }

}
