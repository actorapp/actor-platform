package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;

import im.actor.messenger.R;

/**
 * Created by ex3ndr on 12.09.14.
 */
public class AvatarPlaceholderDrawable extends Drawable {

    private static TextPaint TEXT_PAINT;
    private static float TEXT_SIZE;
    private float selfTextSize;
    private static Paint CIRCLE_PAINT;
    private static int[] COLORS;
    private Context ctx;

    private String title;
    private int color;

    private int textX;
    private int textY;

    public AvatarPlaceholderDrawable(String title, int id, float selfTextSize, Context context) {
        this.ctx = context;
        this.selfTextSize = selfTextSize;
        if (title == null) {
            title = "";
        } else if (title.length() == 0) {
            title = "";
        } else {
            title = title.substring(0, 1).toUpperCase();
        }

        if (COLORS == null) {
            COLORS = new int[]{
                    context.getResources().getColor(R.color.placeholder_0),
                    context.getResources().getColor(R.color.placeholder_1),
                    context.getResources().getColor(R.color.placeholder_2),
                    context.getResources().getColor(R.color.placeholder_3),
                    context.getResources().getColor(R.color.placeholder_4),
                    context.getResources().getColor(R.color.placeholder_5),
                    context.getResources().getColor(R.color.placeholder_6),
            };
        }

        if (CIRCLE_PAINT == null) {
            CIRCLE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
            CIRCLE_PAINT.setStyle(Paint.Style.FILL);
        }

        if (TEXT_PAINT == null) {
            TEXT_PAINT = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            TEXT_PAINT.setTypeface(Fonts.regular());
            TEXT_PAINT.setColor(Color.WHITE);
            TEXT_PAINT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE, context.getResources().getDisplayMetrics()));
        }

        if (id == 0) {
            this.color = context.getResources().getColor(R.color.placeholder_empty);
        } else {
            this.color = COLORS[Math.abs(id) % COLORS.length];
        }

        this.title = title;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        Rect bounds = new Rect();
        if (TEXT_SIZE != selfTextSize) {
            TEXT_PAINT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, selfTextSize, ctx.getResources().getDisplayMetrics()));
            TEXT_SIZE = selfTextSize;
        }
        textX = (int) ((right - left - (TEXT_PAINT).measureText(title, 0, title.length())) / 2);
        (TEXT_PAINT).getTextBounds(title, 0, title.length(), bounds);
        textY = (int) ((bottom - top - bounds.top - bounds.bottom) / 2);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        CIRCLE_PAINT.setColor(color);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() / 2, CIRCLE_PAINT);
        if (TEXT_SIZE != selfTextSize) {
            TEXT_PAINT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, selfTextSize, ctx.getResources().getDisplayMetrics()));
            TEXT_SIZE = selfTextSize;
        }
        canvas.drawText(title, textX, textY, (TEXT_PAINT));
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
