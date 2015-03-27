package im.actor.model.droidkit.engine;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface PreferencesStorage {

    public void putLong(String key, long v);

    public long getLong(String key, long def);

    public void putInt(String key, int v);

    public int getInt(String key, int def);

    public void putBool(String key, boolean v);

    public boolean getBool(String key, boolean def);

    public void putBytes(String key, byte[] v);

    public byte[] getBytes(String key);

    public void putString(String key, String v);

    public String getString(String key);
}