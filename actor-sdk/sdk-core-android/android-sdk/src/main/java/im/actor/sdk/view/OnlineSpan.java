package im.actor.sdk.view;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;

/**
 * Created by badgr on 29.09.2015.
 */
public class OnlineSpan extends ForegroundColorSpan {
    private boolean online = false;
    private static final int COLOR_ONLINE = Color.parseColor("#7b95b6");
    private static final int COLOR_OFFLINE = Color.parseColor("#00000000");

    public OnlineSpan(int color) {
        super(color);
    }

    public OnlineSpan() {
        super(COLOR_OFFLINE);
    }

    @Override
    public int getForegroundColor() {
        return online ? COLOR_ONLINE : COLOR_OFFLINE;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(getForegroundColor());
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}