package im.actor.sdk.util.images.sources;

import android.graphics.Bitmap;
import im.actor.sdk.util.images.common.ImageLoadException;
import im.actor.sdk.util.images.common.ImageMetadata;
import im.actor.sdk.util.images.common.ReuseResult;

/**
 * Source of image
 */
public abstract class ImageSource {

    private ImageMetadata imageMetadata;

    /**
     * Loading of image metadata
     *
     * @return metadata
     * @throws ImageLoadException if it is unable to load file
     */
    protected abstract ImageMetadata loadMetadata() throws ImageLoadException;

    /**
     * Getting of image metadata
     *
     * @return metadata
     * @throws ImageLoadException if it is unable to load file
     */
    public ImageMetadata getImageMetadata() throws ImageLoadException {
        if (imageMetadata == null) {
            imageMetadata = loadMetadata();
        }
        return imageMetadata;
    }

    /**
     * Loading unmodified image
     *
     * @return bitmap
     * @throws ImageLoadException if it is unable to load file
     */
    public abstract Bitmap loadBitmap() throws ImageLoadException;

    /**
     * Loading scaled image
     *
     * @param scale divider of size, might be factor of two
     * @return bitmap
     * @throws ImageLoadException if it is unable to load file
     */
    public abstract Bitmap loadBitmap(int scale) throws ImageLoadException;

    /**
     * Loading image with reuse
     *
     * @param reuse image for reuse
     * @return Reuse image loading result
     * @throws ImageLoadException
     */
    public abstract ReuseResult loadBitmap(Bitmap reuse) throws ImageLoadException;
}