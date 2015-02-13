package im.actor.messenger.model;

import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 09.10.14.
 */
public class ContactsSyncState {
    private static final ValueModel<Boolean> syncState = new ValueModel<Boolean>("contacts.sync", false);

    public static ValueModel<Boolean> getSyncState() {
        return syncState;
    }
}
