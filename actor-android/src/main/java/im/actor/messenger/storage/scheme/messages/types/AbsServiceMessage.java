package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public abstract class AbsServiceMessage extends AbsMessage {
    protected AbsServiceMessage(boolean isEncrypted) {
        super(isEncrypted);
    }

    protected AbsServiceMessage() {
    }

    @Override
    public void parse(BserValues values) throws IOException {

    }

    @Override
    public void serialize(BserWriter writer) throws IOException {

    }
}
