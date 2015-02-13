package com.droidkit.mvvm.preferences;

import android.content.SharedPreferences;
import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class PreferenceInteger extends ValueModel<Integer> {

    private SharedPreferences preferences;

    public PreferenceInteger(String name, SharedPreferences preferences, Integer initialValue) {
        super(name, preferences.getInt(name, initialValue));
        this.preferences = preferences;
    }

    @Override
    public void change(Integer value) {
        super.change(value);
        if (this.preferences != null) {
            this.preferences.edit().putInt(getName(), value).commit();
        }
    }
}
