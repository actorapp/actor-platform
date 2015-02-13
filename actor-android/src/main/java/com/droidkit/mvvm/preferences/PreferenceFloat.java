package com.droidkit.mvvm.preferences;

import android.content.SharedPreferences;
import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class PreferenceFloat extends ValueModel<Float> {

    private SharedPreferences preferences;

    public PreferenceFloat(String name, SharedPreferences preferences, Float initialValue) {
        super(name, preferences.getFloat(name, initialValue));
        this.preferences = preferences;
    }

    @Override
    public void change(Float value) {
        super.change(value);
        if (this.preferences != null) {
            this.preferences.edit().putFloat(getName(), value).commit();
        }
    }
}
