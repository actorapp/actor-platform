package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ServiceGroupUserKicked extends ServiceContent {

    public static ServiceGroupUserKicked fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceGroupUserKicked(), data);
    }

    private int kickedUid;

    public ServiceGroupUserKicked(int kickedUid) {
        super("User kicked");
        this.kickedUid = kickedUid;
    }

    private ServiceGroupUserKicked() {

    }

    public int getKickedUid() {
        return kickedUid;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.SERVICE_KICKED;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        kickedUid = values.getInt(10);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeInt(10, kickedUid);
    }
}
