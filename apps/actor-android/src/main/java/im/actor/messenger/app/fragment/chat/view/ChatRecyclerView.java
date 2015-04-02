package im.actor.messenger.app.fragment.chat.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by ex3ndr on 02.04.15.
 */
public class ChatRecyclerView extends RecyclerView {

    private int oldHeight;

    public ChatRecyclerView(Context context) {
        super(context);
    }

    public ChatRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int delta = (b - t) - oldHeight;
        oldHeight = b - t;
        if (delta < 0) {
            scrollBy(0, -delta);
        }
    }
}
