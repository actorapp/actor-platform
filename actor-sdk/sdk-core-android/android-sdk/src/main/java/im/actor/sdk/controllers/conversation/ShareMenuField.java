package im.actor.sdk.controllers.conversation;

import android.view.View;

public class ShareMenuField {
    int icon;
    int color;
    int selector;
    int id;
    String title;
    View.OnClickListener onClickListener;

    public ShareMenuField(int icon, int color, String title, View.OnClickListener onClickListener) {
        this.icon = icon;
        this.color = color;
        this.title = title;
        this.onClickListener = onClickListener;
    }

    public ShareMenuField(String title, int id, int selector, View.OnClickListener onClickListener) {
        this.title = title;
        this.selector = selector;
        this.onClickListener = onClickListener;
        this.id = id;
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

    public int getSelector() {
        return selector;
    }

    public int getId() {
        return id;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }
}
