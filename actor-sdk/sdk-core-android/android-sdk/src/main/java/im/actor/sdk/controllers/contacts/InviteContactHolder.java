package im.actor.sdk.controllers.contacts;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.core.entity.PhoneBookContact;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.sdk.view.SearchHighlight;
import im.actor.core.entity.Contact;

public class InviteContactHolder extends ViewHolder<PhoneBookContact> {

    public static final int TYPE_PHONE = 0;
    public static final int TYPE_EMAIL = 1;


    private AvatarView avatar;
    private TextView title;
    private TextView subtitle;

    private TextView fastTitle;

    private CheckBox isSelected;

    private FrameLayout cont;

    private FrameLayout fl;


    private OnItemClickedListener<PhoneBookContact> onItemClickedListener;
    private final View separator;

    public InviteContactHolder(FrameLayout fl, Context context, OnItemClickedListener<PhoneBookContact> onItemClickedListener) {

        this.fl = fl;
        this.onItemClickedListener = onItemClickedListener;

        int padding = Screen.dp(16);

        fl.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(64)));

        cont = new FrameLayout(context);
        cont.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        FrameLayout background = new FrameLayout(context);
        background.setBackgroundResource(R.drawable.selector_fill);
        cont.addView(background, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.leftMargin = Screen.dp(40);
            fl.addView(cont, layoutParams);
        }

        View fastBg = new View(context);
        fastBg.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(40), ViewGroup.LayoutParams.MATCH_PARENT);
            fl.addView(fastBg, layoutParams);
        }

        avatar = new AvatarView(context);
        avatar.init(52, 22);
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(52), Screen.dp(52));
            layoutParams.leftMargin = Screen.dp(6);
            layoutParams.topMargin = Screen.dp(6);
            layoutParams.bottomMargin = Screen.dp(6);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            cont.addView(avatar, layoutParams);
        }

        fastTitle = new TextView(context);
        fastTitle.setTextColor(ActorSDK.sharedActor().style.getContactFastTitleColor());
        fastTitle.setTextSize(18);
        fastTitle.setGravity(Gravity.CENTER);
        fastTitle.setTypeface(Fonts.medium());
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(40), ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = Screen.dp(6);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            fl.addView(fastTitle, layoutParams);
        }

        title = new TextView(context);
        title.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        title.setPadding(Screen.dp(72), 0, Screen.dp(64), 0);
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setTypeface(Fonts.regular());

        subtitle = new TextView(context);
        subtitle.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        subtitle.setPadding(Screen.dp(72), 0, Screen.dp(64), 0);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER_VERTICAL);
        subtitle.setSingleLine(true);
        subtitle.setEllipsize(TextUtils.TruncateAt.END);
        subtitle.setTypeface(Fonts.regular());

        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.topMargin = Screen.dp(12);
            layoutParams.bottomMargin = Screen.dp(12);

            LinearLayout ll = new LinearLayout(context);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(title, llp);
            ll.addView(subtitle, llp);

            cont.addView(ll, layoutParams);
        }

        isSelected = new CheckBox(context);
        isSelected.setClickable(false);
        isSelected.setFocusable(false);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        lp.rightMargin = padding;
        cont.addView(isSelected, lp);

        separator = new View(context);
        separator.setBackgroundColor(ActorSDK.sharedActor().style.getContactDividerColor());
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    context.getResources().getDimensionPixelSize(R.dimen.div_size));
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.leftMargin = Screen.dp(72);
            cont.addView(separator, layoutParams);
        }


    }

    public void bind(final PhoneBookContact data, String shortName, String query, boolean selected, int type, boolean isLast) {

        if (shortName == null) {
            fastTitle.setVisibility(View.GONE);
        } else {
            fastTitle.setVisibility(View.VISIBLE);
            fastTitle.setText(shortName);
        }

        avatar.bind(data);

        if (query.length() > 0) {
            title.setText(SearchHighlight.highlightQuery(data.getName(), query, 0xff0277bd));
        } else {
            title.setText(data.getName());
        }

        if ((type == TYPE_EMAIL && data.getEmails().size() > 0) || data.getPhones().size() == 0) {
            subtitle.setText(data.getEmails().get(0).getEmail());
        } else {
            subtitle.setText(Long.toString(data.getPhones().get(0).getNumber()));
        }

        isSelected.setChecked(selected);

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickedListener.onClicked(data);
            }
        });

        cont.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onItemClickedListener.onLongClicked(data);
            }
        });



        if (isLast) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View init(PhoneBookContact data, ViewGroup viewGroup, Context context) {
        return fl;
    }

    @Override
    public void bind(PhoneBookContact data, int position, Context context) {

    }

    public void unbind() {
        avatar.unbind();
    }
}
