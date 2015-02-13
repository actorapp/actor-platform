package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class GroupAdd extends AbsServiceMessage {
    private int addedUid;

    public GroupAdd(int addedUid) {
        super(false);
        this.addedUid = addedUid;
    }

    public GroupAdd() {

    }

    public int getAddedUid() {
        return addedUid;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        addedUid = values.getInt(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(1, addedUid);
    }
}
