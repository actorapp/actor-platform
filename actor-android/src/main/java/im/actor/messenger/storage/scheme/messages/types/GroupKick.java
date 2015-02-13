package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class GroupKick extends AbsServiceMessage {
    private int kickedUid;

    public GroupKick(int kickedUid) {
        super(false);
        this.kickedUid = kickedUid;
    }

    public GroupKick() {

    }

    public int getKickedUid() {
        return kickedUid;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        kickedUid = values.getInt(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(1, kickedUid);
    }
}
