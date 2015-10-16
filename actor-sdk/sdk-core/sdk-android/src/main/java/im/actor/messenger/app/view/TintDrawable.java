package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by ex3ndr on 04.04.15.
 */
public class TintDrawable extends Drawable {

    private final Paint PAINT = new Paint();

    private Bitmap src;

    public TintDrawable(int resourceId, int colorId, Context context) {
        Drawable drawable = context.getResources().getDrawable(resourceId);
        if (!(drawable instanceof BitmapDrawable)) {
            throw new RuntimeException("BitmapDrawable is required");
        }
        src = ((BitmapDrawable) drawable).getBitmap();
        PAINT.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(colorId),
                PorterDuff.Mode.SRC_IN));
    }

    public TintDrawable(Drawable drawable, int color) {
        if (!(drawable instanceof BitmapDrawable)) {
            throw new RuntimeException("BitmapDrawable is required");
        }
        src = ((BitmapDrawable) drawable).getBitmap();
        PAINT.setColorFilter(new PorterDuffColorFilter(color,
                PorterDuff.Mode.SRC_IN));
    }

    public TintDrawable(Bitmap src, int color) {
        this.src = src;
        PAINT.setColorFilter(new PorterDuffColorFilter(color,
                PorterDuff.Mode.SRC_IN));
    }

    @Override
    public int getIntrinsicWidth() {
        return src.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return src.getHeight();
    }

    @Override
    public void draw(Canvas canvas) {
        int x = (getBounds().width() - src.getWidth()) / 2;
        int y = (getBounds().height() - src.getHeight()) / 2;

        canvas.drawBitmap(src, x, y, PAINT);
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
