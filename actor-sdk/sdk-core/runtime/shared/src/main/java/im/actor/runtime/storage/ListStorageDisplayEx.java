/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

public interface ListStorageDisplayEx extends ListStorage {
    @ObjectiveCName("loadBackwardWithSortKey:withLimit:")
    List<ListEngineRecord> loadBackward(Long sortingKey, int limit);

    @ObjectiveCName("loadForwardWithSortKey:withLimit:")
    List<ListEngineRecord> loadForward(Long sortingKey, int limit);

    @ObjectiveCName("loadBackwardWithQuery:withSortKey:withLimit:")
    List<ListEngineRecord> loadBackward(String query, Long sortingKey, int limit);

    @ObjectiveCName("loadForwardWithQuery:withSortKey:withLimit:")
    List<ListEngineRecord> loadForward(String query, Long sortingKey, int limit);

    @ObjectiveCName("loadCenterWithSortKey:withLimit:")
    List<ListEngineRecord> loadCenter(Long centerSortKey, int limit);
}
