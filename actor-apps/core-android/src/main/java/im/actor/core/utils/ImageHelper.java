/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper {
    private static final int MAX_PIXELS = 1200 * 1200;
    private static final int MAX_PIXELS_HQ = 1500 * 1500;

    private static final int JPEG_QUALITY = 80;
    private static final int JPEG_QUALITY_HQ = 90;
    private static final int JPEG_QUALITY_LOW = 55;

    public static void save(Bitmap bitmap, String fileName) throws IOException {
        save(bitmap, fileName, Bitmap.CompressFormat.JPEG, JPEG_QUALITY_HQ);
    }

    public static byte[] save(Bitmap bitmap) {
        return save(bitmap, Bitmap.CompressFormat.JPEG, JPEG_QUALITY_LOW);
    }

    public static Bitmap loadOptimizedHQ(String fileName) {
        int scale = getScaleFactor(getImageSize(fileName), MAX_PIXELS_HQ);

        BitmapFactory.Options o = new BitmapFactory.Options();

        o.inScaled = false;
        o.inSampleSize = scale;
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;

        if (Build.VERSION.SDK_INT >= 10) {
            o.inPreferQualityOverSpeed = true;
        }

        if (Build.VERSION.SDK_INT >= 11) {
            o.inMutable = true;
        }

        if (!new File(fileName).exists()) {
            return null;
        }

        Bitmap res = BitmapFactory.decodeFile(fileName, o);
        if (res == null) {
            return null;
        }

        try {
            ExifInterface exif = new ExifInterface(fileName);
            String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = 0;
            if (exifOrientation != null) {
                orientation = Integer.parseInt(exifOrientation);
            }
            res = fixExif(res, orientation);
        } catch (IOException e) {
            // e.printStackTrace();
        }

        return res;
    }

    private static Bitmap fixExif(Bitmap src, int exifOrientation) {
        try {
            final Matrix bitmapMatrix = new Matrix();
            switch (exifOrientation) {
                case 1:
                    break;  // top left
                case 2:
                    bitmapMatrix.postScale(-1, 1);
                    break;  // top right
                case 3:
                    bitmapMatrix.postRotate(180);
                    break;  // bottom right
                case 4:
                    bitmapMatrix.postRotate(180);
                    bitmapMatrix.postScale(-1, 1);
                    break;  // bottom left
                case 5:
                    bitmapMatrix.postRotate(90);
                    bitmapMatrix.postScale(-1, 1);
                    break;  // left top
                case 6:
                    bitmapMatrix.postRotate(90);
                    break;  // right top
                case 7:
                    bitmapMatrix.postRotate(270);
                    bitmapMatrix.postScale(-1, 1);
                    break;  // right bottom
                case 8:
                    bitmapMatrix.postRotate(270);
                    break;  // left bottom
                default:
                    break;  // Unknown
            }

            // Create new bitmap.
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), bitmapMatrix, false);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    private static int getScaleFactor(BitmapSize size, int maxPixels) {
        int scale = 1;
        int scaledW = size.getWidth();
        int scaledH = size.getHeight();
        while (scaledW * scaledH > maxPixels) {
            scale *= 2;
            scaledH /= 2;
            scaledW /= 2;
        }
        return scale;
    }

    private static BitmapSize getImageSize(String fileName) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, o);
        if (o.outWidth == 0 || o.outHeight == 0) {
            return null;
        }

        int w = o.outWidth;
        int h = o.outHeight;

        try {
            ExifInterface exif = new ExifInterface(fileName);
            String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (exifOrientation != null) {
                if (exifOrientation.equals("5") ||
                        exifOrientation.equals("6") ||
                        exifOrientation.equals("7") ||
                        exifOrientation.equals("8")) {
                    w = o.outHeight;
                    h = o.outWidth;

                }
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }

        return new BitmapSize(w, h);
    }

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

    private static void save(Bitmap src, String fileName, Bitmap.CompressFormat format, int quality) throws IOException {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fileName);
            src.compress(format, quality, outputStream);
            outputStream.close();
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

    public static Bitmap scaleFit(Bitmap src, int maxW, int maxH) {
        float ratio = Math.min(maxW / (float) src.getWidth(), maxH / (float) src.getHeight());
        int newW = (int) (src.getWidth() * ratio);
        int newH = (int) (src.getHeight() * ratio);
        return scale(src, newW, newH);
    }

    private static Bitmap scale(Bitmap src, int dw, int dh) {
        Bitmap res = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888);
        scale(src, res);
        return res;
    }

    private static void scale(Bitmap src, Bitmap dest) {
        scale(src, dest, Color.TRANSPARENT);
    }

    private static void scale(Bitmap src, Bitmap dest, int clearColor) {
        scale(src, dest, clearColor, 0, 0, src.getWidth(), src.getHeight(), 0, 0, dest.getWidth(), dest.getHeight());
    }

    private static void scale(Bitmap src, Bitmap dest, int clearColor,
                              int x, int y, int sw, int sh,
                              int dx, int dy,
                              int dw, int dh) {
        dest.eraseColor(clearColor);
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(src, new Rect(x + 1, y + 1, sw - 1, sh - 1), new Rect(dx, dy, dw, dh), paint);
        canvas.setBitmap(null);
    }

    private static class BitmapSize {
        private int width;
        private int height;

        private BitmapSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}