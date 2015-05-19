package im.actor.messenger.app.view;

import android.view.animation.Interpolator;

/**
 * <p>Created by Stepan Ex3NDR Korshakov (me@ex3ndr.com)</p>
 * Function adapted by Roman Orekhov (http://vk.com/id276848)
 */
public class ReverseMaterialInterpolator implements Interpolator {

    private static final ReverseMaterialInterpolator INSTANCE = new ReverseMaterialInterpolator();

    public static ReverseMaterialInterpolator getInstance() {
        return INSTANCE;
    }

    @Override
    public float getInterpolation(float x) {
        x = Math.abs(x-1f);
        return (float) (6 * Math.pow(x, 2) - 8 * Math.pow(x, 3) + 3 * Math.pow(x, 4));
    }
}
