package im.actor.messenger.app.util.images.common;

/**
 * Image metadata
 */
public class ImageMetadata {
    private int w;
    private int h;
    private ImageFormat format;
    private int exifOrientation;

    /**
     * Creating of ImageMetadata
     *
     * @param w      width of image
     * @param h      height of image
     * @param format format of image
     */
    public ImageMetadata(int w, int h, int exifOrientation, ImageFormat format) {
        this.w = w;
        this.h = h;
        this.format = format;
        this.exifOrientation = exifOrientation;
    }

    /**
     * Exif orientation tag
     *
     * @return orientation tag
     */
    public int getExifOrientation() {
        return exifOrientation;
    }

    /**
     * Width of image
     *
     * @return width
     */
    public int getW() {
        return w;
    }

    /**
     * Height of image
     *
     * @return height
     */
    public int getH() {
        return h;
    }

    /**
     * Format of image
     *
     * @return format
     */
    public ImageFormat getFormat() {
        return format;
    }
}
