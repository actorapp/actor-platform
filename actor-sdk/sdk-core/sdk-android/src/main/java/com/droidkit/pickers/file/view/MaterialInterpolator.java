package com.droidkit.pickers.file.view;


import android.view.animation.Interpolator;

/**
 * <p>Created by Stepan Ex3NDR Korshakov (me@ex3ndr.com)</p>
 * Function adapted by Roman Orekhov (http://vk.com/id276848)
 */
public class MaterialInterpolator implements Interpolator {

    private static final MaterialInterpolator INSTANCE = new MaterialInterpolator();

    public static MaterialInterpolator getInstance() {
        return INSTANCE;
    }

    @Override
    public float getInterpolation(float x) {
        return (float) (6 * Math.pow(x, 2) - 8 * Math.pow(x, 3) + 3 * Math.pow(x, 4));
    }
}
