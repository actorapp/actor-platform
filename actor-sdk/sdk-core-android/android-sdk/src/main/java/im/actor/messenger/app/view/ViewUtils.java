package im.actor.messenger.app.view;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;

import im.actor.messenger.app.util.Screen;

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
        Animation a = new ExpandMentionsAnimation(v, targetHeight, initialHeight);

        a.setDuration((newRowsCount > oldRowsCount ? targetHeight : initialHeight / Screen.dp(1)));
        a.setInterpolator(MaterialInterpolator.getInstance());
        v.startAnimation(a);
    }


    private static class ExpandMentionsAnimation extends Animation {
        private final View v;
        private final int targetHeight;
        private final int initialHeight;
        private int currentHeight;

        public ExpandMentionsAnimation(View v, int targetHeight, int initialHeight) {
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
}
