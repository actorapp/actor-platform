/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import java.util.List;

public interface ListStorageDisplayEx extends ListStorage {
    List<ListEngineRecord> loadBackward(Long sortingKey, int limit);

    List<ListEngineRecord> loadForward(Long sortingKey, int limit);

    List<ListEngineRecord> loadBackward(String query, Long sortingKey, int limit);

    List<ListEngineRecord> loadForward(String query, Long sortingKey, int limit);
}
