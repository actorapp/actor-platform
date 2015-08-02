package im.actor.messenger.app.util.images.sources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import im.actor.messenger.app.util.images.common.*;

/**
 * Created by ex3ndr on 13.09.14.
 */
public class MemorySource extends ImageSource {
    private byte[] data;

    public MemorySource(byte[] data) {
        this.data = data;
    }

    @Override
    protected ImageMetadata loadMetadata() throws ImageLoadException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        o.inTempStorage = WorkCache.BITMAP_TMP.get();
        BitmapFactory.decodeByteArray(data, 0, data.length, o);
        if (o.outWidth == 0 || o.outHeight == 0) {
            throw new ImageLoadException("BitmapFactory.decodeFile: unable to load file");
        }

        int w = o.outWidth;
        int h = o.outHeight;

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

        return new ImageMetadata(w, h, 0, format);
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

        Bitmap res = BitmapFactory.decodeByteArray(data, 0, data.length, o);
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

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        o.inTempStorage = WorkCache.BITMAP_TMP.get();
        o.inPreferQualityOverSpeed = true;
        o.inBitmap = reuse;
        o.inMutable = true;
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;
        o.inSampleSize = 1;

        Bitmap res;
        try {
            res = BitmapFactory.decodeByteArray(data, 0, data.length, o);
        } catch (Throwable t) {
            throw new ImageLoadException(t);
        }
        if (res == null) {
            throw new ImageLoadException("BitmapFactory.decodeFile return null");
        }
        return new ReuseResult(res, true);
    }
}
