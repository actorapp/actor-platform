package im.actor.runtime.clc;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import im.actor.runtime.StorageRuntimeProvider;
import im.actor.runtime.storage.IndexStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by amir on 4/12/16.
 */
public class ClcIndexStorageTest {

    private StorageRuntimeProvider srp;
    private IndexStorage is;

    @Before
    public void initialize() {
        System.out.println("before");
        srp = new StorageRuntimeProvider();
        //create test table
        is = srp.createIndex("test");
    }

    @Test
    public void get() {
        assertNull(is.get(0));

    }


    @Test
    public void getWithContext(){
        is.put(1, 20);
        srp.setContext("935");
        is = srp.createIndex("test");
        is.put(1,30);
        assertEquals(is.get(1), new Long(30));

        srp.setContext(null);
        is = srp.createIndex("test");
        assertEquals(is.get(1), new Long(20));
    }
    @Test
    public void put() {
        is.put(123, 456);
        assertEquals(is.get(123),new Long(456));

        is.put(3,30);
        is.put(4,30);
        assertEquals(is.get(3),new Long(30));
    }

    @Test
    public void putWithContext() {
        is.put(1, 10);
        is.put(2, 20);
        srp.setContext("935");
        is = srp.createIndex("test");
        is.put(3,30);
        is.put(4,40);
        assertNull(is.get(1));
        assertNull(is.get(2));
        assertNotNull(is.get(3));
        is.put(1,50);
        is.put(2,50);

        //test the effect of context(id and context are primary keys)
        assertEquals(((ClcIndexStorage)is).countAll(), 6);
    }

    @Test
    public void findBeforeValue() {
        is.put(1, 10);
        is.put(2, 10);
        is.put(3, 20);
        List<Long> actual = is.findBeforeValue(10);
        List<Long> expected = Arrays.asList(1L, 2L);
        assertThat(actual, is(expected));
    }

    @Test
    public void findBeforeValueWithContext(){
        is.put(1, 10);
        is.put(2, 10);
        srp.setContext("936");
        is = srp.createIndex("test");
        is.put(3, 10);
        is.put(4, 10);
        List<Long> actual = is.findBeforeValue(10);
        List<Long> expected = Arrays.asList(3L, 4L);
        assertThat(actual, is(expected));
        srp.setContext(null);
        is = srp.createIndex("test");
        actual = is.findBeforeValue(10);
        expected = Arrays.asList(1L, 2L);
        assertThat(actual, is(expected));
    }

    @Test
    public void remove() {
        is.put(1, 10);
        is.remove(1);
        assertNull(is.get(1));
    }

    @Test
    public void removeWithContext(){
        is.put(1,10);
        is.put(2,20);
        srp.setContext("935");
        is = srp.createIndex("test");
        is.put(1,30);
        is.put(2,40);
        is.remove(1);
        assertNull(is.get(1));
        srp.setContext(null);
        is = srp.createIndex("test");
        assertEquals(is.get(1), new Long(10));
    }

    @Test
    public void removeList() {
        is.put(1, 10);
        is.put(2, 10);
        is.put(3, 10);
        is.remove(Arrays.asList(1L, 2L, 3L));
        assertNull(is.get(1));
        assertNull(is.get(2));
        assertNull(is.get(3));
    }


    @Test
    public void removeBeforeValue() {
        is.put(1, 10);
        is.put(2, 10);
        is.put(3, 20);
        is.removeBeforeValue(10);
        assertNull(is.get(1));
        assertNull(is.get(2));
        assertEquals(is.get(3), new Long(20));
    }

    @Test
    public void getCount() {
        is.put(1, 10);
        is.put(2, 10);
        is.put(3, 20);
        assertEquals(is.getCount(), 3);
    }

    @Test
    public void getCountWithContext() {
        srp.setContext("935");
        is = srp.createIndex("test");
        is.put(1, 10);
        is.put(2, 10);
        assertEquals(is.getCount(), 2);

        srp.setContext(null);
        is = srp.createIndex("test");
        is.put(3, 20);
        is.put(4, 40);
        is.put(5, 40);
        assertEquals(is.getCount(), 3);

        srp.setContext("935");
        is = srp.createIndex("test");
        assertEquals(is.getCount(), 2);

        srp.setContext("936");
        is = srp.createIndex("test");
        is.put(1, 10);
        assertEquals(is.getCount(), 1);

    }

    @After
    public void finalize() {
        System.out.println("after");
        srp.setContext(null);
        //remove all records
        ((ClcIndexStorage)is).clearAll();
    }
}
