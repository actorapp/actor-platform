package com.droidkit.images.loading;

import com.droidkit.images.cache.BitmapReference;

/**
 * Created by ex3ndr on 20.08.14.
 */
public interface ReceiverCallback {
    public void onImageLoaded(BitmapReference bitmap);

    public void onImageCleared();

    public void onImageError();
}
