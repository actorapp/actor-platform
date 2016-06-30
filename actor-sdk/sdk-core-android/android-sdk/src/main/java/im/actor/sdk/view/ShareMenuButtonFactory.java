package im.actor.sdk.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.StateSet;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;

public class ShareMenuButtonFactory {
    public static StateListDrawable get(int color, Context context) {
        ShapeDrawable bg = new ShapeDrawable(new OvalShape());
        int padding = Screen.dp(5);
        bg.getPaint().setColor(color);
        ShapeDrawable bgPressed = new ShapeDrawable(new OvalShape());
        bgPressed.getPaint().setColor(ActorStyle.getDarkenArgb(color, 0.95));
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},
                new InsetDrawable(bgPressed, padding));
        states.addState(StateSet.WILD_CARD,
                new InsetDrawable(bg, padding));
        return states;
    }
}