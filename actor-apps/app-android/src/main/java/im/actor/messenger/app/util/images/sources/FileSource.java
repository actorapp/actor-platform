package im.actor.messenger.app.util.images.sources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Build;
import im.actor.messenger.app.util.images.common.*;

import java.io.File;
import java.io.IOException;

/**
 * File source
 */
public class FileSource extends ImageSource {

    private String fileName;

    /**
     * Creating new file source
     *
     * @param fileName File Name of picture
     */
    public FileSource(String fileName) {
        this.fileName = fileName;
    }

    /**
     * File name of image
     *
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    @Override
    protected ImageMetadata loadMetadata() throws ImageLoadException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        o.inTempStorage = WorkCache.BITMAP_TMP.get();
        BitmapFactory.decodeFile(fileName, o);
        if (o.outWidth == 0 || o.outHeight == 0) {
            throw new ImageLoadException("BitmapFactory.decodeFile: unable to load file");
        }

        int w = o.outWidth;
        int h = o.outHeight;

        ExifInterface exif = null;
        int orientationTag = 0;
        try {
            exif = new ExifInterface(fileName);
            String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (exifOrientation != null) {
                if (exifOrientation.equals("5") ||
                        exifOrientation.equals("6") ||
                        exifOrientation.equals("7") ||
                        exifOrientation.equals("8")) {
                    w = o.outHeight;
                    h = o.outWidth;

                }
                orientationTag = Integer.parseInt(exifOrientation);
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }

        ImageFormat format = ImageFormat.UNKNOWN;
        if ("image/jpeg".equals(o.outMimeType) || "image/jpg".equals(o.outMimeType)) {
            format = ImageFormat.JPEG;
        } else if ("image/gif".equals(o.outMimeType)) {
            format = ImageFormat.GIF;
        } else if ("image/bmp".equals(o.outMimeType)) {
            format = ImageFormat.BMP;
        } else if ("image/webp".equals(o.outMimeType)) {
            format = ImageFormat.WEBP;
        }

        return new ImageMetadata(w, h, orientationTag, format);
    }

    @Override
    public Bitmap loadBitmap() throws ImageLoadException {
        return loadBitmap(1);
    }

    @Override
    public Bitmap loadBitmap(int scale) throws ImageLoadException {
        BitmapFactory.Options o = new BitmapFactory.Options();

        o.inScaled = false;
        o.inTempStorage = WorkCache.BITMAP_TMP.get();
        o.inSampleSize = scale;
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;

        if (Build.VERSION.SDK_INT >= 10) {
            o.inPreferQualityOverSpeed = true;
        }

        if (Build.VERSION.SDK_INT >= 11) {
            o.inMutable = true;
        }

        if (!new File(fileName).exists()) {
            throw new ImageLoadException("File not exists");
        }

        Bitmap res = BitmapFactory.decodeFile(fileName, o);
        if (res == null) {
            throw new ImageLoadException("BitmapFactory.decodeFile return null");
        }
        return res;
    }

    @Override
    public ReuseResult loadBitmap(Bitmap reuse) throws ImageLoadException {
        if (Build.VERSION.SDK_INT < 11) {
            throw new ImageLoadException("Bitmap reuse not available before HONEYCOMB");
        }

        if (!new File(fileName).exists()) {
            throw new ImageLoadException("File not exists");
        }

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        o.inTempStorage = WorkCache.BITMAP_TMP.get();
        o.inPreferQualityOverSpeed = true;
        o.inBitmap = reuse;
        o.inMutable = true;
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;
        o.inSampleSize = 1;

        Bitmap res = BitmapFactory.decodeFile(fileName, o);
        if (res == null) {
            throw new ImageLoadException("BitmapFactory.decodeFile return null");
        }
        return new ReuseResult(res, true);
    }
}
