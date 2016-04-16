package im.actor.runtime.clc;

import im.actor.runtime.StorageRuntimeProvider;
import im.actor.runtime.storage.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by amir on 4/12/16.
 */
public class ClcListStorageTest {

    private StorageRuntimeProvider srp;
    private ListStorage ls;

    @Before
    public void initialize() {
        srp = new StorageRuntimeProvider();
        //create test table
        ls = srp.createList("test");
    }


    @Test
    public void loadItem() {
        assertNull(ls.loadItem(0));
    }

//    @Test
//    public void loadItemWithContext() {
//        ls.updateOrAdd(1, "value1".getBytes());
//        ls.updateOrAdd(2, "value2".getBytes());
//        assertEquals(new String(kvs.loadItem(1)), "value1");
//        assertEquals(new String(kvs.loadItem(2)), "value2");
//
//        srp.setContext("935");
//        ls = srp.createKeyValue("test");
//        ls.addOrUpdateItem(3, "value3".getBytes());
//        ls.addOrUpdateItem(4, "value4".getBytes());
//        assertEquals(new String(ls.loadItem(3)), "value3");
//        assertNull(ls.loadItem(1));
//        assertNull(ls.loadItem(2));
//
//        srp.setContext(null);
//        kvs = srp.createKeyValue("test");
//        assertEquals(new String(kvs.loadItem(1)), "value1");
//        assertEquals(new String(kvs.loadItem(2)), "value2");
//
//    }


    private void assertEqualLER(ListEngineRecord actualLer, ListEngineRecord expectedLer) {
//        assertEquals(actualLer.getKey(), expectedLer.getKey());
        assertEquals(actualLer.getOrder(), expectedLer.getOrder());
        assertEquals(actualLer.getQuery(), expectedLer.getQuery());
        assertEquals(new String(actualLer.getData()), new String(expectedLer.getData()));
    }

    @Test
    public void updateOrAdd() {
        //insert
        ListEngineRecord ler = new ListEngineRecord(1, 1, "test", "bytes".getBytes());
        ls.updateOrAdd(ler);

        ListEngineRecord actual = ls.loadItem(ler.getKey());
        assertEqualLER(actual, ler);

        //update
        ler = new ListEngineRecord(1, 1, "test2", "bytes2".getBytes());
        ls.updateOrAdd(ler);
        actual = ls.loadItem(ler.getKey());
        assertEqualLER(actual, ler);
    }

    @Test
    public void updateOrAddList() {
        //insert
        ListEngineRecord ler1 = new ListEngineRecord(1, 1, "test1", "bytes1".getBytes());
        ListEngineRecord ler2 = new ListEngineRecord(2, 2, "test2", "bytes2".getBytes());
        ListEngineRecord ler3 = new ListEngineRecord(3, 3, "test3", "bytes3".getBytes());
        ListEngineRecord ler4 = new ListEngineRecord(4, 4, "test4", "bytes4".getBytes());
        ListEngineRecord ler5 = new ListEngineRecord(5, 5, "test5", "bytes5".getBytes());
        ListEngineRecord ler6 = new ListEngineRecord(6, 6, "test6", "bytes6".getBytes());
        ls.updateOrAdd(Arrays.asList(ler1, ler2, ler3, ler4, ler5, ler6));

        ListEngineRecord actual = ls.loadItem(ler1.getKey());
        assertEqualLER(actual, ler1);
        actual = ls.loadItem(ler2.getKey());
        assertEqualLER(actual, ler2);
        actual = ls.loadItem(ler3.getKey());
        assertEqualLER(actual, ler3);
        actual = ls.loadItem(ler4.getKey());
        assertEqualLER(actual, ler4);
        actual = ls.loadItem(ler5.getKey());
        assertEqualLER(actual, ler5);
        actual = ls.loadItem(ler6.getKey());
        assertEqualLER(actual, ler6);

    }

