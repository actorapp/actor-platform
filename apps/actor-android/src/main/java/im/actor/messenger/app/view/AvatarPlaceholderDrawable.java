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
    private static TextPaint FORCED_SIZE_TEXT_PAINT;
    private boolean forceNewTextSize;
    private static Paint CIRCLE_PAINT;
    private static int[] COLORS;

    private String title;
    private int color;

    private int textX;
    private int textY;

    public AvatarPlaceholderDrawable(String title, int id, float fontSize, Context context, boolean forceNewTextSize) {

        this.forceNewTextSize = forceNewTextSize;

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
            TEXT_PAINT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSize, context.getResources().getDisplayMetrics()));
            TEXT_PAINT.setColor(Color.WHITE);
        }
        if(forceNewTextSize){
            if(FORCED_SIZE_TEXT_PAINT == null){
                FORCED_SIZE_TEXT_PAINT = new TextPaint(TEXT_PAINT);
            }
            FORCED_SIZE_TEXT_PAINT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSize, context.getResources().getDisplayMetrics()));
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
        textX = (int) ((right - left - (forceNewTextSize? FORCED_SIZE_TEXT_PAINT :TEXT_PAINT).measureText(title, 0, title.length())) / 2);
        (forceNewTextSize? FORCED_SIZE_TEXT_PAINT :TEXT_PAINT).getTextBounds(title, 0, title.length(), bounds);
        textY = (int) ((bottom - top - bounds.top - bounds.bottom) / 2);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        CIRCLE_PAINT.setColor(color);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() / 2, CIRCLE_PAINT);

        canvas.drawText(title, textX, textY, (forceNewTextSize? FORCED_SIZE_TEXT_PAINT :TEXT_PAINT));
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
