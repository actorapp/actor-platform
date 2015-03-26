package im.actor.images.loading.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import im.actor.images.cache.BitmapReference;
import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.ImageLoaderProvider;
import im.actor.images.loading.ImageReceiver;
import im.actor.images.loading.ReceiverCallback;
import im.actor.images.loading.AbsTask;

/**
 * Created by ex3ndr on 06.09.14.
 */
public class ImageReceiverView extends View implements ReceiverCallback {
    private ImageReceiver receiver;

    public ImageReceiverView(Context context) {
        super(context);
        init();
    }

    public ImageReceiverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageReceiverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (getContext().getApplicationContext() instanceof ImageLoaderProvider) {
            ImageLoader loader = ((ImageLoaderProvider) getContext().getApplicationContext()).getImageLoader();
            receiver = loader.createReceiver(this);
        } else {
            throw new RuntimeException("Application does not implement ImageLoaderProvider");
        }
    }

    public void request(AbsTask absTask) {
        receiver.request(absTask);
    }

    public void requestSwitch(AbsTask absTask) {
        receiver.request(absTask, false);
    }

    public void clear() {
        receiver.clear();
    }

    public void close() {
        receiver.close();
    }

    protected Bitmap getBitmap() {
        BitmapReference reference = receiver.getReference();
        if (reference != null && !reference.isReleased()) {
            return reference.getBitmap();
        } else {
            return null;
        }
    }

    @Override
    public final void onImageLoaded(BitmapReference bitmap) {
        onImageLoadedImpl(bitmap.getBitmap());
        invalidate();
    }

    @Override
    public final void onImageCleared() {
        onImageClearedImpl();
        invalidate();
    }

    @Override
    public final void onImageError() {
        invalidate();
    }

    protected void onImageLoadedImpl(Bitmap bitmap) {

    }

    protected void onImageClearedImpl() {

    }
}
