package im.actor.messenger.app.util.images.ops;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import im.actor.messenger.app.util.images.common.WorkCache;

/**
 * Created by ex3ndr on 20.09.14.
 */
public final class ImageRotating {

    public static Bitmap fixExif(Bitmap src, int rotationTag) {
        if (rotationTag <= 1 || rotationTag > 8) {
            return src;
        }

        int nw;
        int nh;
        switch (rotationTag) {
            default:
            case 1:
            case 2:
            case 3:
            case 4:
                nw = src.getWidth();
                nh = src.getHeight();
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                nw = src.getHeight();
                nh = src.getWidth();
                break;
        }

        Bitmap res = Bitmap.createBitmap(nw, nh, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(res);
        Paint paint = WorkCache.PAINT.get();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        Matrix matrix = new Matrix();
        switch (rotationTag) {
            case 2:
                matrix.setScale(-1, 1, src.getWidth() / 2, src.getHeight() / 2);
                break;
            case 3:
                matrix.setRotate(180, src.getWidth() / 2, src.getHeight() / 2);
                break;
            case 4:
                matrix.setScale(1, -1, src.getWidth() / 2, src.getHeight() / 2);
                break;
            case 5:
                matrix.postRotate(90);
                matrix.postTranslate(src.getHeight(), 0);
                matrix.postScale(-1, 1, src.getHeight() / 2, src.getWidth() / 2);
                break;
            case 6:
                matrix.postRotate(90);
                matrix.postTranslate(src.getHeight(), 0);
                break;
            case 7:
                matrix.postRotate(-90);
                matrix.postTranslate(0, src.getWidth());
                matrix.postScale(-1, 1, src.getHeight() / 2, src.getWidth() / 2);
                break;
            case 8:
                matrix.postRotate(-90);
                matrix.postTranslate(0, src.getWidth());
                break;
        }
        canvas.drawBitmap(src, matrix, paint);
        canvas.setBitmap(null);
        src.recycle();
        return res;
    }

    private ImageRotating() {

    }
}
