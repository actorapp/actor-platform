package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;

/**
 * Created by ex3ndr on 01.04.15.
 */
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
