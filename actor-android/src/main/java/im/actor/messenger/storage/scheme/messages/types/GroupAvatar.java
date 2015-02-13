package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

import im.actor.messenger.storage.scheme.avatar.Avatar;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class GroupAvatar extends AbsServiceMessage {
    private Avatar newAvatar;

    public GroupAvatar(Avatar newAvatar) {
        super(false);
        this.newAvatar = newAvatar;

    }

    public GroupAvatar() {

    }

    public Avatar getNewAvatar() {
        return newAvatar;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        newAvatar = values.optObj(1, Avatar.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        if (newAvatar != null) {
            writer.writeObject(1, newAvatar);
        }
    }
}
