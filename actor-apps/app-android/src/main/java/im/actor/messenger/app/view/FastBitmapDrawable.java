package im.actor.messenger.app.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Created by ex3ndr on 02.04.15.
 */
public class FastBitmapDrawable extends Drawable {
    private static final Paint PAINT = new Paint();

    static {
        PAINT.setAntiAlias(true);
    }

    private Bitmap bitmap;
    private Rect bitmapBounds;

    public FastBitmapDrawable(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.bitmapBounds = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, bitmapBounds, getBounds(), PAINT);
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
