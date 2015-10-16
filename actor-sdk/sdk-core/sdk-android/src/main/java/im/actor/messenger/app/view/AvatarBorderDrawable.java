package im.actor.messenger.app.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Created by ex3ndr on 01.04.15.
 */
public class AvatarBorderDrawable extends Drawable {
    private static final Paint CIRCLE_BORDER_PAINT = new Paint();

    static {
        CIRCLE_BORDER_PAINT.setStyle(Paint.Style.STROKE);
        CIRCLE_BORDER_PAINT.setAntiAlias(true);
        CIRCLE_BORDER_PAINT.setColor(0x19000000);
        CIRCLE_BORDER_PAINT.setStrokeWidth(1);
    }

    @Override
    public void draw(Canvas canvas) {
        int w = getBounds().width();
        int h = getBounds().height();

        canvas.drawCircle(getBounds().centerX(), getBounds().centerY(), (Math.min(w, h)) / 2, CIRCLE_BORDER_PAINT);
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
