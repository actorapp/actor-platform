package im.actor.model;

import im.actor.model.concurrency.MainThread;
import im.actor.model.concurrency.NoMainThread;
import im.actor.model.network.Endpoints;
import im.actor.model.storage.EnginesFactory;
import im.actor.model.storage.MemoryEnginesFactory;
import im.actor.model.storage.MemoryPreferences;
import im.actor.model.storage.PreferencesStorage;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Configuration {

    private boolean persistMessages = false;

    private MainThread mainThread = new NoMainThread();
    private Endpoints endpoints;
    private PreferencesStorage preferencesStorage = new MemoryPreferences();
    private EnginesFactory enginesFactory = new MemoryEnginesFactory();
    private OnlineCallback onlineCallback;

    public boolean isPersistMessages() {
        return persistMessages;
    }

    public void setPersistMessages(boolean persistMessages) {
        this.persistMessages = persistMessages;
    }

    public EnginesFactory getEnginesFactory() {
        return enginesFactory;
    }

    public void setEnginesFactory(EnginesFactory enginesFactory) {
        this.enginesFactory = enginesFactory;
    }

    public PreferencesStorage getPreferencesStorage() {
        return preferencesStorage;
    }

    public void setPreferencesStorage(PreferencesStorage preferencesStorage) {
        this.preferencesStorage = preferencesStorage;
    }

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoints endpoints) {
        this.endpoints = endpoints;
    }

    public MainThread getMainThread() {
        return mainThread;
    }

    public void setMainThread(MainThread mainThread) {
        this.mainThread = mainThread;
    }

    public OnlineCallback getOnlineCallback() {
        return onlineCallback;
    }

    public void setOnlineCallback(OnlineCallback onlineCallback) {
        this.onlineCallback = onlineCallback;
    }
}
