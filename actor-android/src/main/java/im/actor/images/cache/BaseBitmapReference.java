package im.actor.images.cache;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by ex3ndr on 26.08.14.
 */
 /* package */ class BaseBitmapReference {

    /* package */ String key;
    /* package */ Bitmap bitmap;

    private HashMap<BitmapReference, Object> references = new HashMap<BitmapReference, Object>();

    private boolean isReleased;
    private MemoryCache memoryCache;

    /* package */ BaseBitmapReference(MemoryCache memoryCache, String key, Bitmap bitmap) {
        this.memoryCache = memoryCache;
        this.bitmap = bitmap;
        this.key = key;
        this.isReleased = false;
    }

    public synchronized String getKey() {
        return key;
    }

    public synchronized Bitmap getBitmap() {
        return bitmap;
    }

    public synchronized boolean isReleased() {
        return isReleased;
    }

    public synchronized BitmapReference createReference(Object referent) {
        BitmapReference ref = new BitmapReference(key, bitmap, this);
        references.put(ref, referent);
        return ref;
    }

    synchronized void releaseReference(BitmapReference reference) {
        if (isReleased) {
            return;
        }

        references.remove(reference);
        if (references.size() == 0) {
            release();
        }
    }

    public synchronized void release() {
        if (isReleased) {
            return;
        }
        isReleased = true;
        for (BitmapReference reference : references.keySet().toArray(new BitmapReference[0])) {
            reference.release();
        }
        references.clear();
        memoryCache.onReferenceDie(this);
        bitmap = null;
        key = null;
    }
}
