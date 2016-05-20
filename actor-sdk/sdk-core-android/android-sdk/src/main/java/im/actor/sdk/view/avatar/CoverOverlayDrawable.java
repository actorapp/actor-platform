package im.actor.sdk.view.avatar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import im.actor.sdk.R;
import im.actor.sdk.util.Screen;

public class CoverOverlayDrawable extends Drawable {

    private Drawable bottomShadow;

    public CoverOverlayDrawable(Context context) {
        bottomShadow = context.getResources().getDrawable(R.drawable.profile_avatar_bottom_shadow);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = getBounds();
        bottomShadow.setBounds(rect.left, rect.bottom - Screen.dp(64), rect.right, rect.bottom);
        bottomShadow.draw(canvas);
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
