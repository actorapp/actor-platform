package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ServiceContent extends AbsContent {

    public static ServiceContent serviceFromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceContent(), data);
    }

    private String compatText;

    public ServiceContent(String compatText) {
        this.compatText = compatText;
    }

    protected ServiceContent() {

    }

    public String getCompatText() {
        return compatText;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.SERVICE;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        compatText = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(2, compatText);
    }
}