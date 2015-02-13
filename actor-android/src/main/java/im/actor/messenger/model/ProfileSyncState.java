package im.actor.messenger.model;

import android.content.Context;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.mvvm.ValueModel;

import java.io.IOException;

import im.actor.messenger.util.Logger;
import im.actor.messenger.util.io.BserPersistence;

/**
 * Created by ex3ndr on 30.12.14.
 */
public class ProfileSyncState {

    private static final String TAG = "ProfileSyncState";

    private static BserPersistence<SyncState> persistence;

    private static final ValueModel<State> syncState = new ValueModel<State>("profile.sync", State.IN_PROGRESS);

    public static void init(Context context) {
        persistence = new BserPersistence<>(context, "profile.bin", SyncState.class);
        SyncState state = persistence.getObj();
        if (state == null) {
            persistence.setObj(new SyncState());
        }
        updateState();
    }

    public static synchronized void onImportEnded() {
        Logger.d(TAG, "onImportEnded");
        SyncState state = persistence.getObj();
        if (state.isBookImported) {
            return;
        }

        persistence.setObj(new SyncState(true, state.isContactsSynced, state.isContactsEmpty, state.isDialogsEmpty));
        updateState();
    }

    public static synchronized void onDialogsNotEmpty() {
        Logger.d(TAG, "onDialogsNotEmpty");
        SyncState state = persistence.getObj();
        if (!state.isDialogsEmpty) {
            return;
        }
        persistence.setObj(new SyncState(state.isBookImported, state.isContactsSynced, state.isContactsEmpty, false));
        updateState();
    }

    public static synchronized void onContactsNotEmpty() {
        Logger.d(TAG, "onDialogsNotEmpty");
        SyncState state = persistence.getObj();
        if (!state.isContactsEmpty) {
            return;
        }
        persistence.setObj(new SyncState(state.isBookImported, state.isContactsSynced, false, state.isDialogsEmpty));
        updateState();
    }

    public static synchronized void onContactsLoaded(boolean isEmpty) {
        Logger.d(TAG, "onContactsLoaded " + isEmpty);
        SyncState state = persistence.getObj();
        if (state.isContactsSynced && state.isContactsEmpty == isEmpty) {
            return;
        }
        persistence.setObj(new SyncState(state.isBookImported, true, isEmpty, state.isDialogsEmpty));
        updateState();
    }

    private static void updateState() {
        SyncState state = persistence.getObj();
        if (state.isBookImported && state.isContactsSynced) {
            if (state.isContactsEmpty && state.isDialogsEmpty) {
                syncState.change(State.EMPTY_APP);
            } else {
                syncState.change(State.READY);
            }
        } else {
            syncState.change(State.IN_PROGRESS);
        }
    }

    public static ValueModel<State> getSyncState() {
        return syncState;
    }

    public enum State {
        IN_PROGRESS,
        EMPTY_APP,
        READY
    }

    public static class SyncState extends BserObject {

        private boolean isBookImported = false;
        private boolean isContactsSynced = false;

        private boolean isContactsEmpty = true;
        private boolean isDialogsEmpty = true;

        public SyncState(boolean isBookImported, boolean isContactsSynced, boolean isContactsEmpty, boolean isDialogsEmpty) {
            this.isBookImported = isBookImported;
            this.isContactsSynced = isContactsSynced;
            this.isContactsEmpty = isContactsEmpty;
            this.isDialogsEmpty = isDialogsEmpty;
        }

        public SyncState() {
        }

        public boolean isBookImported() {
            return isBookImported;
        }

        public boolean isContactsSynced() {
            return isContactsSynced;
        }

        public boolean isContactsEmpty() {
            return isContactsEmpty;
        }

        public boolean isDialogsEmpty() {
            return isDialogsEmpty;
        }

        @Override
        public void parse(BserValues values) throws IOException {
            isBookImported = values.getBool(1);
            isContactsSynced = values.getBool(2);
            isContactsEmpty = values.getBool(3);
            isDialogsEmpty = values.getBool(4);
        }

        @Override
        public void serialize(BserWriter writer) throws IOException {
            writer.writeBool(1, isBookImported);
            writer.writeBool(2, isContactsSynced);
            writer.writeBool(3, isContactsEmpty);
            writer.writeBool(4, isDialogsEmpty);
        }
    }
}