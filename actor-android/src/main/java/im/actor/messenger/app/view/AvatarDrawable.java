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
import im.actor.messenger.storage.GlobalSearch;
import im.actor.model.entity.Dialog;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 12.09.14.
 */
public class AvatarDrawable extends Drawable {

    public static AvatarDrawable create(GroupVM groupModel, float fontSize, Context context) {
        return new AvatarDrawable(groupModel.getName().get(), groupModel.getId(), fontSize, context);
    }

    public static AvatarDrawable create(Dialog dialogItem, float fontSize, Context context) {
        return new AvatarDrawable(dialogItem.getDialogTitle(), dialogItem.getPeer().getPeerId(), fontSize, context);
    }

    public static AvatarDrawable create(UserVM userModel, float fontSize, Context context) {
        return new AvatarDrawable(userModel.getName().get(), userModel.getId(), fontSize, context);
    }

//    public static AvatarDrawable create(Contact contact, float fontSize, Context context) {
//        return new AvatarDrawable(contact.getName(), contact.getUid(), fontSize, context);
//    }

    public static AvatarDrawable create(GlobalSearch dialogItem, float fontSize, Context context) {
        return new AvatarDrawable(dialogItem.getTitle(), dialogItem.getContId(), fontSize, context);
    }

    private int color;
    private String title;

    private Paint circlePaint;
    private TextPaint textPaint;
    private int textX;
    private int textY;

    public AvatarDrawable(String title, int id, float fontSize, Context context) {

        if (title == null) {
            title = "";
        } else if (title.length() == 0) {
            title = "";
        } else {
            title = title.substring(0, 1).toUpperCase();
        }

        int[] colors = new int[]{
                context.getResources().getColor(R.color.placeholder_0),
                context.getResources().getColor(R.color.placeholder_1),
                context.getResources().getColor(R.color.placeholder_2),
                context.getResources().getColor(R.color.placeholder_3),
                context.getResources().getColor(R.color.placeholder_4),
                context.getResources().getColor(R.color.placeholder_5),
                context.getResources().getColor(R.color.placeholder_6),
        };
        color = colors[Math.abs(id) % colors.length];

        if (id == 0) {
            color = context.getResources().getColor(R.color.placeholder_empty);
        }

        this.title = title;

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Fonts.load(context, "Regular"));
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSize, context.getResources().getDisplayMetrics()));
        textPaint.setColor(Color.WHITE);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(color);
        circlePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        Rect bounds = new Rect();
        textX = (int) ((right - left - textPaint.measureText(title, 0, title.length())) / 2);
        textPaint.getTextBounds(title, 0, title.length(), bounds);
        textY = (int) ((bottom - top - bounds.top - bounds.bottom) / 2);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() / 2, circlePaint);

        canvas.drawText(title, textX, textY, textPaint);
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
