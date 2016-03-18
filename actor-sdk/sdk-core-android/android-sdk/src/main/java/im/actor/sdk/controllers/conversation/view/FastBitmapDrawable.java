package im.actor.sdk.controllers.conversation.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

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
        if (!bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, bitmapBounds, getBounds(), PAINT);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        // Not Supported
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // Not Supported
    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
