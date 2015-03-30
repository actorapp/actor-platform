package im.actor.messenger.app.view;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Created by ex3ndr on 30.10.14.
 */
public class RoundDrawable extends Drawable {

    private Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

    private BitmapShader shader;
    private Matrix shaderMatrix;
    private Bitmap bitmap;

    public RoundDrawable(Bitmap bitmap) {
        this.bitmap = bitmap;

        this.shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        this.shaderMatrix = new Matrix();
    }

    @Override
    public void draw(Canvas canvas) {
        shaderMatrix.reset();
        shaderMatrix.postScale(getBounds().width() / (float) bitmap.getWidth(), getBounds().height() / (float) bitmap.getHeight());
        shader.setLocalMatrix(shaderMatrix);
        paint.setShader(shader);
        canvas.drawCircle(getBounds().centerX(), getBounds().centerY(), getBounds().width() / 2, paint);
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
