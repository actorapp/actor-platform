package com.droidkit.mvvm.preferences;

import android.content.SharedPreferences;
import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class PreferenceBoolean extends ValueModel<Boolean> {

    private SharedPreferences preferences;

    public PreferenceBoolean(String name, SharedPreferences preferences, Boolean initialValue) {
        super(name, preferences.getBoolean(name, initialValue));
        this.preferences = preferences;
    }

    @Override
    public void change(Boolean value) {
        super.change(value);
        if (this.preferences != null) {
            this.preferences.edit().putBoolean(getName(), value).commit();
        }
    }

    public void toggle() {
        change(!getValue());
    }
}
