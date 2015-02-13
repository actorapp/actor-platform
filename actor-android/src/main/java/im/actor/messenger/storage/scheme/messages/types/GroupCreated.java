package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class GroupCreated extends AbsServiceMessage {

    private String title;

    public GroupCreated(String title) {
        super(false);
        this.title = title;
    }

    public GroupCreated() {

    }

    public String getTitle() {
        return title;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        title = values.getString(1, "");
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(1, title);
    }
}
