package im.actor.images.cache;

import android.graphics.Bitmap;

/**
 * Created by ex3ndr on 26.08.14.
 */
public class BitmapReference {
    private Bitmap bitmap;
    private String key;
    private boolean isReleased;
    private BaseBitmapReference baseReference;

    /* package */ BitmapReference(String key, Bitmap bitmap, BaseBitmapReference reference) {
        this.bitmap = bitmap;
        this.key = key;
        this.isReleased = false;
        this.baseReference = reference;
    }

    public Bitmap getBitmap() {
        if (isReleased) {
            throw new RuntimeException("Released Bitmap");
        }
        return bitmap;
    }

    public String getKey() {
        if (isReleased) {
            throw new RuntimeException("Released Bitmap");
        }
        return key;
    }

    public boolean isReleased() {
        return isReleased;
    }

    public BitmapReference fork(Object referrent) {
        if (isReleased) {
            throw new RuntimeException("Released Bitmap");
        }
        return baseReference.createReference(referrent);
    }

    public synchronized void release() {
        if (isReleased) {
            throw new RuntimeException("Already released Bitmap");
        }
        isReleased = true;
        baseReference.releaseReference(this);
        baseReference = null;
        bitmap = null;
        key = null;
    }
}
