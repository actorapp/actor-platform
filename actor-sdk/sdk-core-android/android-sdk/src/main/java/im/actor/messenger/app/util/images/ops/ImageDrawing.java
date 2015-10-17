package im.actor.messenger.app.util.images.ops;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import im.actor.messenger.app.util.images.common.WorkCache;

/**
 * Drawing operations for bitmaps
 */
public class ImageDrawing {

    /**
     * Default clear color (Transparent)
     */
    public static final int CLEAR_COLOR = Color.TRANSPARENT;

    /**
     * Clearing bitmap with transparent color (Transparent)
     *
     * @param bitmap bitmap for clearing
     */
    public static void clearBitmap(Bitmap bitmap) {
        clearBitmap(bitmap, CLEAR_COLOR);
    }

    /**
     * Clearing bitmap with transparent color
     *
     * @param bitmap bitmap for clearing
     */
    public static void clearBitmap(Bitmap bitmap, int color) {
        bitmap.eraseColor(color);
    }

    /**
     * Drawing bitmap over dest bitmap with clearing last one before drawing
     *
     * @param src  source bitmap
     * @param dest destination bitmap
     */
    public static void drawTo(Bitmap src, Bitmap dest) {
        drawTo(src, dest, CLEAR_COLOR);
    }

    /**
     * Drawing bitmap over dest bitmap with clearing last one before drawing
     *
     * @param src   source bitmap
     * @param dest  destination bitmap
     * @param color clear color
     */
    public static void drawTo(Bitmap src, Bitmap dest, int color) {
        clearBitmap(src, color);
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(src, 0, 0, null);
        canvas.setBitmap(null);
    }

    /**
     * Drawing src bitmap to dest bitmap with applied mask.
     *
     * @param src  source bitmap
     * @param mask bitmap mask
     * @param dest destination bitmap
     */

    public static void drawMasked(Bitmap src, Drawable mask, Bitmap dest) {
        drawMasked(src, mask, dest, CLEAR_COLOR);
    }

    /**
     * Drawing src bitmap to dest bitmap with applied mask.
     *
     * @param src        source bitmap
     * @param mask       bitmap mask
     * @param dest       destination bitmap
     * @param clearColor clear color
     */
    public static void drawMasked(Bitmap src, Drawable mask, Bitmap dest, int clearColor) {
        clearBitmap(dest, clearColor);
        Canvas canvas = new Canvas(dest);

        canvas.drawBitmap(src,
                new Rect(0, 0, src.getWidth(), src.getHeight()),
                new Rect(0, 0, dest.getWidth(), dest.getHeight()),
                new Paint(Paint.FILTER_BITMAP_FLAG));

        if (mask instanceof BitmapDrawable) {
            ((BitmapDrawable) mask).getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        } else if (mask instanceof NinePatchDrawable) {
            ((NinePatchDrawable) mask).getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        } else {
            throw new RuntimeException("Supported only BitmapDrawable or NinePatchDrawable");
        }
        mask.setBounds(0, 0, mask.getIntrinsicWidth(), mask.getIntrinsicHeight());
        mask.draw(canvas);
        canvas.setBitmap(null);
    }

    /**
     * Drawing src bitmap to dest bitmap with rounded corners
     *
     * @param src    source bitmap
     * @param dest   destination bitmap
     * @param radius radius in destination bitmap scale
     */
    public static void drawRoundedCorners(Bitmap src, Bitmap dest, int radius) {
        drawRoundedCorners(src, dest, radius, CLEAR_COLOR);
    }

    /**
     * Drawing src bitmap to dest bitmap with rounded corners
     *
     * @param src        source bitmap
     * @param dest       destination bitmap
     * @param radius     radius in destination bitmap scale
     * @param clearColor clear color
     */
    public static void drawRoundedCorners(Bitmap src, Bitmap dest, int radius, int clearColor) {
        clearBitmap(dest, clearColor);
        Canvas canvas = new Canvas(dest);

        Rect sourceRect = WorkCache.RECT1.get();
        Rect destRect = WorkCache.RECT2.get();
        sourceRect.set(0, 0, src.getWidth(), src.getHeight());
        destRect.set(0, 0, dest.getWidth(), dest.getHeight());

        RectF roundRect = WorkCache.RECTF1.get();
        roundRect.set(0, 0, dest.getWidth(), dest.getHeight());

        Paint paint = WorkCache.PAINT.get();
        paint.reset();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(roundRect, radius, radius, paint);

        paint.reset();
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, sourceRect, destRect, paint);

        canvas.setBitmap(null);
    }

    /**
     * Drawing src bitmap to dest bitmap with round mask. Dest might be squared, src is recommended to be square
     *
     * @param src  source bitmap
     * @param dest destination bitmap
     */
    public static void drawInRound(Bitmap src, Bitmap dest) {
        drawInRound(src, dest, CLEAR_COLOR);
    }

    /**
     * Drawing src bitmap to dest bitmap with round mask. Dest might be squared, src is recommended to be square
     *
     * @param src        source bitmap
     * @param dest       destination bitmap
     * @param clearColor clear color
     */
    public static void drawInRound(Bitmap src, Bitmap dest, int clearColor) {
        if (dest.getWidth() != dest.getHeight()) {
            throw new RuntimeException("dest Bitmap must have square size");
        }
        clearBitmap(dest, clearColor);
        Canvas canvas = new Canvas(dest);

        int r = dest.getWidth() / 2;
        Rect sourceRect = WorkCache.RECT1.get();
        Rect destRect = WorkCache.RECT2.get();
        sourceRect.set(0, 0, src.getWidth(), src.getHeight());
        destRect.set(0, 0, dest.getWidth(), dest.getHeight());

        Paint paint = WorkCache.PAINT.get();
        paint.reset();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        canvas.drawCircle(r, r, r, paint);

        paint.reset();
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, sourceRect, destRect, paint);

        canvas.setBitmap(null);
    }
}