package im.actor.sdk.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;

public class ListItemBackgroundView<T, L> extends AsyncView<T, L> {

    private static boolean isInited;
    private static Paint dividerPaint;
    private static int dividerHeight;
    private static Paint backgroundPaint;
    private static Paint backgroundPressedPaint;

    private boolean isDividerVisible = true;
    private int dividerPaddingLeft = 0;
    private int dividerPaddingRight = 0;

    public ListItemBackgroundView(Context context) {
        super(context);
        initStyles();
    }

    public ListItemBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStyles();
    }

    public ListItemBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStyles();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListItemBackgroundView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initStyles();
    }

    private void initStyles() {
        if (!isInited) {
            isInited = true;
            dividerPaint = new Paint();
            dividerPaint.setColor(ActorSDK.sharedActor().style.getDividerColor());
            dividerPaint.setStyle(Paint.Style.FILL);
            dividerHeight = getResources().getDimensionPixelSize(R.dimen.div_size);
            backgroundPaint = new Paint();
            backgroundPaint.setColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPressedPaint = new Paint();
            backgroundPressedPaint.setColor(ActorSDK.sharedActor().style.getDividerColor());
            backgroundPressedPaint.setStyle(Paint.Style.FILL);
        }

        setBackgroundResource(R.drawable.selector_fill);
    }

    public int getDividerPaddingLeft() {
        return dividerPaddingLeft;
    }

    public void setDividerPaddingLeft(int dividerPaddingLeft) {
        this.dividerPaddingLeft = dividerPaddingLeft;
        invalidate();
    }

    public int getDividerPaddingRight() {
        return dividerPaddingRight;
    }

    public void setDividerPaddingRight(int dividerPaddingRight) {
        this.dividerPaddingRight = dividerPaddingRight;
        invalidate();
    }

    public boolean isDividerVisible() {
        return isDividerVisible;
    }

    public void setDividerVisible(boolean dividerVisible) {
        isDividerVisible = dividerVisible;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isDividerVisible) {
            canvas.drawRect(
                    dividerPaddingLeft, getHeight() - dividerHeight,
                    getWidth() - dividerPaddingRight, getHeight(),
                    dividerPaint);
        }
    }
}
