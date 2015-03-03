package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import com.droidkit.images.cache.BitmapReference;
import com.droidkit.images.cache.DiskCache;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.ImageLoaderProvider;
import com.droidkit.images.loading.ImageReceiver;
import com.droidkit.images.loading.ReceiverCallback;
import com.droidkit.images.loading.tasks.RawFileTask;
import com.droidkit.images.loading.view.ReferenceDrawable;
import com.droidkit.images.ops.ImageLoading;

import im.actor.messenger.core.Core;
import im.actor.messenger.app.images.AvatarTask;
import im.actor.messenger.app.images.FileKeys;
import im.actor.messenger.util.Logger;
import im.actor.messenger.util.Screen;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.FileLocation;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class AvatarView extends View implements ReceiverCallback {

    private static final long TRANSITION_DURATION = 200;

    private final Paint CIRCLE_BORDER_PAINT = new Paint();

    private ImageReceiver receiver;

    private Drawable emptyDrawable;

    private Drawable prevDrawable;

    private Drawable drawable;

    private long transitionStart;

    private boolean isNewDrawn = false;

    private int corners;

    private Interpolator interpolator = MaterialInterpolator.getInstance();

    public AvatarView(Context context) {
        super(context);
        init();
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        CIRCLE_BORDER_PAINT.setStyle(Paint.Style.STROKE);
        CIRCLE_BORDER_PAINT.setAntiAlias(true);
        CIRCLE_BORDER_PAINT.setColor(0x19000000);
        CIRCLE_BORDER_PAINT.setStrokeWidth(1);

        if (getContext().getApplicationContext() instanceof ImageLoaderProvider) {
            ImageLoader loader = ((ImageLoaderProvider) getContext().getApplicationContext()).getImageLoader();
            receiver = loader.createReceiver(this);
        } else {
            throw new RuntimeException("Application does not implement ImageLoaderProvider");
        }
    }

    public void setEmptyDrawable(Drawable emptyDrawable) {
        this.emptyDrawable = emptyDrawable;

        if (drawable == null || !(drawable instanceof ReferenceDrawable)) {
            drawable = emptyDrawable;
        }

        if (prevDrawable == null || !(prevDrawable instanceof ReferenceDrawable)) {
            prevDrawable = emptyDrawable;
        }

        invalidate();
    }

    public void bindFastAvatar(int size, Avatar avatar) {
        FileLocation fileLocation = avatar.getSmallImage().getFileLocation();
        DiskCache diskCache = Core.core().getImageLoader().getInternalDiskCache();
        String avatarKey = FileKeys.avatarKey(fileLocation.getFileId());
        String file = diskCache.lockFile(avatarKey);
        if (file != null) {
            try {
                Bitmap bitmap = ImageLoading.loadBitmapOptimized(file);
                bindImage(bitmap);
                transitionStart = 0;
                return;
            } catch (ImageLoadException e) {
                e.printStackTrace();
            }
        }
        bindAvatar(size, avatar);
    }

    public void bindImage(Bitmap bitmap) {
        receiver.clear();

        releasePrev();
        if (!isNewDrawn) {
            transitionStart = 0;
        } else {
            transitionStart = SystemClock.uptimeMillis();
        }
        prevDrawable = drawable;

        drawable = new RoundDrawable(bitmap);
        isNewDrawn = false;
        postInvalidate();
    }

    public void bindAvatar(int size, Avatar avatar) {
        Logger.d("AvatarView", "Request avatar");
        if (receiver != null) {
            receiver.request(new AvatarTask(Screen.dp(size), avatar));
            invalidate();
        }
    }

    public void bindUploading(String fileName) {
        if (receiver != null) {
            receiver.request(new RawFileTask(fileName));
            invalidate();
        }
    }

    public void unbind() {
        if (receiver != null) {
            receiver.clear();

            releasePrev();
            release();

            transitionStart = 0;
            drawable = emptyDrawable;
            isNewDrawn = false;
            invalidate();
        }
    }

    private void releasePrev() {
        if (prevDrawable != null && prevDrawable instanceof ReferenceDrawable) {
            ((ReferenceDrawable) prevDrawable).release();
        }
        prevDrawable = null;
    }

    private void release() {
        if (drawable != null && drawable instanceof ReferenceDrawable) {
            ((ReferenceDrawable) drawable).release();
        }
        drawable = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        isNewDrawn = true;
        long time = SystemClock.uptimeMillis();
        if (time - transitionStart < TRANSITION_DURATION) {
            float alpha = interpolator.getInterpolation((time - transitionStart) / (float) TRANSITION_DURATION);
            if (prevDrawable != null) {
                prevDrawable.setBounds(0, 0, getWidth(), getHeight());
                prevDrawable.setAlpha(255);
                prevDrawable.draw(canvas);
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, getWidth(), getHeight());
                drawable.setAlpha((int) (255 * alpha));
                drawable.draw(canvas);
            }
            invalidate();
        } else {
            releasePrev();
            if (drawable != null) {
                drawable.setBounds(0, 0, getWidth(), getHeight());
                drawable.setAlpha(255);
                drawable.draw(canvas);
            }
        }

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, CIRCLE_BORDER_PAINT);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (receiver != null) {
            receiver.close();
            receiver = null;
        }
        unbind();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (receiver == null) {
            ImageLoader loader = ((ImageLoaderProvider) getContext().getApplicationContext()).getImageLoader();
            receiver = loader.createReceiver(this);
        }
    }

    @Override
    public void onImageLoaded(BitmapReference bitmap) {
        Logger.d("AvatarView", "OnImageLoaded");
        releasePrev();
        if (!isNewDrawn) {
            transitionStart = 0;
        } else {
            transitionStart = SystemClock.uptimeMillis();
        }
        prevDrawable = drawable;

        drawable = new ReferenceDrawable(bitmap.fork(this));
        isNewDrawn = false;
        postInvalidate();
    }

    @Override
    public void onImageCleared() {
        // Ignore
    }

    @Override
    public void onImageError() {
        // Ignore
    }
}
