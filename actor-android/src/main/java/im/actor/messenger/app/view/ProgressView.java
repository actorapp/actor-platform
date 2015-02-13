package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import im.actor.messenger.util.support.Arrays;

public class ProgressView extends ProgressBar {
    RectF rectF;
    Paint p;
    int start = 0;
    int maxvalue = 320;
    int value = 320;
    int[] currentColor = {0, 0, 0};
    boolean reverse = false;
    int nextcolor = 1;

    final int[][] colors = {
            {224, 187, 63},
            {224, 46, 25},
            {39, 105, 227},
            {51, 130, 49}
    };

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        p = new Paint();
        p.setStrokeWidth(6);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.argb(255, colors[0][0], colors[0][1], colors[0][2]));
        currentColor = Arrays.copyOf(colors[0], colors[0].length);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        rectF = new RectF(0 + 5, 0 + 5, w - 5, h - 5);
    }

    @Override
    protected void onDraw(Canvas c) {
        if (reverse)
            start += 15;
        else
            start += 5;

        if (start == 360) {
            start = 1;
        }
        if (!reverse)
            value -= 10;
        else
            value += 10;
        if (value == 0 || value == maxvalue) {
            reverse = !reverse;
        }
        transformColor();
        p.setColor(Color.argb(255, currentColor[0], currentColor[1], currentColor[2]));
        c.drawArc(rectF, start, maxvalue - value, false, p);
        invalidate();
    }

    private void transformColor() {
        changeColors(0);
        changeColors(1);
        changeColors(2);
        if (currentColor[0] == colors[nextcolor][0] && currentColor[1] == colors[nextcolor][1] && currentColor[2] == colors[nextcolor][2]) {
            if (nextcolor == 3)
                nextcolor = 0;
            else
                nextcolor++;
        }
    }

    private void changeColors(int i) {
        if (currentColor[i] > colors[nextcolor][i]) {
            currentColor[i] -= 1;
        }
        if (currentColor[i] < colors[nextcolor][i]) {
            currentColor[i] += 1;
        }
    }
}