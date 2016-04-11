package im.actor.core.modules.storage;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.auth.AuthenticationBackupData;
import im.actor.core.network.AuthKeyStorage;
import im.actor.runtime.Storage;

public class StorageModule extends AbsModule {

    private static final int STORAGE_SCHEME_VERSION = 1;
    private static final String STORAGE_SCHEME_VERSION_KEY = "storage_sheme_version";

    public StorageModule(ModuleContext context) {
        super(context);
    }

    public void applyStorage() {
        int version = preferences().getInt(STORAGE_SCHEME_VERSION_KEY, 0);
        if (version != STORAGE_SCHEME_VERSION) {
            performUpgrade();
            preferences().putInt(STORAGE_SCHEME_VERSION_KEY, STORAGE_SCHEME_VERSION);
        }
    }

    private void performUpgrade() {

        //
        // Backing Up sensitive information
        //

        AuthKeyStorage storage = context().getActorApi().getKeyStorage();

        long authKey = storage.getAuthKey();
        byte[] masterKey = storage.getAuthMasterKey();

        AuthenticationBackupData authenticationBackupData = context().getAuthModule().performBackup();

        //
        // Clear Storage
        //

        Storage.resetStorage();

        //
        // Restore Data
        //

        storage.saveAuthKey(authKey);
        if (masterKey != null) {
            storage.saveMasterKey(masterKey);
        }
        context().getAuthModule().restoreBackup(authenticationBackupData);
    }
}
