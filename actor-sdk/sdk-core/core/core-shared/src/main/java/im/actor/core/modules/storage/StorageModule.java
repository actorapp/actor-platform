package im.actor.core.modules.storage;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.auth.AuthenticationBackupData;
import im.actor.core.network.AuthKeyStorage;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.storage.KeyValueStorage;

public class StorageModule extends AbsModule {

    private static String TAG = "StorageModule";

    private static final int STORAGE_SCHEME_VERSION = 13;
    private static final String STORAGE_SCHEME_VERSION_KEY = "storage_sheme_version";

    private KeyValueStorage storage;

    public StorageModule(ModuleContext context) {
        super(context);
    }

    public void run(boolean isFirst) {
        int version = preferences().getInt(STORAGE_SCHEME_VERSION_KEY, 0);
        if (version != STORAGE_SCHEME_VERSION) {
            Log.w(TAG, "Upgrading scheme from " + version + " to " + STORAGE_SCHEME_VERSION);
            performUpgrade(isFirst);
            preferences().putInt(STORAGE_SCHEME_VERSION_KEY, STORAGE_SCHEME_VERSION);
        }

        storage = Storage.createKeyValue(STORAGE_BLOB);
    }

    public KeyValueStorage getBlobStorage() {
        return storage;
    }

    private void performUpgrade(boolean isFirst) {

        //
        // Backing Up sensitive data
        //
        Log.w(TAG, "Backing up sensitive data");
        AuthKeyStorage storage = context().getActorApi().getKeyStorage();
        long authKey = storage.getAuthKey();
        byte[] masterKey = storage.getAuthMasterKey();
        AuthenticationBackupData authenticationBackupData = null;
        if (!isFirst) {
            authenticationBackupData = context().getAuthModule().performBackup();
        }

        //
        // Clear Storage
        //
        Log.w(TAG, "Resetting storage");
        Storage.resetStorage();

        //
        // Notify Modules
        //
        Log.w(TAG, "Resetting modules");
        context().afterStorageReset();

        //
        // Restore Data
        //
        Log.w(TAG, "Restoring data");
        storage.saveAuthKey(authKey);
        if (masterKey != null) {
            storage.saveMasterKey(masterKey);
        }
        if (authenticationBackupData != null) {
            context().getAuthModule().restoreBackup(authenticationBackupData);
        }
    }
}
