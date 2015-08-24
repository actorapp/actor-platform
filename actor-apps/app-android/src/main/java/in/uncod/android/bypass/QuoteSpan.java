package in.uncod.android.bypass;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;

import im.actor.messenger.app.util.Screen;

/**
 * Created by korka on 25.06.15.
 */
public class QuoteSpan extends android.text.style.QuoteSpan {
    private static final int STRIPE_WIDTH = Screen.dp(2);
    private static final int STRIPE_PADDING = Screen.dp(2);
    private static final int TEXT_PADDING = Screen.dp(4);

    public QuoteSpan(int color) {
        super(color);
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return STRIPE_WIDTH + STRIPE_PADDING + TEXT_PADDING;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(getColor());

        c.drawRect(x + STRIPE_PADDING, top, x + dir * STRIPE_WIDTH + STRIPE_PADDING, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }


}
