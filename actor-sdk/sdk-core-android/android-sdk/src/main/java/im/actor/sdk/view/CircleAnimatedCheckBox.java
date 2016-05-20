package im.actor.sdk.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.CheckBox;

import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.util.ViewUtils;

public class CircleAnimatedCheckBox extends CheckBox {


    private int centerY;
    private int centerX;
    private int outerRadius;
    private int pressedRingRadius;

    private Paint circlePaint;
    private Paint backgroundPaint;

    private float animationProgress = 0f;

    private int pressedRingWidth = Screen.dp(2);
    private int baseRingColor = Color.WHITE;
    private int baseBackColor = 0x66000000;
    private int selectedRingColor = 0xFF33b5e5;
    private int selectedBackColor = 0xcc33b5e5;
    private ObjectAnimator animator;
    private boolean isShow;
    private Drawable d;
    private int checkSize = 18;

    public CircleAnimatedCheckBox(Context context) {
        super(context);
        init(context);
    }

    public CircleAnimatedCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleAnimatedCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed) {
            showSelected();
        } else {
            hideSelected();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (animationProgress >= 0f && animationProgress <= 1f) {
            int toColorSelected = 0;
            int fromColorSelected = 0;
            int toColorBack = 0;
            int fromColorBack = 0;

            if (!isChecked()) {
                if (isShow) {
                    fromColorSelected = baseRingColor;
                    toColorSelected = selectedRingColor;

                    fromColorBack = baseBackColor;
                    toColorBack = baseBackColor;

                } else {
                    fromColorSelected = baseRingColor;
                    toColorSelected = baseRingColor;

                    fromColorBack = selectedBackColor;
                    toColorBack = baseBackColor;
                }
            } else {
                if (isShow) {
                    fromColorSelected = selectedRingColor;
                    toColorSelected = baseRingColor;

                    fromColorBack = selectedBackColor;
                    toColorBack = selectedBackColor;
                } else {
                    fromColorSelected = selectedRingColor;
                    toColorSelected = selectedRingColor;

                    fromColorBack = baseBackColor;
                    toColorBack = selectedBackColor;
                }
            }
            int ringColor = ViewUtils.blendColors(toColorSelected, fromColorSelected, animationProgress, false);
            int backColor = ViewUtils.blendColors(toColorBack, fromColorBack, animationProgress, true);
            circlePaint.setColor(ringColor);
            backgroundPaint.setColor(backColor);

        }

        canvas.drawCircle(centerX, centerY, pressedRingRadius + animationProgress * pressedRingWidth, backgroundPaint);
        canvas.drawCircle(centerX, centerY, pressedRingRadius + animationProgress * pressedRingWidth, circlePaint);

        int padding = (getWidth() - Screen.dp(checkSize)) / 2;

        d.setBounds(padding, padding, getWidth() - padding, getHeight() - padding);
        if (isChecked()) {
            d.draw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        outerRadius = Math.min(w, h) / 2;
        pressedRingRadius = outerRadius - pressedRingWidth - pressedRingWidth / 2;
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public void setAnimationProgress(float animationProgress) {
        this.animationProgress = animationProgress;
        this.invalidate();
    }


    private void hideSelected() {
        isShow = false;
        animator.setDuration(200);

        animator.setFloatValues(1f, 0f);
        animator.start();
    }

    private void showSelected() {
        isShow = true;
        animator.setDuration(100);
        animator.setFloatValues(animationProgress, 1f);
        animator.start();
    }

    private void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(null);
        }
        d = context.getResources().getDrawable(R.drawable.ic_check_white_18dp);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);

        circlePaint.setStrokeWidth(pressedRingWidth);
        animator = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 1f);
    }

}