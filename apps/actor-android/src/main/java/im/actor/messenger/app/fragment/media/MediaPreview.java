package im.actor.messenger.app.fragment.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;


import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.reflect.Field;

import im.actor.images.loading.tasks.PreviewFileTask;
import im.actor.images.loading.view.ImageReceiverView;
import im.actor.images.util.BitmapUtil;

/**
 * Created by Jesus Christ. Amen.
 */
public class MediaPreview extends SimpleDraweeView {


    private final Matrix MATRIX = new Matrix();
    private final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap src;
    private BitmapShader shader;
    private int shaderW;
    private int shaderH;
    private float scale = 1;
    private float x = 0;
    private float y = 0;

    public MediaPreview(Context context) {
        super(context);
    }

    public MediaPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void noRequest() {
        //clear();
    }

    private void bindShader(Bitmap bmp) {
        if (shader != null) {
            try {
                Field fieldA = BitmapShader.class.getDeclaredField("mBitmap");
                fieldA.setAccessible(true);
                fieldA.set(shader, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.shader = null;
        }
        if (bmp == null) {
            this.PAINT.setShader(null);
        } else {
            this.shader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            this.shaderW = bmp.getWidth();
            this.shaderH = bmp.getHeight();

            this.PAINT.setShader(shader);
        }
    }

    public void setSrc(Bitmap src) {
        this.src = src;
        bindShader(src);
    }

    /*@Override
    public void clear() {
        // super.clear();
        setSrc(null);
    }

    @Override
    protected void onImageLoadedImpl(Bitmap bitmap) {
        bindShader(bitmap);
    }

    @Override
    protected void onImageClearedImpl() {
        bindShader(src);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shader != null) {
            MATRIX.reset();
            // centerCrop
            if(shaderH>shaderW){
                scale = getWidth()/(float)shaderW;
                x = 0;
                y = (shaderH - shaderW)/2;
            } else {
                scale = getHeight()/(float)shaderH;
                x = (shaderW - shaderH)/2;
                y = 0;
            }
            MATRIX.postTranslate(-x, -y);
            MATRIX.postScale(scale, scale);
            shader.setLocalMatrix(MATRIX);
            canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), 0, 0, PAINT);
        }
    }*/

    public void setThumb(Bitmap bmp) {
        setSrc(BitmapUtil.fastBlur(bmp, 3));
    }

    /*public void requestPhoto(String downloadedPath, int tileSize) {
        request(new PreviewFileTask(downloadedPath, tileSize, tileSize));
    }*/
}
