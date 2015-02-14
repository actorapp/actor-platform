package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

import com.droidkit.images.loading.tasks.RawFileTask;
import com.droidkit.images.loading.view.ImageReceiverView;

import java.lang.reflect.Field;

import im.actor.messenger.core.images.ImagePreviewTask;
import im.actor.messenger.core.images.VideoPreviewTask;
import im.actor.messenger.core.images.VideoTask;
import im.actor.messenger.util.Screen;
import im.actor.model.entity.Message;

/**
 * Created by ex3ndr on 06.09.14.
 */
public class PhotoPreview extends ImageReceiverView {

    private final Matrix MATRIX = new Matrix();
    private final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap src;
    private BitmapShader shader;
    private int shaderW;
    private int shaderH;

    public PhotoPreview(Context context) {
        super(context);
    }

    public PhotoPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void requestPhoto(String fileName) {
        requestSwitch(new RawFileTask(fileName));
    }

    public void requestVideo(String fileName) {
        requestSwitch(new VideoTask(fileName));
    }

    public void requestVideo(int type, int id, Message message) {
        requestSwitch(new VideoPreviewTask(type, id, message));
    }

    public void requestPhoto(int type, int id, Message message) {
        requestSwitch(new ImagePreviewTask(type, id, message));
    }

    public void noRequest() {
        clear();
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
            MATRIX.postScale(getWidth() / (float) shaderW, getHeight() / (float) shaderH);
            shader.setLocalMatrix(MATRIX);
            canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), Screen.dp(2), Screen.dp(2), PAINT);
        }
    }
}
