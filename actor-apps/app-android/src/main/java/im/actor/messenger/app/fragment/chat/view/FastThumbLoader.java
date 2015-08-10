package im.actor.messenger.app.fragment.chat.view;

import android.graphics.Bitmap;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.actor.messenger.app.util.images.BitmapUtil;
import im.actor.messenger.app.util.images.common.ImageLoadException;
import im.actor.messenger.app.util.images.ops.ImageLoading;
import im.actor.messenger.app.view.FastBitmapDrawable;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class FastThumbLoader {
    private static Executor executor = Executors.newSingleThreadExecutor();

    private SimpleDraweeView preview;
    private final Object LOCKER = new Object();
    private int currentRequest = 0;
    private byte[] data;
    private boolean isActive = false;
    private boolean blur = false;
    private int blurRadius = 0;

    public FastThumbLoader(SimpleDraweeView preview) {
        this.preview = preview;
    }

    public void setBlur(int radius) {
        if (radius > 0) {
            blur = true;
            blurRadius = radius;
        } else {
            blur = false;
            blurRadius = 0;
        }
    }

    public void cancel() {
        synchronized (LOCKER) {
            this.currentRequest++;
            this.data = null;
            this.isActive = false;
        }
        preview.getHierarchy().setPlaceholderImage(null);
    }

    public void request(byte[] data) {
        synchronized (LOCKER) {
            this.currentRequest++;
            this.data = data;
            this.isActive = true;
        }
        preview.getHierarchy().setPlaceholderImage(null);

        executor.execute(new CheckRunnable());
    }

    private class CheckRunnable implements Runnable {

        @Override
        public void run() {
            final int req;
            byte[] d;
            synchronized (LOCKER) {
                if (!isActive) {
                    return;
                }
                req = currentRequest;
                d = data;
            }
            try {
                final Bitmap res = blur ? BitmapUtil.fastBlur(ImageLoading.loadBitmap(d), blurRadius) : ImageLoading.loadBitmap(d);
                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (LOCKER) {
                            if (!isActive) {
                                return;
                            }
                            if (req == currentRequest) {
                                preview.getHierarchy().setPlaceholderImage(new FastBitmapDrawable(res));
                                isActive = false;
                            } else {
                                return;
                            }
                        }
                    }
                });
            } catch (ImageLoadException e) {
                e.printStackTrace();
                synchronized (LOCKER) {
                    if (isActive && currentRequest == req) {
                        isActive = false;
                    }
                }
            }
        }
    }
}