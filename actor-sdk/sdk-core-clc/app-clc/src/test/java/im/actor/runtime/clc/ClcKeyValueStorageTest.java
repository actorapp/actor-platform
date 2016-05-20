package im.actor.runtime.clc;

import im.actor.runtime.StorageRuntimeProvider;
import im.actor.runtime.storage.KeyValueRecord;
import im.actor.runtime.storage.KeyValueStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ClcKeyValueStorageTest {

    private StorageRuntimeProvider srp;
    private KeyValueStorage kvs;

    @Before
    public void initialize() {
        srp = new StorageRuntimeProvider();
        //create test table
        kvs = srp.createKeyValue("test");
    }


    @Test
    public void loadItem() {
        assertNull(kvs.loadItem(0));
    }

    @Test
    public void loadItemWithContext() {
        kvs.addOrUpdateItem(1, "value1".getBytes());
        kvs.addOrUpdateItem(2, "value2".getBytes());
        assertEquals(new String(kvs.loadItem(1)), "value1");
        assertEquals(new String(kvs.loadItem(2)), "value2");

        srp.setContext("935");
        kvs = srp.createKeyValue("test");
        kvs.addOrUpdateItem(3, "value3".getBytes());
        kvs.addOrUpdateItem(4, "value4".getBytes());
        assertEquals(new String(kvs.loadItem(3)), "value3");
        assertNull(kvs.loadItem(1));
        assertNull(kvs.loadItem(2));

        srp.setContext(null);
        kvs = srp.createKeyValue("test");
        assertEquals(new String(kvs.loadItem(1)), "value1");
        assertEquals(new String(kvs.loadItem(2)), "value2");

    }




    @Test
    public void addOrUpdateItem() {
        //insert
        kvs.addOrUpdateItem(1, "value1".getBytes());
        assertEquals(new String(kvs.loadItem(1)), "value1");

        //update
        kvs.addOrUpdateItem(1, "value2".getBytes());
        assertEquals(new String(kvs.loadItem(1)), "value2");
    }


    @Test
    public void addOrUpdateItems() {
        //insert
        List keyValues = Arrays.asList(
                new KeyValueRecord(1,"value1".getBytes()),
                new KeyValueRecord(2,"value2".getBytes()));
        kvs.addOrUpdateItems(keyValues);
        assertEquals(new String(kvs.loadItem(1)), "value1");
        assertEquals(new String(kvs.loadItem(2)), "value2");

        //update
        keyValues = Arrays.asList(
                new KeyValueRecord(1,"value3".getBytes()),
                new KeyValueRecord(2,"value4".getBytes()));
        kvs.addOrUpdateItems(keyValues);
        assertEquals(new String(kvs.loadItem(1)), "value3");
        assertEquals(new String(kvs.loadItem(2)), "value4");
    }

    @Test
    public void removeItem(){
        kvs.addOrUpdateItem(1, "value1".getBytes());
        assertEquals(new String(kvs.loadItem(1)), "value1");
        kvs.removeItem(1);
        assertNull(kvs.loadItem(1));
    }

    @Test
    public void removeItemWithContext(){
        kvs.addOrUpdateItem(1, "value1".getBytes());
        assertEquals(new String(kvs.loadItem(1)), "value1");

        srp.setContext("935");
        kvs = srp.createKeyValue("test");
        kvs.addOrUpdateItem(1, "value2".getBytes());
        kvs.removeItem(1);
        assertNull(kvs.loadItem(1));

        srp.setContext(null);
        kvs = srp.createKeyValue("test");
        assertEquals(new String(kvs.loadItem(1)), "value1");
    }

    @Test
    public void removeItems(){
        List keyValues = Arrays.asList(
                new KeyValueRecord(1,"value1".getBytes()),
                new KeyValueRecord(2,"value2".getBytes()));
        kvs.addOrUpdateItems(keyValues);
        assertEquals(new String(kvs.loadItem(1)), "value1");
        assertEquals(new String(kvs.loadItem(2)), "value2");

        kvs.removeItems(new long[]{1,2});
        assertNull(kvs.loadItem(1));
        assertNull(kvs.loadItem(2));
    }

    @Test
    public void clear(){
        List keyValues = Arrays.asList(
                new KeyValueRecord(1,"value1".getBytes()),
                new KeyValueRecord(2,"value2".getBytes()));
        kvs.addOrUpdateItems(keyValues);
        assertEquals(new String(kvs.loadItem(1)), "value1");
        assertEquals(new String(kvs.loadItem(2)), "value2");

        kvs.clear();
        assertNull(kvs.loadItem(1));
        assertNull(kvs.loadItem(2));
    }

    @Test
    public void clearWithContext(){
        List keyValues = Arrays.asList(
                new KeyValueRecord(1,"value1".getBytes()),
                new KeyValueRecord(2,"value2".getBytes()));
        kvs.addOrUpdateItems(keyValues);
        assertEquals(new String(kvs.loadItem(1)), "value1");
        assertEquals(new String(kvs.loadItem(2)), "value2");

        srp.setContext("935");
        kvs = srp.createKeyValue("test");
        assertNull(kvs.loadItem(1));
        assertNull(kvs.loadItem(2));

        List keyValues2 = Arrays.asList(
                new KeyValueRecord(3,"value3".getBytes()),
                new KeyValueRecord(4,"value4".getBytes()));
        kvs.addOrUpdateItems(keyValues2);
        assertEquals(new String(kvs.loadItem(3)), "value3");
        assertEquals(new String(kvs.loadItem(4)), "value4");
        kvs.clear();
        assertNull(kvs.loadItem(3));
        assertNull(kvs.loadItem(4));

        srp.setContext(null);
        kvs = srp.createKeyValue("test");
        assertEquals(new String(kvs.loadItem(1)), "value1");
        assertEquals(new String(kvs.loadItem(2)), "value2");

    }



    @After
    public void finalize() {
        srp.setContext(null);
        //remove all records
        ((ClcKeyValueStorage)kvs).clearAll();
    }
}
