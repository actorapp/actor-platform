package im.actor.sdk.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;

import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;

public class ShareMenuButtonFactory {
    public static StateListDrawable get(int color, Context context) {
        GradientDrawable bg = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle_btn_bg);
        bg.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        bg.setColor(color);
        GradientDrawable bgPressed = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle_btn_bg);
        bgPressed.setColorFilter(new PorterDuffColorFilter(ActorStyle.getDarkenArgb(color, 0.95), PorterDuff.Mode.MULTIPLY));
        bgPressed.setColor(ActorStyle.getDarkenArgb(color, 0.95));
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},
                bgPressed);
        states.addState(StateSet.WILD_CARD,
                bg);
        return states;
    }
}