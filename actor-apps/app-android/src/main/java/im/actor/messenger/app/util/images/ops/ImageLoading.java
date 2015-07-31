package im.actor.messenger.app.util.images.ops;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import im.actor.messenger.app.util.images.common.*;
import im.actor.messenger.app.util.images.sources.FileSource;
import im.actor.messenger.app.util.images.sources.ImageSource;
import im.actor.messenger.app.util.images.sources.MemorySource;
import im.actor.messenger.app.util.images.sources.UriSource;
import im.actor.messenger.app.util.images.BitmapUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Image loading
 */
public class ImageLoading {

    private static final int MAX_PIXELS = 1200 * 1200;
    private static final int MAX_PIXELS_HQ = 1500 * 1500;

    public static final int JPEG_QUALITY = 80;
    public static final int JPEG_QUALITY_HQ = 90;
    public static final int JPEG_QUALITY_LOW = 55;

    /**
     * Loading bitmap without any modifications
     *
     * @param fileName Image file name
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmap(String fileName) throws ImageLoadException {
        return loadBitmap(new FileSource(fileName));
    }

    /**
     * Loading bitmap without any modifications
     *
     * @param uri     content uri for bitmap
     * @param context Application Context
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmap(Uri uri, Context context) throws ImageLoadException {
        return loadBitmap(new UriSource(uri, context));
    }

    /**
     * Loading bitmap without any modifications
     *
     * @param data image file contents
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmap(byte[] data) throws ImageLoadException {
        return loadBitmap(new MemorySource(data));
    }

    /**
     * Loading bitmap with scaling
     *
     * @param fileName Image file name
     * @param scale    divider of size, might be factor of two
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmap(String fileName, int scale) throws ImageLoadException {
        return loadBitmap(new FileSource(fileName), scale);
    }

    /**
     * Loading bitmap with scaling
     *
     * @param fileName Image file name
     * @param minW     minimal width
     * @param minH     minimal height
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmap(String fileName, int minW, int minH) throws ImageLoadException {
        return loadBitmapOptimized(new FileSource(fileName), minW, minH);
    }

    /**
     * Loading bitmap with optimized loaded size less than 1.4 MPX
     *
     * @param uri     content uri for bitmap
     * @param context Application Context
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmapOptimized(Uri uri, Context context) throws ImageLoadException {
        return loadBitmapOptimized(uri, context, MAX_PIXELS);
    }

    /**
     * Loading bitmap with optimized loaded size less than 1.4 MPX
     *
     * @param fileName Image file name
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmapOptimized(String fileName) throws ImageLoadException {
        return loadBitmapOptimized(fileName, MAX_PIXELS);
    }

    /**
     * Loading bitmap with optimized loaded size less than 2.2 MPX
     *
     * @param fileName Image file name
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmapOptimizedHQ(String fileName) throws ImageLoadException {
        return loadBitmapOptimized(fileName, MAX_PIXELS_HQ);
    }

    /**
     * Loading bitmap with optimized loaded size less than specific pixels count
     *
     * @param fileName Image file name
     * @param limit    maximum pixels size
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmapOptimized(String fileName, int limit) throws ImageLoadException {
        return loadBitmapOptimized(new FileSource(fileName), limit);
    }

    /**
     * Loading bitmap with optimized loaded size less than specific pixels count
     *
     * @param uri     content uri for bitmap
     * @param context Application Context
     * @param limit   maximum pixels size
     * @return loaded bitmap (always not null)
     * @throws ImageLoadException if it is unable to load file
     */
    public static Bitmap loadBitmapOptimized(Uri uri, Context context, int limit) throws ImageLoadException {
        return loadBitmapOptimized(new UriSource(uri, context) {
        }, limit);
    }

    /**
     * Loading bitmap with using reuse bitmap with the same size of source image.
     * If it is unable to load with reuse method tries to load without it.
     * Reuse works only in 3.0+
     *
     * @param fileName Image file name
     * @param dest     reuse bitmap
     * @return result of loading
     * @throws ImageLoadException if it is unable to load file
     */
    public static ReuseResult loadReuseExact(String fileName, Bitmap dest) throws ImageLoadException {
        return loadBitmapReuseExact(new FileSource(fileName), dest);
    }

