package im.actor.console.entity;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.User;

import java.io.IOException;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class UserEntity extends BserObject {

    private User user;

    public UserEntity(User user) {
        this.user = user;
    }

    public UserEntity() {

    }

    public User getUser() {
        return user;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        AvatarEntity avatarEntity = values.optObj(5, new AvatarEntity());
        this.user = new User(values.getInt(1), values.getLong(2), values.getString(3), values.optString(4),
                avatarEntity != null ? avatarEntity.getAvatar() : null);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, user.getUid());
        writer.writeLong(2, user.getAccessHash());
        writer.writeString(3, user.getServerName());
        if (user.getLocalName() != null) {
            writer.writeString(4, user.getLocalName());
        }
        if (user.getAvatar() != null) {
            writer.writeObject(5, new AvatarEntity(user.getAvatar()));
        }
    }
}
