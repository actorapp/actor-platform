package im.actor.images.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import im.actor.images.ops.ImageLoading;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ex3ndr on 26.08.14.
 */
public class MemoryCache {
    private HashMap<String, BaseBitmapReference> references = new HashMap<String, BaseBitmapReference>();
    private HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();

    private BitmapClasificator clasificator;
    private final HashMap<Integer, CategoryHolder> categories = new HashMap<Integer, CategoryHolder>();

    public MemoryCache(BitmapClasificator clasificator) {
        this.clasificator = clasificator;
        BitmapClasificator.CacheConfig[] configs = clasificator.getConfigs();
        for (BitmapClasificator.CacheConfig config : configs) {
            LruCache<String, Bitmap> lruCache = null;
            LruCache<String, Bitmap> freeCache = null;

            if (config.isFreeEnabled()) {
                if (config.isUseSizeInBytes()) {
                    freeCache = new LruCache<String, Bitmap>(config.getMaxFreeSize()) {
                        @Override
                        protected int sizeOf(String key, Bitmap value) {
                            return ImageLoading.bitmapSize(value);
                        }
                    };
                } else {
                    freeCache = new LruCache<String, Bitmap>(config.getMaxFreeSize());
                }
            }

            if (config.isLruEnabled()) {
                final LruCache<String, Bitmap> finalFreeCache = freeCache;
                if (config.isUseSizeInBytes()) {
                    lruCache = new LruCache<String, Bitmap>(config.getMaxLruSize()) {
                        @Override
                        protected int sizeOf(String key, Bitmap value) {
                            return ImageLoading.bitmapSize(value);
                        }

                        @Override
                        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                            if (newValue == null) {
                                return;
                            }
                            if (evicted) {
                                if (finalFreeCache != null) {
                                    finalFreeCache.put(key, newValue);
                                }
                            }
                        }
                    };
                } else {
                    lruCache = new LruCache<String, Bitmap>(config.getMaxLruSize()) {
                        @Override
                        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                            if (newValue == null) {
                                return;
                            }
                            if (evicted) {
                                if (finalFreeCache != null) {
                                    finalFreeCache.put(key, newValue);
                                }
                            }
                        }
                    };
                }
            }

            categories.put(config.getCategory(), new CategoryHolder(config, lruCache, freeCache));
        }
    }

    public synchronized BitmapReference findInCache(String key, Object referrent) {
        if (references.containsKey(key)) {
            return references.get(key).createReference(referrent);
        }

        if (!categoryMap.containsKey(key)) {
            return null;
        }

        int category = categoryMap.get(key);
        if (category < 0) {
            return null;
        }

        CategoryHolder holder = categories.get(category);
        if (holder.config.isLruEnabled()) {
            Bitmap img = holder.bitmapLruCache.get(key);

            if (img != null) {
                BaseBitmapReference baseBitmapReference = new BaseBitmapReference(this, key, img);
                references.put(key, baseBitmapReference);
                return baseBitmapReference.createReference(referrent);
            }
        }

        return null;
    }

    public synchronized BitmapReference referenceBitmap(String key, Bitmap bitmap, Object referrent) {
        if (references.containsKey(key)) {
            return references.get(key).createReference(referrent);
        }
        int category = clasificator.getType(bitmap);
        BaseBitmapReference baseBitmapReference = new BaseBitmapReference(this, key, bitmap);
        references.put(key, baseBitmapReference);
        categoryMap.put(key, category);
        return baseBitmapReference.createReference(referrent);
    }

    /* package */
    synchronized void onReferenceDie(BaseBitmapReference reference) {
        references.remove(reference.key);
        int category = clasificator.getType(reference.bitmap);
        if (category < 0) {
            return;
        }
        CategoryHolder holder = categories.get(category);
        if (holder.config.isLruEnabled()) {
            holder.bitmapLruCache.put(reference.getKey(), reference.bitmap);
        }
    }

    public synchronized Bitmap findExactSize(int w, int h) {
        int category = clasificator.getType(w, h);
        if (category < 0) {
            return null;
        }
        CategoryHolder holder = categories.get(category);
        if (!holder.config.isFreeEnabled()) {
            return null;
        }
        Iterator<Map.Entry<String, Bitmap>> bitmapIterator = holder.bitmapFreeCache.snapshot().entrySet().iterator();
        while (bitmapIterator.hasNext()) {
            Map.Entry<String, Bitmap> b = bitmapIterator.next();
            if (b.getValue().getWidth() == w && b.getValue().getHeight() == h) {
                bitmapIterator.remove();
                return b.getValue();
            }
        }

        return null;
    }

    public synchronized void putFree(Bitmap free) {
        int category = clasificator.getType(free);
        if (category < 0) {
            return;
        }
        // TODO: Implement
        // freeBitmaps.add(free);
    }

    private class CategoryHolder {
        private BitmapClasificator.CacheConfig config;
        private LruCache<String, Bitmap> bitmapLruCache;
        private LruCache<String, Bitmap> bitmapFreeCache;

        private CategoryHolder(BitmapClasificator.CacheConfig config, LruCache<String, Bitmap> bitmapLruCache, LruCache<String, Bitmap> bitmapFreeCache) {
            this.config = config;
            this.bitmapLruCache = bitmapLruCache;
            this.bitmapFreeCache = bitmapFreeCache;
        }
    }
}