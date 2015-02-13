package com.droidkit.mvvm.preferences;

import android.content.SharedPreferences;
import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class PreferenceString extends ValueModel<String> {

    private SharedPreferences preferences;

    public PreferenceString(String name, SharedPreferences preferences, String initialValue) {
        super(name, preferences.getString(name, initialValue));
        this.preferences = preferences;
    }

    @Override
    public void change(String value) {
        super.change(value);
        if (this.preferences != null) {
            this.preferences.edit().putString(getName(), value).commit();
        }
    }
}
