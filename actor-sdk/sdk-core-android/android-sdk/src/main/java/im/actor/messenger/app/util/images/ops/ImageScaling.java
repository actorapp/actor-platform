package im.actor.messenger.app.util.images.ops;

import android.graphics.*;
import im.actor.messenger.app.util.images.common.WorkCache;

import static im.actor.messenger.app.util.images.ops.ImageDrawing.*;

/**
 * Scaling images effectively with keeping good quality.
 *
 * @author Stepan ex3ndr Korshakov me@ex3ndr.com
 */
public class ImageScaling {

    /**
     * Scaling bitmap to fill rect with centering. Method keep aspect ratio.
     *
     * @param src source bitmap
     * @param w   width of result
     * @param h   height of result
     * @return scaled bitmap
     */
    public static Bitmap scaleFill(Bitmap src, int w, int h) {
        Bitmap res = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        scaleFill(src, res);
        return res;
    }

    /**
     * Scaling src bitmap to fill dest bitmap with centering. Method keep aspect ratio.
     *
     * @param src  source bitmap
     * @param dest destination bitmap
     */
    public static void scaleFill(Bitmap src, Bitmap dest) {
        scaleFill(src, dest, CLEAR_COLOR);
    }

    /**
     * Scaling src bitmap to fill dest bitmap with centering. Method keep aspect ratio.
     *
     * @param src        source bitmap
     * @param dest       destination bitmap
     * @param clearColor color for clearing dest before drawing
     */
    public static void scaleFill(Bitmap src, Bitmap dest, int clearColor) {
        float ratio = Math.max(dest.getWidth() / (float) src.getWidth(), dest.getHeight() / (float) src.getHeight());
        int newW = (int) (src.getWidth() * ratio);
        int newH = (int) (src.getHeight() * ratio);
        int paddingTop = (dest.getHeight() - (int) (src.getHeight() * ratio)) / 2;
        int paddingLeft = (dest.getWidth() - (int) (src.getWidth() * ratio)) / 2;

        scale(src, dest, clearColor, 0, 0, src.getWidth(), src.getHeight(), paddingLeft, paddingTop,
                newW + paddingLeft,
                newH + paddingTop);
    }

    /**
     * Scaling bitmap to fit required sizes. Method keep aspect ratio.
     *
     * @param src  source bitmap
     * @param maxW maximum width of result bitmap
     * @param maxH maximum height of result bitmap
     * @return scaled bitmap
     */
    public static Bitmap scaleFit(Bitmap src, int maxW, int maxH) {
        float ratio = Math.min(maxW / (float) src.getWidth(), maxH / (float) src.getHeight());
        int newW = (int) (src.getWidth() * ratio);
        int newH = (int) (src.getHeight() * ratio);
        return scale(src, newW, newH);
    }

    /**
     * Scaling src Bitmap to fit and cenetered in dest bitmap. Method keep aspect ratio.
     *
     * @param src  source bitmap
     * @param dest destination bitmap
     */
    public static void scaleFit(Bitmap src, Bitmap dest) {
        scaleFit(src, dest, CLEAR_COLOR);
    }

    /**
     * Scaling src Bitmap to fit and cenetered in dest bitmap. Method keep aspect ratio.
     *
     * @param src        source bitmap
     * @param dest       destination bitmap
     * @param clearColor color for clearing dest before drawing
     */
    public static void scaleFit(Bitmap src, Bitmap dest, int clearColor) {
        float ratio = Math.min(dest.getWidth() / (float) src.getWidth(), dest.getHeight() / (float) src.getHeight());
        int newW = (int) (src.getWidth() * ratio);
        int newH = (int) (src.getHeight() * ratio);
        int paddingTop = (dest.getHeight() - (int) (src.getHeight() * ratio)) / 2;
        int paddingLeft = (dest.getWidth() - (int) (src.getWidth() * ratio)) / 2;

        scale(src, dest, clearColor, 0, 0, src.getWidth(), src.getHeight(), paddingLeft, paddingTop,
                newW + paddingLeft,
                newH + paddingTop);
    }

    /**
     * Scaling bitmap to specific width and height without keeping aspect ratio.
     *
     * @param src source bitmap
     * @param dw  new width
     * @param dh  new height
     * @return scaled bitmap
     */
    public static Bitmap scale(Bitmap src, int dw, int dh) {
        Bitmap res = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888);
        scale(src, res);
        return res;
    }

    /**
     * Scaling bitmap to fill dest bitmap without keeping aspect ratio.
     *
     * @param src  source bitmap
     * @param dest destination bitmap
     */
    public static void scale(Bitmap src, Bitmap dest) {
        scale(src, dest, CLEAR_COLOR);
    }

    /**
     * Scaling bitmap to fill dest bitmap without keeping aspect ratio.
     *
     * @param src        source bitmap
     * @param dest       destination bitmap
     * @param clearColor color for clearing dest before drawing
     */
    public static void scale(Bitmap src, Bitmap dest, int clearColor) {
        scale(src, dest, clearColor, 0, 0, src.getWidth(), src.getHeight(), 0, 0, dest.getWidth(), dest.getHeight());
    }

    /**
     * Scaling region of bitmap to destination bitmap region
     *
     * @param src  source bitmap
     * @param dest destination bitmap
     * @param x    source x
     * @param y    source y
     * @param sw   source width
     * @param sh   source height
     * @param dx   destination x
     * @param dy   destination y
     * @param dw   destination width
     * @param dh   destination height
     */
    public static void scale(Bitmap src, Bitmap dest,
                             int x, int y, int sw, int sh,
                             int dx, int dy,
                             int dw, int dh) {
        scale(src, dest, CLEAR_COLOR, x, y, sw, sh, dx, dy, dw, dh);
    }

    /**
     * Scaling region of bitmap to destination bitmap region
     *
     * @param src        source bitmap
     * @param dest       destination bitmap
     * @param clearColor color for clearing dest before drawing
     * @param x          source x
     * @param y          source y
     * @param sw         source width
     * @param sh         source height
     * @param dx         destination x
     * @param dy         destination y
     * @param dw         destination width
     * @param dh         destination height
     */
    public static void scale(Bitmap src, Bitmap dest, int clearColor,
                             int x, int y, int sw, int sh,
                             int dx, int dy,
                             int dw, int dh) {
        clearBitmap(dest, clearColor);
        Canvas canvas = new Canvas(dest);
        Paint paint = WorkCache.PAINT.get();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(src, new Rect(x + 1, y + 1, sw - 1, sh - 1), new Rect(dx, dy, dw, dh), paint);
        canvas.setBitmap(null);
    }

    protected ImageScaling() {
    }
}