    /**
     * Loading bitmap with using reuse bitmap with the same size of source image.
     * If it is unable to load with reuse method tries to load without it.
     * Reuse works only in 3.0+
     *
     * @param data image file contents
     * @param dest reuse bitmap
     * @return result of loading
     * @throws ImageLoadException if it is unable to load file
     */
    public static ReuseResult loadReuseExact(byte[] data, Bitmap dest) throws ImageLoadException {
        return loadBitmapReuseExact(new MemorySource(data), dest);
    }

    /**
     * Loading bitmap with using reuse bitmap with the same size of source image.
     * If it is unable to load with reuse method tries to load without it.
     * Reuse works only in 3.0+
     *
     * @param uri     content uri for bitmap
     * @param context Application Context
     * @param dest    reuse bitmap
     * @return result of loading
     * @throws ImageLoadException if it is unable to load file
     */
    public static ReuseResult loadReuseExact(Uri uri, Context context, Bitmap dest) throws ImageLoadException {
        return loadBitmapReuseExact(new UriSource(uri, context), dest);
    }

    /**
     * Loading bitmap with using reuse bitmap with the different size of source image.
     * If it is unable to load with reuse method tries to load without it.
     * Reuse works only for Android 4.4+
     *
     * @param fileName Image file name
     * @param dest     reuse bitmap
     * @return result of loading
     * @throws ImageLoadException if it is unable to load file
     */
    public static ReuseResult loadReuse(String fileName, Bitmap dest) throws ImageLoadException {
        return loadBitmapReuse(new FileSource(fileName), dest);
    }

    /**
     * Loading bitmap with using reuse bitmap with the different size of source image.
     * If it is unable to load with reuse method tries to load without it.
     * Reuse works only for Android 4.4+
     *
     * @param data image file contents
     * @param dest reuse bitmap
     * @return result of loading
     * @throws ImageLoadException if it is unable to load file
     */
    public static ReuseResult loadReuse(byte[] data, Bitmap dest) throws ImageLoadException {
        return loadBitmapReuse(new MemorySource(data), dest);
    }

    /**
     * Loading bitmap with using reuse bitmap with the different size of source image.
     * If it is unable to load with reuse method tries to load without it.
     * Reuse works only for Android 4.4+
     *
     * @param uri     content uri for bitmap
     * @param dest    destination bitmap
     * @param context Application Context
     * @return result of loading
     * @throws ImageLoadException if it is unable to load file
     */
    public static ReuseResult loadReuse(Uri uri, Context context, Bitmap dest) throws ImageLoadException {
        return loadBitmapReuse(new UriSource(uri, context), dest);
    }


    /**
     * Saving image in jpeg to byte array with quality 80
     *
     * @param src source image
     * @return saved data
     * @throws ImageSaveException if it is unable to save image
     */
    public static byte[] save(Bitmap src) throws ImageSaveException {
        return save(src, Bitmap.CompressFormat.JPEG, JPEG_QUALITY);
    }

    /**
     * Saving image in jpeg to byte array with better quality 90
     *
     * @param src source image
     * @return saved data
     * @throws ImageSaveException if it is unable to save image
     */
    public static byte[] saveHq(Bitmap src) throws ImageSaveException {
        return save(src, Bitmap.CompressFormat.JPEG, JPEG_QUALITY_HQ);
    }

    /**
     * Saving image in jpeg to byte array with specific quality
     *
     * @param src     source image
     * @param quality jpeg quality
     * @return saved data
     * @throws ImageSaveException if it is unable to save image
     */
    public static byte[] saveJpeg(Bitmap src, int quality) throws ImageSaveException {
        return save(src, Bitmap.CompressFormat.JPEG, quality);
    }

