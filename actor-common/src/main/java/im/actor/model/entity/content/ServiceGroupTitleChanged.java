package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ServiceGroupTitleChanged extends ServiceContent {

    public static ServiceGroupTitleChanged fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceGroupTitleChanged(), data);
    }

    private String newTitle;

    public ServiceGroupTitleChanged(String newTitle) {
        super("Group theme changed");
        this.newTitle = newTitle;
    }

    private ServiceGroupTitleChanged() {

    }

    public String getNewTitle() {
        return newTitle;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.SERVICE_TITLE;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        newTitle = values.getString(10);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(10, newTitle);
    }
}
