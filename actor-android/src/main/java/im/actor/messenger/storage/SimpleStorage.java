package im.actor.messenger.storage;

import com.droidkit.engine.persistence.PersistenceLongSet;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import im.actor.messenger.core.AppContext;

/**
 * Created by ex3ndr on 04.11.14.
 */
public class SimpleStorage {
    private static PersistenceLongSet contactUsers = new PersistenceLongSet(new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()),
            "contacts_users"));

    public static PersistenceLongSet getContactsMap() {
        return contactUsers;
    }
}
