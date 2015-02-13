package com.droidkit.mvvm.preferences;

import android.content.SharedPreferences;

import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class PreferenceLong extends ValueModel<Long> {

    private SharedPreferences preferences;

    public PreferenceLong(String name, SharedPreferences preferences, Long initialValue) {
        super(name, preferences.getLong(name, initialValue));
        this.preferences = preferences;
    }

    @Override
    public void change(Long value) {
        super.change(value);
        if (this.preferences != null) {
            this.preferences.edit().putLong(getName(), value).commit();
        }
    }
}
