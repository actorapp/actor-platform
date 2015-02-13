package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class GroupTitle extends AbsServiceMessage {
    private String newTitle;

    public GroupTitle(String newTitle) {
        super(false);
        this.newTitle = newTitle;

    }

    public GroupTitle() {

    }

    public String getNewTitle() {
        return newTitle;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        newTitle = values.getString(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(1, newTitle);
    }
}
