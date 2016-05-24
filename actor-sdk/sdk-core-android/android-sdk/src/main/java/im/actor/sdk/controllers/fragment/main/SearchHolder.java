package im.actor.sdk.controllers.fragment.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.sdk.view.SearchHighlight;
import im.actor.core.entity.SearchEntity;

public class SearchHolder extends BindedViewHolder {

    private AvatarView avatar;
    private TextView title;
    private View separator;
    private SearchEntity entity;

    private int highlightColor;

    public SearchHolder(Context context, final OnItemClickedListener<SearchEntity> clickedListener) {
        super(new FrameLayout(context));

        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        highlightColor = context.getResources().getColor(R.color.primary);

        avatar = new AvatarView(context);
        avatar.init(Screen.dp(52), 22);
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(52), Screen.dp(52));
            layoutParams.leftMargin = Screen.dp(12);
            layoutParams.topMargin = Screen.dp(6);
            layoutParams.bottomMargin = Screen.dp(6);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            ((ViewGroup) itemView).addView(avatar, layoutParams);
        }

        title = new TextView(context);
        title.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        title.setPadding(Screen.dp(74), 0, Screen.dp(12), 0);
        title.setTextSize(16);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setTypeface(Fonts.regular());
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.topMargin = Screen.dp(10);
            layoutParams.bottomMargin = Screen.dp(10);
            ((ViewGroup) itemView).addView(title, layoutParams);
        }

        separator = new View(context);
        separator.setBackgroundColor(ActorSDK.sharedActor().style.getDialogsDividerColor());
        {
            FrameLayout.LayoutParams divLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    context.getResources().getDimensionPixelSize(R.dimen.div_size));
            divLayoutParams.leftMargin = Screen.dp(74);
            divLayoutParams.rightMargin = Screen.dp(12);
            divLayoutParams.gravity = Gravity.BOTTOM;
            ((ViewGroup) itemView).addView(separator, divLayoutParams);
        }

        itemView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.selector_fill));
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedListener.onClicked(entity);
            }
        });
    }

    public void bind(SearchEntity entity, String query, boolean isLast) {
        this.entity = entity;

        avatar.bind(entity.getAvatar(), entity.getTitle(), entity.getPeer().getPeerId());
        if (query != null) {
            title.setText(SearchHighlight.highlightQuery(entity.getTitle(), query, highlightColor));
        } else {
            title.setText(entity.getTitle());
        }

        if (isLast) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
    }
}
