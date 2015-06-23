package im.actor.messenger.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import im.actor.messenger.R;

/**
 * Created by ex3ndr on 01.01.15.
 */
public class TintImageView extends View {

    private static final long TINT_ANIMATION_TIME = 200;

    private Paint PAINT = new Paint();

    private Bitmap baseBitmap;
    private int currentTintColor = Color.WHITE;

    private int startTintColor = Color.WHITE;
    private int destTintColor = Color.WHITE;
    private long animationStart = 0;

    public TintImageView(Context context) {
        super(context);
    }

    public TintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TintImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TintImageView);

        setTint(a.getColor(R.styleable.TintImageView_tintColor, Color.WHITE));

        int resourceId = a.getResourceId(R.styleable.TintImageView_src, 0);
        if (resourceId != 0) {
            Drawable drawable = getResources().getDrawable(resourceId);
            if (drawable instanceof BitmapDrawable) {
                setDrawable(((BitmapDrawable) drawable).getBitmap());
            }
        }

        a.recycle();
    }

    public void setResource(int resId) {
        setDrawable(((BitmapDrawable) getResources().getDrawable(resId)).getBitmap());
    }

    public void setDrawable(Bitmap bitmap) {
        baseBitmap = bitmap;
        invalidate();
    }

    public void setTint(int color) {
        this.currentTintColor = color;
        this.startTintColor = color;
        this.destTintColor = color;
        this.animationStart = 0;

        invalidate();
    }

    public void animateTint(int destColor) {
        this.startTintColor = currentTintColor;
        this.destTintColor = destColor;
        this.animationStart = SystemClock.uptimeMillis();

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        long animationTime = SystemClock.uptimeMillis() - animationStart;
        if (animationTime > TINT_ANIMATION_TIME) {
            currentTintColor = destTintColor;
        } else {
            float alpha = animationTime / (float) TINT_ANIMATION_TIME;
            int sR = Color.red(startTintColor);
            int sG = Color.green(startTintColor);
            int sB = Color.blue(startTintColor);
            int sA = Color.alpha(startTintColor);

            int dR = Color.red(destTintColor);
            int dG = Color.green(destTintColor);
            int dB = Color.blue(destTintColor);
            int dA = Color.alpha(destTintColor);

            int r = (int) (sR * (1 - alpha) + dR * (alpha));
            int g = (int) (sG * (1 - alpha) + dG * (alpha));
            int b = (int) (sB * (1 - alpha) + dB * (alpha));
            int a = (int) (sA * (1 - alpha) + dA * (alpha));

            currentTintColor = Color.argb(a, r, g, b);

            invalidate();
        }

        if (baseBitmap != null) {

            PorterDuffColorFilter filter = new PorterDuffColorFilter(currentTintColor, PorterDuff.Mode.SRC_IN);
            PAINT.setColorFilter(filter);

            int x = (getWidth() - baseBitmap.getWidth()) / 2;
            int y = (getHeight() - baseBitmap.getHeight()) / 2;

            canvas.drawBitmap(baseBitmap, x, y, PAINT);
        }
    }
}
