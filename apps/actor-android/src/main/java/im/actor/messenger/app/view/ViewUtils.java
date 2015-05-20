package im.actor.messenger.app.view;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;

import im.actor.messenger.app.util.Screen;

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
                view.clearAnimation();
                view.startAnimation(alphaAnimation);
                view.setVisibility(View.VISIBLE);
            }
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void expandMentions(final View v, final int oldRowsCount, final int newRowsCount) {
        if(newRowsCount==oldRowsCount){
            return;
        }
        v.measure(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = (Screen.dp(29) * newRowsCount)>Screen.dp(87)?Screen.dp(100):Screen.dp(29) * newRowsCount;
        final int initialHeight = Screen.dp(29) *  oldRowsCount;

        v.getLayoutParams().height = initialHeight;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation(){
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(newRowsCount>oldRowsCount){
                    v.getLayoutParams().height =
                            (int)((targetHeight * interpolatedTime) - initialHeight*interpolatedTime + initialHeight);
                    v.requestLayout();
                }else{
                    v.getLayoutParams().height =
                            (int)(initialHeight - (initialHeight * interpolatedTime) - targetHeight*(1f-interpolatedTime) + targetHeight);
                    v.requestLayout();
                }

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) ((newRowsCount>oldRowsCount?targetHeight:initialHeight / Screen.dp(1))));
        a.setInterpolator(MaterialInterpolator.getInstance());
        a.setFillAfter(true);
        v.startAnimation(a);

    }


}
