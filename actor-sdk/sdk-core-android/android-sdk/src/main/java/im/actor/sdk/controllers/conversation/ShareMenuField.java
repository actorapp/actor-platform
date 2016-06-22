package im.actor.sdk.controllers.conversation;

import android.view.View;

public class ShareMenuField {
    int icon;
    int color;
    String title;
    View.OnClickListener onClickListener;

    public ShareMenuField(int icon, int color, String title, View.OnClickListener onClickListener) {
        this.icon = icon;
        this.color = color;
        this.title = title;
        this.onClickListener = onClickListener;
    }

    public int getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public String getTitle() {
        return title;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }
}