    /**
     * Saving image in png to byte array with specific quality
     *
     * @param src source image
     * @return saved data
     * @throws ImageSaveException if it is unable to save image
     */
    public static byte[] savePng(Bitmap src) throws ImageSaveException {
        return save(src, Bitmap.CompressFormat.PNG, 100);
    }

    /**
     * Saving image in jpeg to file with quality 80
     *
     * @param src      source image
     * @param fileName destination file name
     * @throws ImageSaveException if it is unable to save image
     */
    public static void save(Bitmap src, String fileName) throws ImageSaveException {
        saveJpeg(src, fileName, JPEG_QUALITY);
    }

    /**
     * Saving image in jpeg to file with quality 55
     *
     * @param src      source image
     * @param fileName destination file name
     * @throws ImageSaveException if it is unable to save image
     */
    public static void saveLq(Bitmap src, String fileName) throws ImageSaveException {
        saveJpeg(src, fileName, JPEG_QUALITY_LOW);
    }

    /**
     * Saving image in jpeg to file with better quality 90
     *
     * @param src      source image
     * @param fileName destination file name
     * @throws ImageSaveException if it is unable to save image
     */
    public static void saveHq(Bitmap src, String fileName) throws ImageSaveException {
        saveJpeg(src, fileName, JPEG_QUALITY_HQ);
    }

    /**
     * Saving image in jpeg to file with better quality 90
     *
     * @param src      source image
     * @param fileName destination file name
     * @throws ImageSaveException if it is unable to save image
     */
    public static void saveJpeg(Bitmap src, String fileName, int quality) throws ImageSaveException {
        save(src, fileName, Bitmap.CompressFormat.JPEG, quality);
    }

    /**
     * Saving image in png to file
     *
     * @param src      source image
     * @param fileName destination file name
     * @throws ImageSaveException if it is unable to save image
     */
    public static void savePng(Bitmap src, String fileName) throws ImageSaveException {
        save(src, fileName, Bitmap.CompressFormat.PNG, 100);
    }

    /**
     * Saving image in bmp to file
     *
     * @param src      source image
     * @param fileName destination file name
     * @throws ImageSaveException if it is unable to save image
     */
    public static void saveBmp(Bitmap src, String fileName) throws ImageSaveException {
        try {
            BitmapUtil.save(src, fileName);
        } catch (IOException e) {
            throw new ImageSaveException(e);
        }
    }

    /**
     * Calculating allocated memory for bitmap
     *
     * @param bitmap source bitmap
     * @return size in bytes
     */
    public static int bitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    /**
     * Loading bitmap from ImageSource
     *
     * @param source image source
     * @return loaded bitmap
     * @throws ImageLoadException if it is unable to load image
     */
    private static Bitmap loadBitmap(ImageSource source) throws ImageLoadException {
        return source.loadBitmap();
    }

    /**
     * Loading bitmap from ImageSource with limit of amout of pixels
     *
     * @param source image source
     * @param limit  maximum pixels size
     * @return loaded bitmap
     * @throws ImageLoadException if it is unable to load image
     */
    private static Bitmap loadBitmapOptimized(ImageSource source, int limit) throws ImageLoadException {
        int scale = getScaleFactor(source.getImageMetadata(), limit);
        return loadBitmap(source, scale);
    }


    /**
     * Loading bitmap from ImageSource with limit of amout of pixels
     *
     * @param source image source
     * @param w      min width
     * @param h      min height
     * @return loaded bitmap
     * @throws ImageLoadException if it is unable to load image
     */
    private static Bitmap loadBitmapOptimized(ImageSource source, int w, int h) throws ImageLoadException {
        int scale = getScaleFactor(source.getImageMetadata(), w, h);
        return loadBitmap(source, scale);
    }

    /**
     * Loading bitmap from ImageSource with specific scale
     *
     * @param source image source
     * @param scale  divider of size, might be factor of two
     * @return loaded bitmap
     * @throws ImageLoadException if it is unable to load image
     */
    private static Bitmap loadBitmap(ImageSource source, int scale) throws ImageLoadException {
        return source.loadBitmap(scale);
    }

