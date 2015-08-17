package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

public interface IndexStorage {

    @ObjectiveCName("putWithKey:withValue:")
    void put(long key, long value);

    @ObjectiveCName("get:")
    Long get(long key);

    @ObjectiveCName("findBeforeValue:")
    List<Long> findBeforeValue(long value);

    @ObjectiveCName("removeBeforeValue:")
    List<Long> removeBeforeValue(long value);

    @ObjectiveCName("removeWithKey:")
    void remove(long key);

    @ObjectiveCName("removeWithKeys:")
    void remove(List<Long> keys);

    @ObjectiveCName("getCount")
    int getCount();

    @ObjectiveCName("clear")
    void clear();
}
