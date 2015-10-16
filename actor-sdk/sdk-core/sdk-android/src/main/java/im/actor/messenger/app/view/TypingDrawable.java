package im.actor.messenger.app.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import im.actor.messenger.app.util.Screen;

/**
 * Created by ex3ndr on 25.03.14.
 */
public class TypingDrawable extends Drawable {

    private static final int DURATION = 600;
    private static final int ANIMATION_DELTA = 800;
    private static final int ANIMATION_OFFSET = DURATION / 3;

    private static final int OFFSET = 1;
    private static final int RADIUS = 3;
    private static final int DIAMETER = RADIUS * 2;
    private static final int PADDING = 3;

    private Paint paint;

    private int height;

    public TypingDrawable() {
        height = Screen.dp(DIAMETER);
        paint = new Paint();
        paint.setColor(0xffdae1ea);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    public int getIntrinsicWidth() {
        return Screen.dp(((DIAMETER + OFFSET) * 3 + PADDING));
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    private float process(int offset) {
        long time = (System.currentTimeMillis() + offset) % (DURATION + ANIMATION_DELTA);
        if (time > DURATION) {
            return 0;
        }
        float p = time / (float) (DURATION);
        return (float) Math.abs(Math.sin(Math.PI * p));
    }

    @Override
    public void draw(Canvas canvas) {
        float size1 = 0.7f + (0.3f * process(ANIMATION_OFFSET * 2));
        float size2 = 0.7f + (0.3f * process(ANIMATION_OFFSET));
        float size3 = 0.7f + (0.3f * process(0));
        Rect bounds = getBounds();
        int top = ((getBounds().height() - Screen.dp(DIAMETER)) / 2) + bounds.top;
        canvas.drawCircle(Screen.dp(RADIUS), Screen.dp(RADIUS) + top, size1 * Screen.dp(RADIUS), paint);
        canvas.drawCircle(Screen.dp(RADIUS + DIAMETER + OFFSET), Screen.dp(RADIUS) + top, size2 * Screen.dp(RADIUS), paint);
        canvas.drawCircle(Screen.dp(RADIUS + (DIAMETER + OFFSET) * 2), Screen.dp(RADIUS) + top, size3 * Screen.dp(RADIUS), paint);
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}