package im.actor.messenger.app.fragment.preview;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;

import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.MaterialInterpolator;

public class MediaFullscreenAnimationUtils {

        public static final int animationMultiplier = 1;
        public static int startDelay = 60;

        public static void animateForward(final View transitionView, Bitmap bitmap,
                                          int transitionLeft, int transitionTop,
                                          int transitionWidth, int transitionHeight, final Animator.AnimatorListener listener) {
            transitionView.clearAnimation();
            float bitmapWidth = bitmap.getWidth();
            float bitmapHeight = bitmap.getHeight();


            float screenWidth = Screen.getWidth();
            float screenHeight = Screen.getHeight() + (Build.VERSION.SDK_INT >= 21 ? Screen.getNavbarHeight() : 0);

            if (bitmapHeight > screenHeight || bitmapWidth > screenWidth) {
                if (bitmapWidth / screenWidth < bitmapHeight / screenHeight) {
                    bitmapWidth = bitmapWidth * (screenHeight / bitmapHeight);
                    bitmapHeight = screenHeight;
                } else {
                    bitmapHeight = bitmapHeight * (screenWidth / bitmapWidth);
                    bitmapWidth = screenWidth;
                }
            }

            float startScaleWidth = (float) transitionWidth / bitmapWidth;
            float startScaleHeight = (float) transitionHeight / bitmapHeight;
            /*transitionView.setLeft((int) (transitionLeft + (bitmapWidth * (startScaleWidth - 1) / 2)));
            transitionView.setTop((int) (transitionTop + (bitmapHeight * (startScaleHeight - 1) / 2)));
            transitionView.setScaleX(startScaleWidth);
            transitionView.setScaleY(startScaleHeight);*/
            transitionView.animate().setInterpolator(new MaterialInterpolator())
                    .setDuration(0)
                    .setStartDelay(0)
                    .alpha(1)
                    .x(transitionLeft + (bitmapWidth * (startScaleWidth - 1) / 2))
                    .y(transitionTop + (bitmapHeight * (startScaleHeight - 1) / 2))
                    .scaleX(startScaleWidth)
                    .scaleY(startScaleHeight)
                    .setListener(null)
                    .start();

            float endScale = 1;
            // float endScaleHeight = 1;
            float xPadding = 0;
            float yPadding = 0;
            if (bitmapWidth / screenWidth > bitmapHeight / screenHeight) {
                endScale = screenWidth / bitmapWidth;
                xPadding = (bitmapWidth * (endScale - 1) / 2);
                yPadding = screenHeight / 2 - (bitmapHeight / 2);
            } else {
                endScale = screenHeight / bitmapHeight;
                xPadding = screenWidth / 2 - (bitmapWidth / 2);
                yPadding = (bitmapHeight * (endScale - 1)) / 2;
            }
            final float finalXPadding = xPadding;
            final float finalEndScale = endScale;
            final float finalYPadding = yPadding;
            transitionView.post(new Runnable() {
                @Override
                public void run() {
                    transitionView.animate()
                            .setInterpolator(new MaterialInterpolator())
                            .setStartDelay(startDelay)
                            .setDuration(300 * animationMultiplier)
                            .setInterpolator(new MaterialInterpolator())
                            .x(finalXPadding)
                            .y(finalYPadding)
                            .scaleX(finalEndScale)
                            .scaleY(finalEndScale)
                            .setListener(listener)
                            .start();
                }
            });

        }

        public static void animateBack(final View transitionView, Bitmap bitmap,
                                       final int transitionLeft, final int transitionTop,
                                       int transitionWidth, int transitionHeight, final Animator.AnimatorListener listener) {
            transitionView.clearAnimation();
            float bitmapWidth = bitmap.getWidth();
            float bitmapHeight = bitmap.getHeight();


            float screenWidth = Screen.getWidth();
            float screenHeight = Screen.getHeight() + (Build.VERSION.SDK_INT >= 19 ? Screen.getNavbarHeight() : 0);

            if (bitmapHeight > screenHeight || bitmapWidth > screenWidth) {
                if (bitmapWidth / screenWidth < bitmapHeight / screenHeight) {
                    bitmapWidth = bitmapWidth * (screenHeight / bitmapHeight);
                    bitmapHeight = screenHeight;
                } else {
                    bitmapHeight = bitmapHeight * (screenWidth / bitmapWidth);
                    bitmapWidth = screenWidth;
                }
            }

            final float finishScaleWidth = (float) transitionWidth / bitmapWidth;
            final float finishScaleHeight = (float) transitionHeight / bitmapHeight;


            float endScale = 1;
            // float endScaleHeight = 1;
            float xPadding = 0;
            float yPadding = 0;
            if (bitmapWidth / screenWidth > bitmapHeight / screenHeight) {
                endScale = screenWidth / bitmapWidth;
                xPadding = (bitmapWidth * (endScale - 1) / 2);
                yPadding = screenHeight / 2 - (bitmapHeight / 2);
            } else {
                endScale = screenHeight / bitmapHeight;
                xPadding = screenWidth / 2 - (bitmapWidth / 2);
                yPadding = (bitmapHeight * (endScale - 1)) / 2;
            }

            transitionView.animate()
                    .setInterpolator(new MaterialInterpolator())
                    .setStartDelay(0)
                    .setDuration(0)
                    .setInterpolator(new MaterialInterpolator())
                    .x(xPadding)
                    .y(yPadding)
                    .scaleX(endScale)
                    .scaleY(endScale)
                    .setListener(null)
                    .start();

            final float finalBitmapWidth = bitmapWidth;
            final float finalBitmapHeight = bitmapHeight;
            final float finalXPadding = xPadding;
            final float finalYPadding = yPadding;
            transitionView.post(new Runnable() {
                @Override
                public void run() {
                    transitionView.animate()
                            .setStartDelay(startDelay)
                            .setInterpolator(new MaterialInterpolator())
                            .setDuration(300 * animationMultiplier)
                            .x(transitionLeft + (finalBitmapWidth * (finishScaleWidth - 1) / 2))
                            .y(transitionTop + (finalBitmapHeight * (finishScaleHeight - 1) / 2))
                            .scaleX(finishScaleWidth)
                            .scaleY(finishScaleHeight)
                            .setListener(listener)
                            .start();
                }
            });
        }

        public static void animateBackgroundForward(View backgroundView, Animator.AnimatorListener listener) {
            backgroundView.animate()
                    .setDuration(300 * animationMultiplier)
                    .setInterpolator(new MaterialInterpolator())
                    .alpha(1)
                    .setListener(listener)
                    .start();
        }

        public static void animateBackgroundBack(View backgroundView, Animator.AnimatorListener listener) {
            backgroundView.animate()
                    .setDuration(300 * animationMultiplier)
                    .setInterpolator(new MaterialInterpolator())
                    .alpha(0)
                    .setListener(listener)
                    .start();
        }
    }
