package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ServiceGroupCreated extends ServiceContent {

    public static ServiceGroupCreated fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceGroupCreated(), data);
    }

    private String groupTitle;

    public ServiceGroupCreated(String groupTitle) {
        super("Group '" + groupTitle + "' created");
        this.groupTitle = groupTitle;
    }

    private ServiceGroupCreated() {

    }

    public String getGroupTitle() {
        return groupTitle;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.SERVICE_CREATED;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        groupTitle = values.getString(10);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(10, groupTitle);
    }
}
