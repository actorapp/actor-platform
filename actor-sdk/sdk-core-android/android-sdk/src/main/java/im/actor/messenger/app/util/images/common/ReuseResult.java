package im.actor.messenger.app.util.images.common;

import android.graphics.Bitmap;

/**
 * Result of reused image loading
 */
public class ReuseResult {
    private Bitmap res;
    private boolean isReused;

    /**
     * Creating ReuseResult
     * @param res loaded image
     * @param isReused is image reused
     */
    public ReuseResult(Bitmap res, boolean isReused) {
        this.res = res;
        this.isReused = isReused;
    }

    /**
     * Loaded image
     *
     * @return image
     */
    public Bitmap getRes() {
        return res;
    }

    /**
     * Is image reused
     *
     * @return reused flag
     */
    public boolean isReused() {
        return isReused;
    }
}
