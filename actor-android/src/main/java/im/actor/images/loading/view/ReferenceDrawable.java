package im.actor.images.loading.view;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import im.actor.images.cache.BitmapReference;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class ReferenceDrawable extends Drawable {
    private BitmapReference reference;
    private Rect src = new Rect();
    private Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private int corners;
    private BitmapShader shader;
    private Matrix shaderMatrix;

    public ReferenceDrawable(BitmapReference reference) {
        this.reference = reference;
        this.shader = new BitmapShader(reference.getBitmap(), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        this.shaderMatrix = new Matrix();
    }

    @Override
    public void draw(Canvas canvas) {
        if (reference == null || reference.isReleased()) {
            return;
        }
//        if (corners > 0) {
//            Bitmap bmp = reference.getBitmap();
//            shaderMatrix.reset();
//            shaderMatrix.postScale(getBounds().width() / (float) bmp.getWidth(), getBounds().height() / (float) bmp.getHeight());
//            shader.setLocalMatrix(shaderMatrix);
//            paint.setShader(shader);
//            canvas.drawRoundRect(new RectF(getBounds()), corners, corners, paint);
//        } else {
//            Bitmap bmp = reference.getBitmap();
//            src.set(0, 0, bmp.getWidth(), bmp.getHeight());
//            canvas.drawBitmap(reference.getBitmap(), src, getBounds(), paint);
//        }

        Bitmap bmp = reference.getBitmap();
        shaderMatrix.reset();
        shaderMatrix.postScale(getBounds().width() / (float) bmp.getWidth(), getBounds().height() / (float) bmp.getHeight());
        shader.setLocalMatrix(shaderMatrix);
        paint.setShader(shader);
        canvas.drawCircle(getBounds().centerX(), getBounds().centerY(), getBounds().width() / 2, paint);
    }

    public void release() {
        if (reference != null && !reference.isReleased()) {
            reference.release();
            reference = null;
        }
        shader = null;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
