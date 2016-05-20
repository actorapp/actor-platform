package im.actor.sdk.util;

import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.AbsListView;

import im.actor.sdk.view.MaterialInterpolator;

public class ViewUtils {

    public static void goneView(View view) {
        goneView(view, true);
    }

    public static void goneView(final View view, boolean isAnimated) {
        goneView(view, isAnimated, true);
    }

    public static void goneView(final View view, boolean isAnimated, boolean isSlow) {
        if (view == null) {
            return;
        }
        if (isAnimated) {
            if (view.getVisibility() != View.GONE) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(isSlow ? 300 : 150);
                alphaAnimation.setInterpolator(MaterialInterpolator.getInstance());
                view.clearAnimation();
                view.startAnimation(alphaAnimation);
                view.setVisibility(View.GONE);
            }
        } else {
            view.setVisibility(View.GONE);
        }
    }

    public static void hideView(View view) {
        hideView(view, true);
    }

    public static void hideView(final View view, boolean isAnimated) {
        hideView(view, isAnimated, true);
    }

    public static void hideView(final View view, boolean isAnimated, boolean isSlow) {
        if (view == null) {
            return;
        }

        if (isAnimated) {
            if (view.getVisibility() != View.INVISIBLE) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(isSlow ? 300 : 150);
                alphaAnimation.setInterpolator(MaterialInterpolator.getInstance());
                view.clearAnimation();
                view.startAnimation(alphaAnimation);
                view.setVisibility(View.INVISIBLE);
            }
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public static void zoomOutView(final View view) {
        if (view == null) {
            return;
        }


        if (view.getVisibility() != View.INVISIBLE) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0, 1f, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(150);
            scaleAnimation.setInterpolator(MaterialInterpolator.getInstance());
            view.clearAnimation();
            view.startAnimation(scaleAnimation);
            view.setVisibility(View.INVISIBLE);
        }

    }

    public static void zoomInView(final View view) {
        if (view == null) {
            return;
        }


        if (view.getVisibility() != View.VISIBLE) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1f, 0, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(150);
            scaleAnimation.setInterpolator(MaterialInterpolator.getInstance());
            view.clearAnimation();
            view.startAnimation(scaleAnimation);
            view.setVisibility(View.VISIBLE);
        }

    }

    public static void showView(View view) {
        showView(view, true);
    }

    public static void showView(final View view, boolean isAnimated) {
        showView(view, isAnimated, true);
    }

    public static void showView(final View view, boolean isAnimated, boolean isSlow) {
        if (view == null) {
            return;
        }

        if (isAnimated) {
            if (view.getVisibility() != View.VISIBLE) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(isSlow ? 300 : 150);
                alphaAnimation.setInterpolator(MaterialInterpolator.getInstance());
                view.clearAnimation();
                view.startAnimation(alphaAnimation);
                view.setVisibility(View.VISIBLE);
            }
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void expandMentions(final View v, final int oldRowsCount, final int newRowsCount) {
        if (newRowsCount == oldRowsCount) {
            return;
        }

        v.measure(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        int newRowsHeight = Screen.dp(48) * newRowsCount + newRowsCount;

        final int targetHeight = (newRowsHeight) > Screen.dp(96 + 2) ? Screen.dp(122) : newRowsHeight;
        final int initialHeight = new Integer(v.getLayoutParams().height);

        v.getLayoutParams().height = initialHeight;
        v.setVisibility(View.VISIBLE);
        Animation a = new ExpandAnimation(v, targetHeight, initialHeight);

        a.setDuration((newRowsCount > oldRowsCount ? targetHeight : initialHeight / Screen.dp(1)));
        a.setInterpolator(MaterialInterpolator.getInstance());
        v.startAnimation(a);
    }

    public static int blendColors(int color1, int color2, float amount, boolean inverse) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        final byte BLUE_CHANNEL = 0;

        if (inverse) {
            amount = 1.0f - amount;
        }
        final float inverseAmount = 1.0f - amount;

        int a = ((int) (((float) (color1 >> ALPHA_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> ALPHA_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int r = ((int) (((float) (color1 >> RED_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> RED_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int g = ((int) (((float) (color1 >> GREEN_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> GREEN_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int b = ((int) (((float) (color1 & 0xff) * amount) +
                ((float) (color2 & 0xff) * inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }


    private static class ExpandAnimation extends Animation {
        private final View v;
        private final int targetHeight;
        private final int initialHeight;
        private int currentHeight;

        public ExpandAnimation(View v, int targetHeight, int initialHeight) {
            this.v = v;
            this.targetHeight = targetHeight;
            this.initialHeight = initialHeight;
            this.currentHeight = initialHeight;

        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (targetHeight > initialHeight) {
                currentHeight =
                        (int) ((targetHeight * interpolatedTime) - initialHeight * interpolatedTime + initialHeight);
            } else {
                currentHeight =
                        (int) (initialHeight - (initialHeight * interpolatedTime) - targetHeight * (1f - interpolatedTime) + targetHeight);
            }

            v.getLayoutParams().height = currentHeight;
            v.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }


    }

    public static void elevateView(final View view) {
        elevateView(view, true);
    }

    public static void elevateView(final View view, float scale) {
        elevateView(view, true, scale);
    }

    public static void elevateView(final View view, boolean isAnimated) {
        elevateView(view, isAnimated, 1.1f);
    }
    public static void elevateView(final View view, boolean isAnimated, float scale) {
        if (view == null) {
            return;
        }


        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, scale, 1.0f, scale, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
        scaleAnimation.setDuration(isAnimated ? 150 : 0);
        scaleAnimation.setInterpolator(MaterialInterpolator.getInstance());
        scaleAnimation.setFillAfter(true);
        view.clearAnimation();
        view.startAnimation(scaleAnimation);

    }

    public static void demoteView(final View view) {
        demoteView(view, true);
    }

    public static void demoteView(final View view, boolean isAnimated) {
        if (view == null) {
            return;
        }

        if (isAnimated) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.1f, 1.0f, 1.1f, 1.0f, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
            scaleAnimation.setDuration(isAnimated ? 150 : 0);
            scaleAnimation.setInterpolator(MaterialInterpolator.getInstance());
            scaleAnimation.setFillAfter(true);
            view.clearAnimation();
            view.startAnimation(scaleAnimation);
        }
    }

    public static void expand(final View v, int targetHeight) {

        v.measure(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);

        final int initialHeight = new Integer(v.getLayoutParams().height);

        v.getLayoutParams().height = initialHeight;
        Animation a = new ExpandAnimation(v, targetHeight, initialHeight);

        a.setDuration(/*(targetHeight > 0 ? targetHeight : initialHeight / Screen.dp(1))*/150);
        a.setInterpolator(MaterialInterpolator.getInstance());
        v.startAnimation(a);
        if (targetHeight == 0) {
            v.setVisibility(View.INVISIBLE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    public static void wave(View[] layers, float scale, int duration, float offset){
        int step = 0;

        for (View l:layers) {
            wave(l, scale, duration, ((float)1/layers.length) * step * offset);
            step++;
        }
    }

    public static void wave(final View layer, float scale, int duration, float stepOffset){
        final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, scale, 1.0f, scale, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
        scaleAnimation.setDuration(duration);

        scaleAnimation.setInterpolator(new OffsetCycleInterpolator(stepOffset));
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        layer.clearAnimation();
        layer.startAnimation(scaleAnimation);
    }




}
