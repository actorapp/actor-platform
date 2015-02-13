package im.actor.messenger.storage;

import com.droidkit.engine.persistence.PersistenceMap;
import com.droidkit.engine.persistence.SerializableMap;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import im.actor.messenger.core.AppContext;
import im.actor.messenger.model.DialogUids;

/**
 * Created by ex3ndr on 27.09.14.
 */
public class DialogStorage {
    private static final PersistenceMap<String> DRAFTS = new SerializableMap<String>(
            new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()), "DRAFTS"));

    public static PersistenceMap<String> draftStorage() {
        return DRAFTS;
    }
}
