package im.actor.messenger.app.fragment.contacts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.app.view.SearchHighlight;
import im.actor.core.entity.Contact;

/**
 * Created by ex3ndr on 15.03.15.
 */
public class ContactHolder extends BindedViewHolder {

    private AvatarView avatar;
    private TextView title;

    private TextView fastTitle;

    private CheckBox isSelected;

    private FrameLayout cont;

    private boolean isSelectable;

    private OnItemClickedListener<Contact> onItemClickedListener;

    public ContactHolder(FrameLayout fl, boolean isSelectable, Context context, OnItemClickedListener<Contact> onItemClickedListener) {
        super(fl);

        this.onItemClickedListener = onItemClickedListener;
        this.isSelectable = isSelectable;

        int padding = Screen.dp(16);

        fl.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(64)));

        cont = new FrameLayout(context);
        cont.setBackgroundResource(R.drawable.selector_fill);
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.leftMargin = Screen.dp(40);
            fl.addView(cont, layoutParams);
        }

        View fastBg = new View(context);
        fastBg.setBackgroundColor(context.getResources().getColor(R.color.bg_main));
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(40), ViewGroup.LayoutParams.MATCH_PARENT);
            fl.addView(fastBg, layoutParams);
        }

        avatar = new AvatarView(context);
        avatar.init(52, 24);
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(52), Screen.dp(52));
            layoutParams.leftMargin = Screen.dp(6);
            layoutParams.topMargin = Screen.dp(6);
            layoutParams.bottomMargin = Screen.dp(6);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            cont.addView(avatar, layoutParams);
        }

        fastTitle = new TextView(context);
        fastTitle.setTextColor(context.getResources().getColor(R.color.contacts_fast));
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
        title.setTextColor(context.getResources().getColor(R.color.text_primary));
        title.setPadding(Screen.dp(72), 0, (isSelectable ? Screen.dp(64) : 0) + Screen.dp(8), 0);
        title.setTextSize(16);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setTypeface(Fonts.regular());
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.topMargin = padding;
            layoutParams.bottomMargin = padding;
            cont.addView(title, layoutParams);
        }

        if (isSelectable) {
            isSelected = new CheckBox(context);
            isSelected.setClickable(false);
            isSelected.setFocusable(false);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            layoutParams.rightMargin = padding;
            cont.addView(isSelected, layoutParams);
        }

        View div = new View(context);
        div.setBackgroundColor(context.getResources().getColor(R.color.contacts_divider));
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    context.getResources().getDimensionPixelSize(R.dimen.div_size));
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.leftMargin = Screen.dp(72);
            cont.addView(div, layoutParams);
        }
    }

    public void bind(final Contact data, String shortName, String query, boolean selected) {

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

        if (isSelectable) {
            isSelected.setChecked(selected);
        }

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
    }

    public void unbind() {
        avatar.unbind();
    }
}
