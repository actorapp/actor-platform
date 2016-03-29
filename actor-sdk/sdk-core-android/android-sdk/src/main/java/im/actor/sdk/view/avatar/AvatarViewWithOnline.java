package im.actor.sdk.view.avatar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;

public class AvatarViewWithOnline extends FrameLayout {
    public static final boolean DRAW_ONLINES = false;
    AvatarView avatarView;
    ImageView online;
    private boolean isOnline = false;

    public AvatarViewWithOnline(Context context) {
        super(context);
    }

    public AvatarViewWithOnline(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarViewWithOnline(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void init(int size, float text) {
        avatarView = new AvatarView(getContext());

        avatarView.init(size, text);
        addView(avatarView);

        online = new ImageView(getContext());
        online.setImageResource(R.drawable.indicator_offline);
        online.setVisibility(INVISIBLE);
        addView(online, new LayoutParams(Screen.dp(11), Screen.dp(11), Gravity.RIGHT | Gravity.BOTTOM));
    }

    public void unbind() {
        avatarView.unbind();
    }

    public void bind(UserVM u) {
        avatarView.bind(u);
    }

    public void bind(GroupVM g) {
        avatarView.bind(g);
    }

    public void setOnline(boolean online, boolean isBot) {
        if (!isBot && DRAW_ONLINES) {
            this.online.setVisibility(VISIBLE);
            if (isOnline != online) {
                isOnline = online;
                this.online.setImageResource(isOnline ? R.drawable.indicator_online : R.drawable.indicator_offline);
            }
        } else {
            this.online.setVisibility(INVISIBLE);
        }

    }
}