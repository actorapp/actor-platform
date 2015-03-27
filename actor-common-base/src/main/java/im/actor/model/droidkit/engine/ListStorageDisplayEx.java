package im.actor.model.droidkit.engine;

import java.util.List;

/**
 * Created by ex3ndr on 27.03.15.
 */
public interface ListStorageDisplayEx extends ListStorage {
    public List<ListEngineRecord> loadBackward(Long sortingKey, int limit);

    public List<ListEngineRecord> loadForward(Long sortingKey, int limit);

    public List<ListEngineRecord> loadBackward(String query, Long sortingKey, int limit);

    public List<ListEngineRecord> loadForward(String query, Long sortingKey, int limit);
}
