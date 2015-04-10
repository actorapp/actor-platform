package im.actor.images.loading.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import im.actor.images.cache.BitmapReference;
import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.ImageLoaderProvider;
import im.actor.images.loading.ImageReceiver;
import im.actor.images.loading.ReceiverCallback;
import im.actor.images.loading.AbsTask;

/**
 * Created by ex3ndr on 20.08.14.
 */
public class ImageKitView extends ImageView implements ReceiverCallback {
    private ImageReceiver receiver;
    private boolean isCallbackBlocked = false;
    private boolean isInternal = false;
    private ReceiverCallback extraReceiver;

    public ImageKitView(Context context) {
        super(context);
        init();
    }

    public ImageKitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageKitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    public void request(AbsTask task) {
        receiver.request(task);
    }

    @Override
    public void setImageResource(int resId) {
        if (!isInternal) {
            isCallbackBlocked = true;
            receiver.clear();
            isCallbackBlocked = false;
        }
        super.setImageResource(resId);
    }

    @Override
    public void setImageURI(Uri uri) {
        if (!isInternal) {
            isCallbackBlocked = true;
            receiver.clear();
            isCallbackBlocked = false;
        }
        super.setImageURI(uri);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (!isInternal) {
            isCallbackBlocked = true;
            receiver.clear();
            isCallbackBlocked = false;
        }
        super.setImageDrawable(drawable);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (!isInternal) {
            isCallbackBlocked = true;
            receiver.clear();
            isCallbackBlocked = false;
        }
        super.setImageBitmap(bm);
    }

    public void clear() {
        receiver.clear();
    }

    @Override
    public void onImageLoaded(BitmapReference bitmap) {
        if (!isCallbackBlocked) {
            isInternal = true;
            setImageBitmap(bitmap.getBitmap());
            isInternal = false;
        }
        if(extraReceiver!=null){
            extraReceiver.onImageLoaded(bitmap);
        }
    }

    @Override
    public void onImageCleared() {
        if (!isCallbackBlocked) {
            isInternal = true;
            setImageBitmap(null);
            isInternal = false;
        }
        if(extraReceiver!=null){
            extraReceiver.onImageCleared();
        }
    }

    @Override
    public void onImageError() {
        if (!isCallbackBlocked) {
            isInternal = true;
            setImageBitmap(null);
            isInternal = false;
        }
        if(extraReceiver!=null){
            extraReceiver.onImageError();
        }
    }
    public void setExtraReceiverCallback(ReceiverCallback receiverCallback){
        this.extraReceiver = receiverCallback;
    }
}
