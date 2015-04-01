package im.actor.messenger.app.fragment.chat.view;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.actor.images.common.ImageLoadException;
import im.actor.images.ops.ImageLoading;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class FastThumbLoader {
    private static Executor executor = Executors.newSingleThreadExecutor();

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            synchronized (LOCKER) {
                if (!isActive) {
                    return;
                }
                if (msg.what == currentRequest) {
                    preview.getHierarchy().setPlaceholderImage(new BitmapDrawable((Bitmap) msg.obj));
                    isActive = false;
                } else {
                    return;
                }
            }
        }
    };

    private SimpleDraweeView preview;
    private final Object LOCKER = new Object();
    private int currentRequest = 0;
    private byte[] data;
    private boolean isActive = false;

    public FastThumbLoader(SimpleDraweeView preview) {
        this.preview = preview;
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
            int req;
            byte[] d;
            synchronized (LOCKER) {
                if (!isActive) {
                    return;
                }
                req = currentRequest;
                d = data;
            }
            try {
                Bitmap res = ImageLoading.loadBitmap(d);
                handler.sendMessage(handler.obtainMessage(req, res));
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
