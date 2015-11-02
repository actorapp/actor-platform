package im.actor.sdk.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;

import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;

public class SelectorFactory {
    public static StateListDrawable get(int color, Context context) {
        GradientDrawable bg = (GradientDrawable) context.getResources().getDrawable(R.drawable.btn_bg);
        bg.setColor(color);
        GradientDrawable bgPressed = (GradientDrawable) context.getResources().getDrawable(R.drawable.btn_bg_pressed);
        bgPressed.setColor(ActorStyle.getDarkenArgb(color, 0.95));
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},
                bgPressed);
        states.addState(StateSet.WILD_CARD,
                bg);
        return states;
    }
}