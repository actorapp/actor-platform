package im.actor.messenger.app.util.images.sources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import im.actor.messenger.app.util.images.common.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class UriSource extends ImageSource {
    private Uri uri;
    private Context context;

    public UriSource(Uri uri, Context context) {
        this.uri = uri;
        this.context = context;
    }

    @Override
    protected ImageMetadata loadMetadata() throws ImageLoadException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        o.inTempStorage = WorkCache.BITMAP_TMP.get();
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, o);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ImageLoadException("Unable to load image from stream");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        if (o.outWidth == 0 || o.outHeight == 0) {
            throw new ImageLoadException("BitmapFactory.decodeFile: unable to load file");
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

        int w = o.outWidth;
        int h = o.outHeight;
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

        InputStream is = null;
        Bitmap res;
        try {
            is = context.getContentResolver().openInputStream(uri);
            res = BitmapFactory.decodeStream(is, null, o);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ImageLoadException("Unable to load image from stream");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

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

        InputStream is = null;
        Bitmap res;
        try {
            is = context.getContentResolver().openInputStream(uri);
            res = BitmapFactory.decodeStream(is, null, o);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ImageLoadException("Unable to load image from stream");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        if (res == null) {
            throw new ImageLoadException("BitmapFactory.decodeFile return null");
        }
        return new ReuseResult(res, true);
    }
}