    @Test
    public void remove() {
        updateOrAdd();
        ls.delete(1);
        assertNull(ls.loadItem(1));
    }

    @Test
    public void removeList() {
        updateOrAddList();
        ls.delete(new long[]{1, 2});
        assertNull(ls.loadItem(1));
        assertNull(ls.loadItem(2));
        assertNotNull(ls.loadItem(3));
    }

    @Test
    public void isEmpty() {
        assertTrue(ls.isEmpty());
        updateOrAdd();
        assertFalse(ls.isEmpty());
    }

//    @Test
//    public void loadForward() {
//        updateOrAddList();
//        ListStorageDisplayEx lsd = (ListStorageDisplayEx) ls;
//        List<ListEngineRecord> actual = lsd.loadForward(5L, 2);
//
//        List<ListEngineRecord> expected = Arrays.asList(
//                new ListEngineRecord(3, 3, "test3", "bytes3".getBytes()),
//                new ListEngineRecord(4, 4, "test3", "bytes3".getBytes())
//        );
//        assertEqualLER(actual.get(0), expected.get(0));
//        assertEqualLER(actual.get(1), expected.get(1));
//    }

    //
//
//    @Test
//    public void addOrUpdateItems() {
//        //insert
//        List keyValues = Arrays.asList(
//                new KeyValueRecord(1,"value1".getBytes()),
//                new KeyValueRecord(2,"value2".getBytes()));
//        kvs.addOrUpdateItems(keyValues);
//        assertEquals(new String(kvs.loadItem(1)), "value1");
//        assertEquals(new String(kvs.loadItem(2)), "value2");
//
//        //update
//        keyValues = Arrays.asList(
//                new KeyValueRecord(1,"value3".getBytes()),
//                new KeyValueRecord(2,"value4".getBytes()));
//        kvs.addOrUpdateItems(keyValues);
//        assertEquals(new String(kvs.loadItem(1)), "value3");
//        assertEquals(new String(kvs.loadItem(2)), "value4");
//    }
//
//    @Test
//    public void removeItem(){
//        kvs.addOrUpdateItem(1, "value1".getBytes());
//        assertEquals(new String(kvs.loadItem(1)), "value1");
//        kvs.removeItem(1);
//        assertNull(kvs.loadItem(1));
//    }
//
//    @Test
//    public void removeItemWithContext(){
//        kvs.addOrUpdateItem(1, "value1".getBytes());
//        assertEquals(new String(kvs.loadItem(1)), "value1");
//
//        srp.setContext("935");
//        kvs = srp.createKeyValue("test");
//        kvs.addOrUpdateItem(1, "value2".getBytes());
//        kvs.removeItem(1);
//        assertNull(kvs.loadItem(1));
//
//        srp.setContext(null);
//        kvs = srp.createKeyValue("test");
//        assertEquals(new String(kvs.loadItem(1)), "value1");
//    }
//
//    @Test
//    public void removeItems(){
//        List keyValues = Arrays.asList(
//                new KeyValueRecord(1,"value1".getBytes()),
//                new KeyValueRecord(2,"value2".getBytes()));
//        kvs.addOrUpdateItems(keyValues);
//        assertEquals(new String(kvs.loadItem(1)), "value1");
//        assertEquals(new String(kvs.loadItem(2)), "value2");
//
//        kvs.removeItems(new long[]{1,2});
//        assertNull(kvs.loadItem(1));
//        assertNull(kvs.loadItem(2));
//    }
//
    @Test
    public void clear() {
        updateOrAddList();
        ls.clear();
        assertNull(ls.loadItem(1));
        assertNull(ls.loadItem(2));
    }

    @Test
    public void getCount(){
        updateOrAddList();
        assertEquals(ls.getCount(), 6);
    }

    @After
    public void finalize() {
        srp.setContext(null);
        //remove all records
        ((ClcListStorage) ls).clearAll();
    }
}
