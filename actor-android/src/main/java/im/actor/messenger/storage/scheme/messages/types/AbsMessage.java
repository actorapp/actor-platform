package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserComposite;

/**
 * Created by ex3ndr on 25.10.14.
 */
public abstract class AbsMessage extends BserComposite {
    protected boolean isEncrypted;

    protected AbsMessage(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    protected AbsMessage() {
        this.isEncrypted = false;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }
}