    /**
     * Loading image with reuse bitmap of same size as source
     *
     * @param source image source
     * @param dest   destination bitmap
     * @return loaded bitmap result
     * @throws ImageLoadException if it is unable to load image
     */
    private static ReuseResult loadBitmapReuseExact(ImageSource source, Bitmap dest) throws ImageLoadException {
        ImageMetadata metadata = source.getImageMetadata();
        boolean tryReuse = false;
        if (dest.isMutable()
                && dest.getWidth() == metadata.getW()
                && dest.getHeight() == metadata.getH()) {
            if (Build.VERSION.SDK_INT >= 19) {
                tryReuse = true;
            } else if (Build.VERSION.SDK_INT >= 11) {
                if (metadata.getFormat() == ImageFormat.JPEG || metadata.getFormat() == ImageFormat.PNG) {
                    tryReuse = true;
                }
            }
        }

        if (tryReuse) {
            return source.loadBitmap(dest);
        } else {
            return new ReuseResult(loadBitmap(source), false);
        }
    }

    /**
     * Loading image with reuse bitmap
     *
     * @param source image source
     * @param dest   destination bitmap
     * @return loaded bitmap result
     * @throws ImageLoadException if it is unable to load image
     */
    private static ReuseResult loadBitmapReuse(ImageSource source, Bitmap dest) throws ImageLoadException {
        ImageMetadata metadata = source.getImageMetadata();
        boolean tryReuse = false;
        if (dest.isMutable()) {
            if (Build.VERSION.SDK_INT >= 19) {
                tryReuse = dest.getAllocationByteCount() >= metadata.getW() * metadata.getH() * 4;
            } else if (Build.VERSION.SDK_INT >= 11) {
                if (metadata.getFormat() == ImageFormat.JPEG || metadata.getFormat() == ImageFormat.PNG) {
                    tryReuse = dest.getWidth() == metadata.getW()
                            && dest.getHeight() == metadata.getH();
                }
            }
        }

        if (tryReuse) {
            return source.loadBitmap(dest);
        } else {
            return new ReuseResult(loadBitmap(source), false);
        }
    }

    /**
     * Saving image to file
     *
     * @param src      source image
     * @param fileName destination file name
     * @param format   image format
     * @param quality  jpeg quality
     * @throws ImageSaveException if it is unable to save image
     */
    private static void save(Bitmap src, String fileName, Bitmap.CompressFormat format, int quality) throws ImageSaveException {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fileName);
            src.compress(format, quality, outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new ImageSaveException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Saving image to byte array
     *
     * @param src     source image
     * @param format  image format
     * @param quality jpeg quality
     * @return saved image
     */
    private static byte[] save(Bitmap src, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            src.compress(format, quality, outputStream);
            return outputStream.toByteArray();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Calculating scale factor with limit of pixel amount
     *
     * @param metadata  image metadata
     * @param maxPixels limit for pixels
     * @return scale factor
     */
    private static int getScaleFactor(ImageMetadata metadata, int maxPixels) {
        int scale = 1;
        int scaledW = metadata.getW();
        int scaledH = metadata.getH();
        while (scaledW * scaledH > maxPixels) {
            scale *= 2;
            scaledH /= 2;
            scaledW /= 2;
        }
        return scale;
    }

    /**
     * Calculating scale factor with limit of pixel amount
     *
     * @param metadata image metadata
     * @param minH     minimal height
     * @param minW     minimal width
     * @return scale factor
     */
    private static int getScaleFactor(ImageMetadata metadata, int minW, int minH) {
        int scale = 1;
        int scaledW = metadata.getW();
        int scaledH = metadata.getH();
        while (scaledW / 2 > minW && scaledH / 2 > minH) {
            scale *= 2;
            scaledH /= 2;
            scaledW /= 2;
        }
        return scale;
    }

    protected ImageLoading() {
    }
}
