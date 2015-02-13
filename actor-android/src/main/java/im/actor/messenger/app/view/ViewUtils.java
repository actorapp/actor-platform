package im.actor.messenger.app.view;

import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * Created by ex3ndr on 05.10.14.
 */
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
                // alphaAnimation.setFillAfter(true);
                view.clearAnimation();
                view.startAnimation(alphaAnimation);
                view.setVisibility(View.VISIBLE);
            }
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }
}
