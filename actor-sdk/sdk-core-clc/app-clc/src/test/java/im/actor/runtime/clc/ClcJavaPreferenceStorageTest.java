package im.actor.runtime.clc;

import im.actor.runtime.storage.PreferencesStorage;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by amir on 3/14/16.
 */
public class ClcJavaPreferenceStorageTest {
    @Test
    public void getString(){
        PreferencesStorage pref = new ClcJavaPreferenceStorage();
        pref.putString("key", "string");
//        assertEquals("string", pref.getString("key"));
    }

    @Test
    public void getLong(){
        PreferencesStorage pref = new ClcJavaPreferenceStorage();
        pref.putLong("key", 1);
//        assertEquals(1, pref.getLong("key", 0));
//        assertEquals(0, pref.getLong("nokey", 0));
    }


    @Test
    public void getBytes(){
        PreferencesStorage pref = new ClcJavaPreferenceStorage();
        pref.putBytes("key", new byte[]{1,2,3});
        byte[] arr = pref.getBytes("key");
//        assertEquals((byte)1, arr[0]);
//        assertEquals((byte)2, arr[1]);
//        assertEquals((byte)3, arr[2]);
    }

}
