package im.actor.runtime;

import im.actor.runtime.clc.*;
import im.actor.runtime.storage.PreferencesStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class StorageRuntimeProvider implements StorageRuntime, ClcContext {
    private static final Logger logger = LoggerFactory.getLogger(StorageRuntimeProvider.class);
    private ClcJavaPreferenceStorage preferences;
    private String context;

    public StorageRuntimeProvider() {
        this.preferences = new ClcJavaPreferenceStorage();
    }

    @Override
    public PreferencesStorage createPreferencesStorage() {
        if (context == null) {
            logger.warn("context is not set");
            context = "";
        }
        return this.preferences;
    }

    static Connection kv_db = null;

    @Override
    public KeyValueStorage createKeyValue(String name) {

        try {
            if (kv_db == null) {
                Class.forName("org.sqlite.JDBC");
                kv_db = DriverManager.getConnection("jdbc:sqlite:keyvalue.db");
            }
            return new ClcKeyValueStorage(kv_db, name, this.context);
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error in creating keyvalue storage", e);
        }
        return null;
    }

    static Connection l_db = null;

    @Override
    public ListStorage createList(String name) {
        try {
            if (l_db == null) {
                Class.forName("org.sqlite.JDBC");
                l_db = DriverManager.getConnection("jdbc:sqlite:list.db");
            }

            return new ClcListStorage(l_db, name, this.context);

        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error in creating list storage", e);
        }
        return null;
    }

    @Override
    public void resetStorage() {
        //Clear preferences
        Preferences pref = this.preferences.getPref();
        try {
            //Only remove this context preferences
            for (String key : pref.keys()) {
                pref.remove(context + "_" + key);
            }
        } catch (BackingStoreException e) {
            logger.error("Error in clearing preferences for context: " + context, e);
        }
        //TODO clear records in sqlites for this context
    }

    @Override
    public void setContext(String context) {
        if (context == null) {
            this.context = "";
        } else {
            this.context = context;
        }
    }

    @Override
    public String getContext() {
        return this.context;
    }
}
