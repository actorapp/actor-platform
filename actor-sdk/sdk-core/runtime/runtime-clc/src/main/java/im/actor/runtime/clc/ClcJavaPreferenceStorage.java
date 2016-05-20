package im.actor.runtime.clc;

import im.actor.runtime.storage.PreferencesStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

/**
 * Created by mohammad on 11/18/15.
 * Edited by amir on 3/14/16
 */
public class ClcJavaPreferenceStorage implements ClcPreferencesStorage {
    private static final Logger logger = LoggerFactory.getLogger(ClcJavaPreferenceStorage.class);

    private Preferences pref = Preferences.userNodeForPackage(getClass());
    private String context;

    public ClcJavaPreferenceStorage() {
        if (context == null) {
            logger.warn("context is not set");
            context = "";
        }
    }

    @Override
    public void putLong(String key, long v) {
        pref.putLong(context + "_" + key, v);
    }

    @Override
    public long getLong(String key, long def) {
        return pref.getLong(context + "_" + key, def);
    }

    @Override
    public void putInt(String key, int v) {
        pref.putInt(context + "_" + key, v);
    }

    @Override
    public int getInt(String key, int def) {
        return pref.getInt(context + "_" + key, def);
    }

    @Override
    public void putBool(String key, boolean v) {
        pref.putBoolean(context + "_" + key, v);
    }

    @Override
    public boolean getBool(String key, boolean def) {
        return pref.getBoolean(context + "_" + key, def);
    }

    @Override
    public void putBytes(String key, byte[] v) {
        if (v != null) {
            pref.putByteArray(context + "_" + key, v);
        }
    }

    @Override
    public byte[] getBytes(String key) {
        return pref.getByteArray(context + "_" + key, null);
    }

    @Override
    public void putString(String key, String v) {
        if (v != null) {
            pref.put(context + "_" + key, v);
        }

    }

    @Override
    public String getString(String key) {
        return pref.get(context + "_" + key, null);
    }

    public Preferences getPref() {
        return this.pref;
    }

    @Override
    public void setContext(String context) {
        if (context == null) {
            logger.warn("context is not set");
            this.context = "";
        }else{
            this.context = context;
        }
    }

    @Override
    public String getContext() {
        return context;
    }
}
