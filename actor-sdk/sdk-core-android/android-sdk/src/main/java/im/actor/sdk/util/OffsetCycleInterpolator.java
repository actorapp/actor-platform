package im.actor.sdk.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.CycleInterpolator;

public class OffsetCycleInterpolator extends CycleInterpolator {
    private float offset;
    public OffsetCycleInterpolator(float offset) {
        super(1f);
        this.offset = offset;
    }

    public OffsetCycleInterpolator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public float getInterpolation(float input) {
        return (float)(Math.sin(2 * Math.PI * input + offset)) + 1f;
    }
}
